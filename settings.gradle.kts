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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://storage.googleapis.com/download.flutter.io")
        // maven("/Users/stein/Documents/DEV/AZEOO/sdk/nutrition_sdk/build/host/outputs/repo")
        maven("https://jitpack.io")
        // Local Flutter AARs repository - contains the built Flutter SDK files
        flatDir {
            dirs("AzeooSDK/libs")
        }
        maven {
            url = uri("AzeooSDK/libs")
        }
    }
}

rootProject.name = "AzeooSDK"
include(":app")
include(":AzeooSDK")

