plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-parcelize")
    `maven-publish`
}

android {
    namespace = "com.azeoo.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    testOptions {
        targetSdk = 36
    }
    
    lint {
        targetSdk = 36
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    //debugImplementation("com.azeoo.sdk:flutter_debug:1.0")
    releaseImplementation("com.azeoo.sdk:flutter_release:1.0")
    add("profileImplementation", "com.azeoo.sdk:flutter_profile:1.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.gson)
    implementation(libs.flutter.embedding)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Publishing configuration for JitPack and Maven
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                
                groupId = "com.azeoo.sdk"
                artifactId = "azeoo-sdk-android"
                version = "1.0.0"
                
                pom {
                    name.set("Azeoo SDK")
                    description.set("A Flutter-based Azeoo SDK for Android applications")
                    url.set("https://bitbucket.org/azeoo/azeoo_sdk_android")
                    
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
                            email.set("puvistyne.rajasegar@azeoo.com")
                        }
                    }
                    
                    scm {
                        connection.set("scm:git:git://bitbucket.org/azeoo/azeoo_sdk_android.git")
                        developerConnection.set("scm:git:ssh://bitbucket.org/azeoo/azeoo_sdk_android.git")
                        url.set("https://bitbucket.org/azeoo/azeoo_sdk_android")
                    }
                }
            }
        }
    }
}