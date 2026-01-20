import fr.smolder.hytale.gradle.PatchLine

plugins {
    java
    idea
    `maven-publish`
    id("fr.smolder.hytale.dev") version "0.0.3"
    id("fr.smolder.javadoc.migration") version "0.0.1"
}

group = "net.frozenblock"
version = "0.0.1"
val javaVersion = 25

repositories {
    mavenCentral()
    maven("https://maven.hytale-modding.info/releases") {
        name = "HytaleModdingReleases"
    }
    maven("https://repo.averix.tech/repository/maven-public/") {
        name = "Averix"
    }
    exclusiveContent {
        forRepository {
            maven("https://cursemaven.com")
        }
        filter {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)

    compileOnly("curse.maven:hyxin-1405491:7399430")

    // this mod is optional, but is included so you can preview your mod icon
    // in the in-game mod list via the /modlist command
    runtimeOnly(libs.bettermodlist)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withSourcesJar()
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(tasks.updatePluginManifest)
}

tasks.processResources {
    var replaceProperties = mapOf(
        "plugin_group" to findProperty("plugin_group"),
        "plugin_maven_group" to project.group,
        "plugin_name" to project.name,
        "plugin_version" to version,
        "server_version" to findProperty("server_version"),

        "plugin_description" to findProperty("plugin_description"),
        "plugin_website" to findProperty("plugin_website"),

        "plugin_main_entrypoint" to findProperty("plugin_main_entrypoint"),
        "plugin_author" to findProperty("plugin_author")
    )

    inputs.properties(replaceProperties)

    filesMatching("manifest.json") {
        expand(replaceProperties)
    }
}

hytale {
    //jvmArgs.add("-Dhyxin-target=${sourceSets.main.get().output.joinToString(",")}")
    autoUpdateManifest.set(true)
    earlyPlugin.set(true)

    patchLine.set(PatchLine.PRE_RELEASE)
}

javadocMigration {
    // Fetch documentation from the published JSON
    docsUrl.set("https://raw.githubusercontent.com/GhostRider584/hytale-docs/refs/heads/master/javadocs-export.json")

    // Or use a local file
    // docsFile.set(file("path/to/hytale-docs.json"))

    // The server JAR to inject documentation into
    newJar.set(hytale.serverJar)

    // Output directory for documented sources
    outputDir.set(layout.buildDirectory.dir("documented-sources"))

    // Filter which packages to document
    decompileFilter.set(listOf("com/hypixel/**"))
}

tasks.withType<Jar> {
    manifest {
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = version
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] =
            providers.environmentVariable("COMMIT_SHA_SHORT")
                .map { "${version}-${it}" }
                .getOrElse(version.toString())
    }
}

publishing {
    repositories {
        // This is where you put repositories that you want to publish to.
        // Do NOT put repositories for your dependencies here.
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
