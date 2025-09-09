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

        // Local Flutter AARs repository - committed to source control
        maven {
            url = uri("libs")
            metadataSources {
                gradleMetadata()
                mavenPom()
            }
        }
    }
}

rootProject.name = "AzeooSDK"
include(":app")
include(":AzeooSDK")

