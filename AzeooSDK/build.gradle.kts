plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}


android {
    namespace = "com.azeoo.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            // Filter for architectures supported by Flutter
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        create("profile") {
            initWith(getByName("debug"))
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

// Repositories are handled by settings.gradle.kts


dependencies {
    // Flutter SDK AARs - use only the release version for all build types
    // This prevents conflicts and matches what we publish as a separate artifact
    implementation("com.azeoo.sdk:flutter_release:1.0.0")

    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    // Define version for all publications - use the project version
    val sdkVersion = project.version.toString()
    
    publishing {
        publications {
            // Main SDK publication
            register<MavenPublication>("release") {
                from(components["release"])
                
                groupId = "com.github.stynemobi"
                artifactId = "azeoo_sdk_android"
                version = sdkVersion
                
                // Exclude Flutter AAR dependencies from published POM so consumers don't try to resolve them
                pom.withXml {
                    val dependenciesNode = asNode().get("dependencies")
                    if (dependenciesNode is groovy.util.NodeList && dependenciesNode.isNotEmpty()) {
                        val deps = dependenciesNode[0] as groovy.util.Node
                        // Remove Flutter dependencies
                        deps.children().removeIf { child ->
                            if (child is groovy.util.Node) {
                                val groupId = child.get("groupId")
                                val artifactId = child.get("artifactId")
                                if (groupId is groovy.util.NodeList && artifactId is groovy.util.NodeList) {
                                    val group = groupId.text()
                                    val artifact = artifactId.text()
                                    group == "com.azeoo.sdk" && artifact.startsWith("flutter_")
                                } else false
                            } else false
                        }
                    }
                }
                
                pom {
                    name.set("Azeoo SDK for Android")
                    description.set("Native Android wrapper for Azeoo SDK - Flutter-based nutrition and health management")
                    url.set("https://github.com/stynemobi/mobile-sdk")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("stynemobi")
                            name.set("Azeoo Team")
                            email.set("dev@azeoo.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/stynemobi/mobile-sdk.git")
                        developerConnection.set("scm:git:ssh://github.com/stynemobi/mobile-sdk.git")
                        url.set("https://github.com/stynemobi/mobile-sdk")
                    }
                }
            }
            
            // Only publish the release Flutter AAR as a separate artifact
            // This is what consuming apps expect to find
            register<MavenPublication>("flutterRelease") {
                groupId = "com.github.stynemobi.azeoo_sdk_android"
                artifactId = "flutter_release"
                version = sdkVersion
                
                // Find and publish the Flutter release AAR
                val flutterReleaseAar = file("../AzeooSDK/flutter-deps/com/azeoo/sdk/flutter_release/1.0.0/flutter_release-1.0.0.aar")
                if (flutterReleaseAar.exists()) {
                    artifact(flutterReleaseAar)
                }
            }
        }
    }
}