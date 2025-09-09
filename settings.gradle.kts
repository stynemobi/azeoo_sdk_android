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

        // Local Flutter AARs repository - JitPack copies files to build/libs
        // JitPack resolves dependencies from build directory, not source
        maven {
            url = uri("file://${rootProject.projectDir}/build/libs")
            metadataSources {
                gradleMetadata()
                mavenPom()
            }
        }
        // Fallback: Source directory for local development
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

