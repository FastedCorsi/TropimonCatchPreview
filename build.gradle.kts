import java.security.MessageDigest
import java.util.zip.ZipFile
import groovy.json.JsonSlurper

plugins {
    id("fabric-loom") version "1.15.5"
    id("maven-publish")
}

version = property("mod_version") as String
group = property("maven_group") as String

base { archivesName.set(property("archives_base_name") as String) }

repositories {
    mavenCentral()
    maven("https://api.modrinth.com/maven")
}

val launcherHome = sequenceOf(
    providers.environmentVariable("TROPIMON_HOME").orNull?.let(::file),
    providers.environmentVariable("APPDATA").orNull?.let { file(it).resolve(".tropimon") },
    file(System.getProperty("user.home")).resolve(".tropimon")
).filterNotNull().firstOrNull { it.resolve("profiles").isDirectory || it.resolve("mods").isDirectory }
val officialDependenciesOnly = providers.gradleProperty("officialDependenciesOnly").isPresent
val cobblemonJar = if (officialDependenciesOnly) null else providers.gradleProperty("cobblemonJar").orNull?.let(::file) ?: run {
    val profiles = launcherHome?.resolve("profiles")?.listFiles()
        ?.filter { it.resolve("instance/mods").isDirectory }.orEmpty()
    if (profiles.size > 1) throw GradleException("Profil actif ambigu ; définir -PcobblemonJar=<jar>.")
    val localMods = profiles.singleOrNull()?.resolve("instance/mods") ?: launcherHome?.resolve("mods")
    val installed = localMods?.listFiles()
        ?.filter { candidate ->
            candidate.isFile && candidate.extension.equals("jar", true) && ZipFile(candidate).use { zip ->
                zip.getEntry("fabric.mod.json")?.let { entry ->
                    zip.getInputStream(entry).reader().use { reader ->
                        (JsonSlurper().parse(reader) as Map<*, *>)["id"] == "cobblemon"
                    }
                } ?: false
            }
        }
        .orEmpty()
    if (installed.size > 1) {
        throw GradleException("Plusieurs JAR Cobblemon détectés ; définir -PcobblemonJar=<jar>.")
    }
    if (localMods?.isDirectory == true && installed.isEmpty()) {
        throw GradleException("Aucun JAR Cobblemon actif ; définir -PcobblemonJar=<jar> pour la matrice explicite.")
    }
    installed.singleOrNull()
}
if (cobblemonJar != null && !cobblemonJar.isFile) {
    throw GradleException("JAR Cobblemon introuvable : définir un -PcobblemonJar valide.")
}
val cobblemonTestJar = configurations.detachedConfiguration(
    dependencies.create("maven.modrinth:MdwFAVRL:${property("cobblemon_modrinth_version")}")
)

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    if (cobblemonJar != null) {
        modImplementation(files(cobblemonJar))
    } else {
        modImplementation("maven.modrinth:MdwFAVRL:${property("cobblemon_modrinth_version")}")
    }
    modRuntimeOnly("net.fabricmc:fabric-language-kotlin:1.13.7+kotlin.2.2.21")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.ow2.asm:asm-tree:9.8")
}

tasks.test {
    useJUnitPlatform()
    doFirst {
        systemProperty("catchpreview.cobblemonJar",
            (cobblemonJar ?: cobblemonTestJar.singleFile).absolutePath)
    }
    providers.gradleProperty("coexistenceAuditDir").orNull?.let {
        systemProperty("catchpreview.coexistenceMods", it)
    }
}

loom {
    runs {
        named("client") {
            runDir("build/verify-client")
            programArgs("--username", "CatchPreviewTest")
        }
    }
}

java { withSourcesJar() }

// Offline verification only: never included in the distributable mod.
val smoke by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().output + sourceSets.main.get().runtimeClasspath
}
loom {
    mods {
        create("tropimon_catch_preview") { sourceSet(sourceSets.main.get()) }
        create("catch_preview_smoke") { sourceSet(smoke) }
    }
}
val smokeJar by tasks.registering(Jar::class) {
    from(smoke.output)
    archiveClassifier.set("smoke-dev")
    destinationDirectory.set(layout.buildDirectory.dir("smoke-helper"))
}
tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapSmokeJar") {
    inputFile.set(smokeJar.flatMap { it.archiveFile })
    archiveClassifier.set("smoke")
    destinationDirectory.set(layout.buildDirectory.dir("smoke-helper"))
    addNestedDependencies.set(false)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

