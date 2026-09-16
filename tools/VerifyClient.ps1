param(
    [ValidateSet('standalone', 'integrations', 'coexistence')][string]$Mode = 'standalone',
    [string]$LauncherDirectory = $env:TROPIMON_HOME
)
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (!$LauncherDirectory) { $LauncherDirectory = Join-Path $env:APPDATA '.tropimon' }
$launcher = $LauncherDirectory
$run = Join-Path $project "build/verify-$Mode"
if (Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'" |
        Where-Object { $_.CommandLine -and $_.CommandLine.Contains($run) }) {
    throw 'Close this isolated test instance before replacing its test JAR.'
}
$mods = Join-Path $run 'mods'
New-Item -ItemType Directory -Force $mods | Out-Null
$modVersion = ((Get-Content (Join-Path $project 'gradle.properties') | Select-String '^mod_version=').Line -split '=', 2)[1]
$artifact = Join-Path $project "build/libs/tropimon-catch-preview-$modVersion.jar"
if (!(Test-Path -LiteralPath $artifact)) { throw 'Build the release JAR first.' }
Copy-Item -LiteralPath $artifact -Destination $mods
$patterns = @('Cobblemon-fabric-1.7.2+1.21.1.jar', 'fabric-api-0.116.6+1.21.1.jar', 'fabric-language-kotlin-*.jar')
if ($Mode -eq 'coexistence') { $patterns += @('*Tropi*.jar', '*geckolib*.jar', '*XaerosWorldMap*.jar') }
if ($Mode -eq 'integrations') { $patterns += @('TropimonTeamBuilder-*.jar', 'TropimonDamageCalc-*.jar', 'TropimonChatFilter-*.jar') }
foreach ($pattern in $patterns) {
    Get-ChildItem (Join-Path $launcher 'mods') -Filter $pattern |
        Where-Object { $_.Name -notlike '*CatchPreview*' } |
        ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination $mods }
}
if ($Mode -eq 'coexistence') {
    # Launcher-provided dependency of the OTHER mods only; absent from the standalone test.
    $otherModsCore = Join-Path $launcher 'common/mods/fabric/fr/tropimon/tropimodcore/1.0.1+1.21.1/tropimodcore-1.0.1+1.21.1.jar'
    if (Test-Path -LiteralPath $otherModsCore) { Copy-Item -LiteralPath $otherModsCore -Destination $mods }
}
$version = Get-Content (Join-Path $launcher '1.21.1.json') -Raw | ConvertFrom-Json
$loader = Get-Content (Join-Path $launcher 'fabric-loader-0.17.3-1.21.1.json') -Raw | ConvertFrom-Json
$classpath = [Collections.Generic.List[string]]::new()
foreach ($library in @($loader.libraries) + @($version.libraries)) {
    $allowed = !$library.rules
    foreach ($rule in $library.rules) {
        if (!$rule.os -or (!$rule.os.name -or $rule.os.name -eq 'windows')) { $allowed = $rule.action -eq 'allow' }
    }
    if (!$allowed) { continue }
    $relative = $library.downloads.artifact.path
    if (!$relative) {
        $parts = $library.name.Split(':')
        $relative = $parts[0].Replace('.', '/') + '/' + $parts[1] + '/' + $parts[2] + '/' + $parts[1] + '-' + $parts[2] + '.jar'
    }
    $path = Join-Path (Join-Path $launcher 'libraries') $relative
    if (!(Test-Path -LiteralPath $path)) { throw "Missing library: $relative" }
    $classpath.Add($path)
}
$classpath.Add((Join-Path $launcher 'client.jar'))
$java = Join-Path $launcher 'runtime/x64/jdk-21.0.6+7/bin/java.exe'
$arguments = @('-Xmx3G', '-Dfabric.log.disableAnsi=true',
    "-Djava.library.path=$(Join-Path $launcher 'natives')",
    '-cp', ($classpath -join ';'), $loader.mainClass,
    '--username', 'CatchPreviewTest', '--uuid', '00000000000000000000000000000001',
    '--accessToken', '0', '--version', '1.21.1', '--userType', 'legacy',
    '--gameDir', $run, '--assetsDir', (Join-Path $launcher 'assets'),
    '--assetIndex', $version.assetIndex.id, '--width', '854', '--height', '480')
Push-Location $run
try { & $java @arguments } finally { Pop-Location }
exit $LASTEXITCODE
