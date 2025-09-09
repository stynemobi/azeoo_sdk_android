# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Azeoo Android SDK** - a Flutter-based nutrition and training SDK wrapped for native Android integration. The project provides:

- **Nutrition Module**: Food tracking, meal planning, barcode scanning, recipe management
- **Training Module**: Workout plans, exercises, progress tracking, scheduling  
- **Native Integration**: Seamless integration with existing Android applications
- **Flutter Engine Management**: Optimized engine lifecycle and caching
- **JitPack Publishing**: Maven package distribution via JitPack

## Architecture

### Project Structure
```
sdk/android/
├── AzeooSDK/           # Main SDK library module
│   ├── src/main/java/com/azeoo/sdk/
│   │   ├── client/     # AzeooClient - main SDK entry point
│   │   ├── ui/         # AzeooUI - UI management and modules
│   │   ├── user/       # AzeooUser - user management
│   │   ├── config/     # Configuration classes
│   │   └── core/       # Core Flutter communication layer
├── app/                # Demo/test application
├── libs/               # Flutter AAR dependencies
└── gradle/             # Gradle wrapper and configuration
```

### Key Components

1. **AzeooClient** (`AzeooSDK/src/main/java/com/azeoo/sdk/client/AzeooClient.kt`)
   - Main SDK entry point and initialization
   - API key management and validation
   - Subscription handling
   - Coroutine and callback-based APIs

2. **AzeooUI** (`AzeooSDK/src/main/java/com/azeoo/sdk/ui/AzeooUI.kt`)
   - UI management and Flutter fragment creation
   - Theme configuration
   - Screen navigation
   - Module management (Nutrition, Training)

3. **Flutter Communication Layer** (`AzeooSDK/src/main/java/com/azeoo/sdk/core/`)
   - FlutterCommandExecutor: Method channel communication
   - AzeooCore: Flutter engine lifecycle management
   - FlutterMethod: Available Flutter method constants

4. **Configuration System** (`AzeooSDK/src/main/java/com/azeoo/sdk/config/`)
   - Config.kt: Main configuration builder
   - SafeAreaConfig: UI safe area handling

## Common Development Tasks

### Building the Project

```bash
# Clean and build
./gradlew clean

# Build library module
./gradlew :AzeooSDK:build

# Build demo app
./gradlew :app:build

# Build release AAR
./gradlew :AzeooSDK:assembleRelease

# Publish to Maven Local (for testing)
./gradlew :AzeooSDK:publishToMavenLocal
```

### Testing

```bash
# Run unit tests for SDK
./gradlew :AzeooSDK:test

# Run instrumentation tests for SDK
./gradlew :AzeooSDK:connectedAndroidTest

# Run app tests
./gradlew :app:test
./gradlew :app:connectedAndroidTest
```

### Publishing to JitPack

The project uses JitPack for distribution. Key files:
- `jitpack.yml`: JitPack build configuration
- `AzeooSDK/build.gradle.kts`: Maven publishing setup

```bash
# Prepare Flutter AAR dependencies (must be done before publishing)
./gradlew :AzeooSDK:preBuild

# Create release tag for JitPack
git tag -a v2.1.5 -m "Release version 2.1.5"
git push origin v2.1.5
```

### Code Style & Conventions

- **Language**: Kotlin with Java 11 target
- **Async Operations**: Dual API pattern - both callback and coroutine-based methods
- **Naming**: Follow iOS SDK counterparts (AzeooClient, AzeooUI, AzeooUser)
- **Architecture**: Module-based with Flutter method channel communication
- **Error Handling**: Result<T> pattern for callbacks, exceptions for coroutines

### Flutter Dependencies

The SDK depends on Flutter AAR files located in `libs/` directory:
- `flutter_debug.aar` - Debug build
- `flutter_profile.aar` - Profile build  
- `flutter_release.aar` - Release build

These must be built from the main Flutter project and placed in libs/ before building the Android SDK.

### Key Configuration

- **minSdk**: 24 (Android 7.0)
- **compileSdk**: 36
- **targetSdk**: 36
- **Java**: Version 11
- **Kotlin**: Latest stable
- **Gradle**: 8.7+ with Android Gradle Plugin 8.7+

## Important Files to Understand

- `README.md`: Comprehensive API documentation and integration guide
- `INTEGRATION_GUIDE.md`: Step-by-step integration instructions and troubleshooting
- `JITPACK_SETUP.md`: JitPack publishing process and configuration
- `gradle.properties`: Maven publishing metadata and optimization settings
- `settings.gradle.kts`: Repository configuration including Flutter storage

## Working with the Codebase

When making changes:

1. **Follow the dual API pattern**: Provide both callback and coroutine versions for async operations
2. **Match iOS SDK**: Keep API parity with the iOS counterpart where possible
3. **Flutter Communication**: All Flutter interactions go through FlutterCommandExecutor
4. **Error Handling**: Use Result<T> for callbacks, throw exceptions for coroutines
5. **State Management**: Maintain state consistency between Android and Flutter layers
6. **Module Pattern**: Keep functionality organized in modules (Nutrition, Training)

## Testing Integration

The SDK includes both unit tests and instrumentation tests. When adding features:
- Add unit tests for business logic
- Add instrumentation tests for Flutter communication
- Test both callback and coroutine APIs
- Verify error handling paths