val cobblemonMinimumVersion = property("cobblemon_min_version") as String
val verifyCobblemonCompatibility = tasks.register("verifyCobblemonCompatibility") {
    group = "verification"
    description = "Refuse les anciennes bornes Cobblemon avant de fabriquer un JAR."
    inputs.property("cobblemonMinimumVersion", cobblemonMinimumVersion)
    inputs.file("src/main/resources/fabric.mod.json")
    doLast {
        val expected = "\"cobblemon\": \">=$cobblemonMinimumVersion\""
        check(file("src/main/resources/fabric.mod.json").readText().contains(expected)) {
            "fabric.mod.json doit déclarer Cobblemon >=$cobblemonMinimumVersion sans borne maximale artificielle."
        }
    }
}

tasks.processResources {
    dependsOn(verifyCobblemonCompatibility)
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") { expand("version" to project.version) }
}

// Share only reviewed source inputs, never the workspace or local test instances.
val privateContent = listOf("**/.git/**", "**/.env*", "**/*.log", "**/logs/**",
    "**/config/**", "**/configs/**", "**/saves/**", "**/screenshots/**",
    "**/crash-reports/**", "**/sessions/**", "**/*.jks", "**/*.p12", "**/*.pem")
tasks.withType<Jar>().configureEach { exclude(privateContent) }
val shareSources by tasks.registering(Zip::class) {
    archiveFileName.set("tropimon-catch-preview-${project.version}-project.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(projectDir) {
        include("src/**", "tools/**", "AGENTS.md", "README.md", ".gitignore",
            "build.gradle.kts", "settings.gradle.kts", "gradle.properties")
        exclude(privateContent)
    }
}
val privacyCheck by tasks.registering(Exec::class) {
    dependsOn(tasks.remapJar, tasks.remapSourcesJar, shareSources)
    // No up-to-date shortcut: also check newly introduced files on every delivery.
    commandLine(file(System.getProperty("java.home")).resolve("bin/java").absolutePath,
        "tools/PrivacyCheck.java", projectDir.absolutePath,
        tasks.remapJar.get().archiveFile.get().asFile.absolutePath,
        tasks.remapSourcesJar.get().archiveFile.get().asFile.absolutePath,
        shareSources.get().archiveFile.get().asFile.absolutePath)
}
tasks.check { dependsOn(privacyCheck) }


val prepareReleaseDelivery = tasks.register("prepareReleaseDelivery") {
    group = "distribution"
    description = "Produit les JAR local et partageable vérifiés de la même version."
    dependsOn(tasks.build)
    doLast {
        val source = tasks.remapJar.get().archiveFile.get().asFile
        val deliveryRoot = layout.buildDirectory.dir("delivery/${project.version}").get().asFile
        val shareDirectory = deliveryRoot.resolve("shareable")
        val localDirectory = deliveryRoot.resolve("local")
        check(!shareDirectory.exists() && !localDirectory.exists()) {
            "La livraison existe déjà ; préserver ses fichiers et utiliser une nouvelle version."
        }
        shareDirectory.mkdirs()
        localDirectory.mkdirs()

        fun copyAndHash(target: File) {
            source.copyTo(target, overwrite = true)
            val digest = MessageDigest.getInstance("SHA-256")
            target.inputStream().use { input ->
                val buffer = ByteArray(16 * 1024)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                }
            }
            val hash = digest.digest().joinToString("") { "%02x".format(it) }
            target.resolveSibling(target.name + ".sha256").writeText(hash + System.lineSeparator())
        }

        copyAndHash(shareDirectory.resolve("TropimonCatchPreview-${project.version}+1.21.1.jar"))
        copyAndHash(localDirectory.resolve("TropimonCatchPreview-${project.version}+1.21.1-LOCAL.jar"))
        file("tools/install-local-deferred.ps1")
            .copyTo(localDirectory.resolve("install-local-deferred.ps1"), overwrite = true)
        file("tools/InstallManagedLocalMod.ps1")
            .copyTo(localDirectory.resolve("InstallManagedLocalMod.ps1"), overwrite = true)
    }
}

tasks.register("armReleaseLocal") {
    group = "distribution"
    description = "Arme l'installation locale différée sans arrêter Minecraft ni le launcher."
    dependsOn(prepareReleaseDelivery)
    doLast {
        val script = layout.buildDirectory.file("delivery/${project.version}/local/install-local-deferred.ps1").get().asFile
        ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden",
            "-ExecutionPolicy", "Bypass", "-File", script.absolutePath)
            .directory(script.parentFile)
            .start()
    }
}


