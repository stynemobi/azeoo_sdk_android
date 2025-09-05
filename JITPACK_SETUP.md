# JitPack Setup Guide for Azeoo Nutrition SDK Android

This guide explains how to publish the Azeoo Nutrition SDK Android to JitPack for easy integration into Android projects.

## Repository Structure

```
sdk/android/
├── build.gradle              # Main build configuration
├── jitpack.yml              # JitPack build configuration
├── scripts/
│   ├── setup.sh             # JitPack setup script
│   └── build_with_flutter.sh # Flutter AAR build script
└── libs/                    # Flutter AAR files (generated)
```

## Prerequisites

1. **Flutter SDK** installed and in PATH
2. **Java 11** (OpenJDK 11) for JitPack builds
3. **Git** repository with proper access
4. **Bitbucket repository**: https://bitbucket.org/azeoo/mobile-sdk/src/main/

## Step 1: Prepare Your Repository

### 1.1 Ensure JitPack Configuration

Your `jitpack.yml` should contain:

```yaml
group: com.azeoo.sdk
jdk:
  - openjdk11

before_install:
  - ./scripts/setup.sh

install:
  - echo "Running custom install command"
  - ./gradlew clean build publishToMavenLocal

env:
  GRADLE_OPTS: -Dorg.gradle.daemon=false -Dorg.gradle.configureondemand=true -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8"
```

### 1.2 Verify Build Configuration

Your `build.gradle` should have:

```gradle
plugins {
    id 'com.android.library' version '8.7.0'
    id 'org.jetbrains.kotlin.android' version '2.0.20'
    id 'maven-publish'
}

android {
    namespace 'com.azeoo.sdk'
    compileSdk 34
    
    defaultConfig {
        minSdk 21
        targetSdk 34
        // ... other config
    }
}

afterEvaluate {
    publishing {
        publications {
            release(MavenPublication) {
                from components.release
                
                groupId = 'com.azeoo.sdk'
                artifactId = 'azeoo-sdk-android'
                version = '1.0.0'
                
                pom {
                    name = 'Azeoo SDK for Android'
                    description = 'Native Android wrapper for Azeoo SDK - Flutter-based nutrition and health management'
                    url = 'https://bitbucket.org/azeoo/mobile-sdk'
                    // ... other POM info
                }
            }
        }
    }
}
```

## Step 2: Build Flutter AAR Files

Before publishing to JitPack, you need to build the Flutter AAR files:

```bash
cd sdk/android
./scripts/build_with_flutter.sh
```

This script will:
1. Build Flutter AAR files from your main project
2. Copy them to the `libs/` directory
3. Build the Android SDK
4. Optionally publish to Maven Local

## Step 3: Commit and Push Changes

```bash
# Add all changes
git add .

# Commit with descriptive message
git commit -m "Prepare Android SDK for JitPack publishing

- Update JitPack configuration
- Fix build scripts
- Update package metadata
- Prepare Flutter AAR integration"

# Push to main branch
git push origin main
```

## Step 4: Create Release Tag

JitPack uses Git tags to determine versions:

```bash
# Create annotated tag
git tag -a v1.0.0 -m "Release version 1.0.0"

# Push tag to remote
git push origin v1.0.0
```

## Step 5: Publish to JitPack

### 5.1 Connect Repository to JitPack

1. Go to [JitPack.io](https://jitpack.io)
2. Sign in with your Bitbucket account
3. Add your repository: `azeoo/mobile-sdk`
4. JitPack will automatically detect your `jitpack.yml`

### 5.2 Monitor Build Process

1. JitPack will start building when you push the tag
2. Monitor the build logs for any errors
3. The build process will:
   - Run `./scripts/setup.sh`
   - Execute `./gradlew clean build publishToMavenLocal`
   - Generate Maven artifacts

### 5.3 Verify Publication

Once the build succeeds, your package will be available at:

```
org.bitbucket.azeoo:azeoo_sdk_android:Tag
```

Where `Tag` is your Git tag (e.g., `v1.0.0`, `main`, etc.)

## Step 6: Test Integration

### 6.1 Add JitPack Repository

In your Android project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 6.2 Add Dependency

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("org.bitbucket.azeoo:azeoo_sdk_android:Tag")
}
```

**Note**: Replace `Tag` with your actual Git tag (e.g., `v1.0.0`) or use `main` for the latest commit on the main branch.

### 6.3 Test Build

```bash
./gradlew clean build
```

## Troubleshooting

### Common Issues

1. **Build Fails on JitPack**
   - Check JitPack build logs
   - Verify Java 11 compatibility
   - Ensure all scripts are executable

2. **Flutter AAR Not Found**
   - Run `./scripts/build_with_flutter.sh` locally first
   - Verify AAR files are in `libs/` directory

3. **Package Not Found**
   - Check JitPack repository URL
   - Verify group ID and artifact ID
   - Ensure version tag is pushed

### Debug Commands

```bash
# Check JitPack build status
curl "https://jitpack.io/api/builds/azeoo/mobile-sdk"

# Verify local build
./gradlew clean build --info

# Check AAR files
ls -la libs/*.aar
```

## Version Management

For future releases:

```bash
# Update version in build.gradle
# Create new tag
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin v1.1.0

# JitPack will automatically build the new version
```

## Support

- **JitPack Documentation**: https://jitpack.io/docs/
- **Bitbucket Repository**: https://bitbucket.org/azeoo/mobile-sdk/src/main/
- **Build Issues**: Check JitPack build logs and GitHub issues

## Notes

- JitPack builds are triggered by Git tags
- Each tag creates a new version
- Build artifacts are cached for faster subsequent builds
- The `group` field in `jitpack.yml` determines the package group ID
- Flutter AAR files must be built before the Android SDK build
