include(":web-bridge")

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

    }
    resolutionStrategy {
        eachPlugin {
                if (requested.id.id == "com.google.android.gms.oss-licenses-plugin") {
                    useModule("com.google.android.gms:oss-licenses-plugin:0.10.9")
                }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MCS"

include(
    ":app",
    ":signer",
    ":webapp",
    ":web-bridge",
    ":editor",
    ":editor-lsp",
    ":language-treesitter"
)

include(":core:main")
include(":core:components")
include(":core:resources")
include(":core:terminal-emulator")
include(":core:terminal-view")