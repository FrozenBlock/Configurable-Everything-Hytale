pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.hytale-modding.info/releases") {
            name = "HytaleModdingReleases"
        }
        maven("https://repo.smolder.fr/public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Configurable Everything HY"

localRepository("Hyxin", "curse.maven:hyxin-1405491", kotlin = true, enabled = true)

includeBuild("../hytale-gradle-plugin") {}

fun localRepository(repo: String, dependencySub: String, kotlin: Boolean, enabled: Boolean) {
    if (!enabled) return
    println("Attempting to include local repo $repo")

    val github = System.getenv("GITHUB_ACTIONS") == "true"

    val allowLocalRepoUse = true
    val allowLocalRepoInConsoleMode = true

    val androidInjectedInvokedFromIde by extra("android.injected.invoked.from.ide")
    val xpcServiceName by extra("XPC_SERVICE_NAME")
    val ideaInitialDirectory by extra("IDEA_INITIAL_DIRECTORY")

    val isIDE = androidInjectedInvokedFromIde != "" || (System.getenv(xpcServiceName) ?: "").contains("intellij") || (System.getenv(xpcServiceName) ?: "").contains(".idea") || System.getenv(ideaInitialDirectory) != null

    var path = "../$repo"
    var file = File(path)

    if (allowLocalRepoUse && (isIDE || allowLocalRepoInConsoleMode)) {
        if (github) {
            path = repo
            file = File(path)
            println("Running on GitHub")
        }
        if (file.exists()) {
            includeBuild(path) {
                dependencySubstitution {
                    if (dependencySub != "") {
                        substitute(module(dependencySub)).using(project(":"))
                    }
                }
            }
            println("Included local repo $repo")
        } else {
            println("Local repo $repo not found")
        }
    }
}