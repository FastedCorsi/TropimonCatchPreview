param(
    [string]$LauncherRoot = $(if ($env:APPDATA) { Join-Path $env:APPDATA '.tropimon' } else { $env:TROPIMON_HOME }),
    [switch]$CheckOnly
)

# By FastedCorsi. External local helper; not included in the public JAR.
$ErrorActionPreference = 'Stop'
$sources = @(Get-ChildItem -LiteralPath $PSScriptRoot -Filter 'TropimonCatchPreview-*-LOCAL.jar' -File)
if ($sources.Count -ne 1) { throw 'Exactly one local delivery JAR is required.' }
& (Join-Path $PSScriptRoot 'InstallManagedLocalMod.ps1') -SourceJar $sources[0].FullName `
    -ExpectedModId 'tropimon_catch_preview' -LauncherRoot $LauncherRoot -CheckOnly:$CheckOnly
exit $LASTEXITCODE

