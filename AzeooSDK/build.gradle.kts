plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

// Disable Gradle module metadata generation to prevent Flutter dependencies from appearing
tasks.withType<GenerateModuleMetadata> {
    enabled = false
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
    
    packagingOptions {
        // Include Flutter AARs in the final AAR
        pickFirst("**/flutter_*.aar")
        pickFirst("**/flutter_*.jar")
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
    // Flutter SDK AARs - Use api to expose to JitPack consumers
    debugApi("com.azeoo.sdk:flutter_debug:1.0.0")
    add("profileApi", "com.azeoo.sdk:flutter_profile:1.0.0")
    releaseApi("com.azeoo.sdk:flutter_release:1.0.0")


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
    val sdkVersion = "1.0.11"
    
    publishing {
        publications {
            // Main SDK publication
            register<MavenPublication>("release") {
                from(components["release"])
                
                groupId = "com.github.stynemobi"
                artifactId = "azeoo_sdk_android"
                version = sdkVersion
                
                // Transform Flutter dependencies to use JitPack coordinates in POM
                pom.withXml {
                    val dependenciesNode = asNode().get("dependencies")
                    if (dependenciesNode is groovy.util.NodeList && dependenciesNode.isNotEmpty()) {
                        val deps = dependenciesNode[0] as groovy.util.Node
                        // Transform Flutter dependencies to JitPack coordinates
                        deps.children().forEach { child ->
                            if (child is groovy.util.Node) {
                                val groupId = child.get("groupId")
                                val artifactId = child.get("artifactId")
                                if (groupId is groovy.util.NodeList && artifactId is groovy.util.NodeList) {
                                    val group = groupId.text()
                                    val artifact = artifactId.text()
                                    if (group == "com.azeoo.sdk" && artifact.startsWith("flutter_")) {
                                        // Transform to JitPack coordinates
                                        (groupId[0] as groovy.util.Node).setValue("com.github.stynemobi.azeoo_sdk_android")
                                        val version = child.get("version")
                                        if (version is groovy.util.NodeList) {
                                            (version[0] as groovy.util.Node).setValue(sdkVersion)
                                        }
                                    }
                                }
                            }
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
            
            // Flutter release AAR publication
            register<MavenPublication>("flutterRelease") {
                groupId = "com.github.stynemobi.azeoo_sdk_android"
                artifactId = "flutter_release"
                version = sdkVersion

                // Find and publish the Flutter release AAR
                val flutterReleaseAar = file("flutter-deps/com/azeoo/sdk/flutter_release/1.0.0/flutter_release-1.0.0.aar")
                if (flutterReleaseAar.exists()) {
                    artifact(flutterReleaseAar)
                }
            }
            
          
        }
    }
}