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
//        getByName("debug") {
//            signingConfig = signingConfigs.getByName("debug")
//        }
//        create("profile") {
//            initWith(getByName("debug"))
//        }
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
    implementation("androidx.core:core-ktx:1.17.0")

    // Flutter SDK AARs - resolved from the local libs Maven repository
    // Use version 3.0.0 to match the SDK version and ensure JitPack can find them
    //debugImplementation("com.azeoo.sdk:flutter_debug:3.0.0")
    //add("profileImplementation", "com.azeoo.sdk:flutter_profile:3.0.0")
    releaseImplementation("com.azeoo.sdk:flutter_release:1.0")

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
    publishing {
        publications {
            register<MavenPublication>("release") {
//                from(components["release"])
                
                groupId = "com.azeoo.sdk"
                artifactId = "azeoosdk"
                version = "3.0.0"

                afterEvaluate {
                    from(components["release"])
                }

                  
                // Note: Flutter dependencies are embedded in the AAR and should not be published as transitive dependencies
                // They will be available to consuming apps through the local libs repository

                pom {
                    name.set("Azeoo SDK for Android")
                    description.set("Native Android wrapper for Azeoo SDK - Flutter-based nutrition and health management")
                    url.set("https://github.com/azeoo/mobile-sdk")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("azeoo")
                            name.set("Azeoo Team")
                            email.set("dev@azeoo.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/azeoo/mobile-sdk.git")
                        developerConnection.set("scm:git:ssh://github.com/azeoo/mobile-sdk.git")
                        url.set("https://github.com/azeoo/mobile-sdk")
                    }
                }
            }
        }
    }
}