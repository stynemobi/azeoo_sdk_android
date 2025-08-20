# Azeoo Nutrition SDK

A Flutter-based nutrition SDK for Android applications that provides comprehensive nutrition tracking, user management, and health metrics functionality.

## Features

- 🍎 **Nutrition Tracking**: Comprehensive nutrition and health metrics
- 👤 **User Management**: User profiles, authentication, and data management
- 🎨 **Customizable UI**: Theme support with light/dark mode
- 🔒 **Secure**: API key authentication and secure data handling
- 📱 **Flexible Integration**: Embed anywhere in your Android app
- 🚀 **Flutter Powered**: Modern, performant Flutter-based UI

## Installation

### JitPack (Recommended for immediate use)

Add JitPack repository to your project-level `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        // ... other repositories
        maven("https://jitpack.io")
    }
}
```

Add the dependency to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.azeoo:azeoo_sdk_android:1.0.0")
}
```

### Maven Central (Coming Soon)

```kotlin
dependencies {
    implementation("com.azeoo.sdk:azeoo-sdk-android:1.0.0")
}
```

## Quick Start

### 1. Initialize the SDK

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Azeoo SDK
        AzeooSDK.initialize(this)
    }
}
```

### 2. Configure the SDK

```kotlin
val config = SDKConfiguration.builder()
    .apiKey("your_api_key")
    .userId("user123")
    .authToken("auth_token")
    .locale("en")
    .enableAnalytics(true)
    .theme(ThemeConfiguration.light())
    .build()

// Configure the SDK
AzeooSDK.configure(config)
```

### 3. Show Nutrition Screen

```kotlin
// Show the main nutrition screen
AzeooUI.showMainScreen(context)
```

## Advanced Usage

### Custom Theme Configuration

```kotlin
val theme = ThemeConfiguration.dark(
    primaryColor = Color.parseColor("#FF6B6B")
)

val config = SDKConfiguration.builder()
    .apiKey("your_api_key")
    .userId("user123")
    .authToken("auth_token")
    .theme(theme)
    .build()
```

### Safe Area Configuration

```kotlin
val safeArea = SafeAreaConfiguration(
    top = true,      // Respect top safe area
    bottom = true,   // Respect bottom safe area
    left = false,    // Don't respect left safe area
    right = false    // Don't respect right safe area
)

val config = SDKConfiguration.builder()
    .apiKey("your_api_key")
    .userId("user123")
    .authToken("auth_token")
    .safeArea(safeArea)
    .build()
```

### Deep Link Support

```kotlin
val deepLinks = DeepLinkConfiguration(
    scheme = "azeoo",
    host = "nutrition",
    enabledPaths = listOf("/profile", "/nutrition"),
    enableUniversalLinks = true
)

val config = SDKConfiguration.builder()
    .apiKey("your_api_key")
    .userId("user123")
    .authToken("auth_token")
    .deepLinks(deepLinks)
    .build()
```

## Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **Kotlin**: 2.0.21+
- **Android Gradle Plugin**: 8.12.1+

## Dependencies

The SDK automatically includes these dependencies:
- Flutter Embedding
- AndroidX Lifecycle
- AndroidX Activity Compose
- Gson for JSON handling
- Kotlin Coroutines

## Architecture

The SDK follows modern Android development best practices:

- **Lifecycle Awareness**: Proper lifecycle management with ProcessLifecycleOwner
- **Coroutines**: Asynchronous operations using Kotlin Coroutines
- **State Management**: Reactive state management with StateFlow
- **Fragment Integration**: Seamless integration with existing Android UI
- **Method Channels**: Flutter-Native communication via method channels

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Support

For support and questions:
- Create an issue on GitHub
- Contact: your.email@example.com

## Changelog

### Version 1.0.0
- Initial release
- Core nutrition tracking functionality
- User management system
- Customizable themes
- Flutter-based UI
