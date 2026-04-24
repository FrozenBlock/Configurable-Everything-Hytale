import dev.hygradle.dsl.plugin.LatePlugin
import dev.hygradle.dsl.run.Run

plugins {
    `java-library`
    idea
    `maven-publish`
    id("dev.hygradle")
}

group = "net.frozenblock"
version = "0.0.1"
val javaVersion = 25

repositories {
    maven("https://maven.hytale-modding.info/releases") {
        name = "HytaleModdingReleases"
    }
    maven("https://maven.hytale.com/release")
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
    maven("https://maven.eufonia.studio/public")
    mavenCentral()
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)

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
    //dependsOn(tasks.updatePluginManifest)
}

hygradle {
    plugins {
        register<LatePlugin>("configurable_everything") {
            manifest {
                mainClass = "net.frozenblock.configurableeverything.CEPlugin"
                name = "Configurable Everything"
                group = "FrozenBlock"
                version = project.version.toString()
                description = "Adds configurability to Hytale"
                includesAssetPack = true

                author { name = "Ethan Stokes" }

                dependency { name = "NPC"; group = "Hytale"; version = "*" }
            }
        }
    }

    runs {
        register<Run>("dev")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-deprecation", "-Xlint:all"))
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
