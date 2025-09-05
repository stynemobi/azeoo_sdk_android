# Azeoo SDK for Android

A native Android wrapper for the Azeoo Nutrition & Training SDK built with Flutter. This SDK provides comprehensive nutrition and training features with offline support, theming, localization, and analytics.

## Features

- 🥗 **Nutrition Module**: Food tracking, meal planning, barcode scanning, recipe management
- 🏋️ **Training Module**: Workout plans, exercises, progress tracking, scheduling
- 📱 **Native Integration**: Seamless integration with existing Android applications
- 🚀 **Performance Optimized**: Flutter engine caching and lifecycle management
- 🔄 **Offline Support**: Built-in caching for offline functionality
- 🎨 **Themeable**: Customizable colors and themes
- 🌍 **Localized**: Multi-language support
- 📊 **Analytics**: Built-in analytics and user tracking

## Installation

### Gradle (Recommended)

Add the JitPack repository to your project's `build.gradle` file:

```gradle
allprojects {
    repositories {
        // ... other repositories
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your app's `build.gradle` file:

```gradle
dependencies {
    implementation 'com.azeoo.sdk:azeoo-sdk-android:1.0.0'
}
```

### Manual AAR Integration

1. Download the AAR file from releases
2. Copy to `app/libs/` directory
3. Add to `build.gradle`:

```gradle
dependencies {
    implementation files('libs/azeoo-sdk-android-1.0.0.aar')
}
```

## Quick Start

### 1. Initialize the SDK

```kotlin
## Import required classes
import com.azeoo.sdk.*

class MainActivity : AppCompatActivity() {
    private lateinit var azeooClient: AzeooClient
    private lateinit var azeooUser: AzeooUser
    private lateinit var azeooUI: AzeooUI
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize SDK
        lifecycleScope.launch {
            initializeSDK()
        }
    }
    
    private suspend fun initializeSDK() {
        try {
            // Step 1: Initialize client with API key
            val config = SDKConfig.Builder()
                .enableAnalytics(true)
                .enableOfflineSupport(true)
                .enableLogging(BuildConfig.DEBUG)
                .build()
                
            azeooClient = AzeooClient.initialize(
                context = this@MainActivity,
                apiKey = "your_api_key_here",
                config = config
            )
            
            // Step 2: Create user instance
            azeooUser = azeooClient.createUser("user_123")
            
            // Step 3: Initialize UI with configuration
            val uiConfig = UIConfig.Builder()
                .userId("user_123")
                .authToken("user_auth_token")
                .locale("en")
                .analyticsEnabled(true)
                .theme(createCustomTheme())
                .build()
                
            azeooUI = azeooClient.createUI(uiConfig)
            
            // SDK is ready to use!
            showNutritionScreen()
            
        } catch (e: Exception) {
            Log.e("AzeooSDK", "Failed to initialize SDK", e)
        }
    }
    
    private fun createCustomTheme(): ThemeConfig {
        return ThemeConfig.Builder()
            .lightPrimaryColor(Color.parseColor("#2196F3"))
            .darkPrimaryColor(Color.parseColor("#1976D2"))
            .successColor(Color.parseColor("#4CAF50"))
            .errorColor(Color.parseColor("#F44336"))
            .build()
    }
    
    private suspend fun showNutritionScreen() {
        azeooUI.nutrition.showMainScreen()
    }
}
```

### 2. Using Modules

#### Nutrition Module

```kotlin
// Show main nutrition screen
azeooUI.nutrition.showMainScreen()

// Show specific screens
azeooUI.nutrition.showNutritionPlans()
azeooUI.nutrition.showRecipes()
azeooUI.nutrition.showBarcodeScanner()
azeooUI.nutrition.showCart()

// Show specific nutrition plan
azeooUI.nutrition.showNutritionPlan("plan_123")

// Show specific recipe
azeooUI.nutrition.showRecipe(recipeId = 456, recipeName = "Healthy Salad")
```

#### Training Module

```kotlin
// Show main training screen
azeooUI.training.showMainScreen()

// Show specific screens
azeooUI.training.showWorkoutPlans()
azeooUI.training.showExercises()
azeooUI.training.showProgress()
azeooUI.training.showSchedule()

// Show specific workout plan
azeooUI.training.showWorkoutPlan("workout_123")

// Show specific exercise
azeooUI.training.showExercise("exercise_456")
```

#### User Management

```kotlin
// Get user profile
val profile = azeooUser.getProfile()
println("User: ${profile.firstName} ${profile.lastName}")

// Update user information
azeooUser.changeHeight(175.5) // cm
azeooUser.changeWeight(70.2)  // kg

// Update profile
val updates = mapOf(
    "firstName" to "John",
    "lastName" to "Doe",
    "email" to "john.doe@example.com"
)
azeooUser.update(updates)

// Get user info
val userInfo = azeooUser.getUserInfo()
println("Subscriptions: ${userInfo.subscriptions}")
```

### 3. Fragment Integration

You can embed Flutter screens directly in your layouts:

```kotlin
class NutritionFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nutrition, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Create Flutter fragment
        val flutterFragment = azeooUI.createFlutterFragment(
            initialRoute = "/nutrition",
            arguments = mapOf("userId" to "user_123")
        )
        
        // Add to container
        childFragmentManager.beginTransaction()
            .replace(R.id.flutter_container, flutterFragment)
            .commit()
    }
}
```

Layout file (`fragment_nutrition.xml`):
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <FrameLayout
        android:id="@+id/flutter_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
</FrameLayout>
```

## Advanced Configuration

### Custom Themes

```kotlin
val themeConfig = ThemeConfig.Builder()
    .lightPrimaryColor(Color.parseColor("#FF5722"))
    .lightSecondaryColor(Color.parseColor("#FFC107"))
    .lightBackgroundColor(Color.WHITE)
    .darkPrimaryColor(Color.parseColor("#D32F2F"))
    .darkSecondaryColor(Color.parseColor("#FF8F00"))
    .darkBackgroundColor(Color.parseColor("#121212"))
    .successColor(Color.parseColor("#4CAF50"))
    .errorColor(Color.parseColor("#F44336"))
    .warningColor(Color.parseColor("#FF9800"))
    .build()
```

### Safe Area Configuration

```kotlin
val safeAreaConfig = SafeAreaConfig.Builder()
    .top(true)     // Respect status bar
    .bottom(false) // Ignore navigation bar
    .left(true)
    .right(true)
    .build()
```

### Deep Link Configuration

```kotlin
val deepLinkConfig = DeepLinkConfig.Builder()
    .customScheme("nutrition")
    .addUniversalLinkHost("nutrition.com")
    .addUniversalLinkHost("app.nutrition.com")
    .enableDebugLogging(BuildConfig.DEBUG)
    .build()
```

### Cache Configuration

```kotlin
val config = SDKConfig.Builder()
    .enableOfflineSupport(true)
    .cacheSize(100 * 1024 * 1024L) // 100MB
    .build()

// Set cache limits after initialization
azeooClient.getCacheManager()?.setCacheLimits(
    memorySizeMB = 50,
    diskSizeMB = 200L
)

// Get cache statistics
val stats = azeooClient.getCacheManager()?.getStatistics()
println("Memory cache: ${stats?.memoryEntries} entries, ${stats?.memorySizeBytes} bytes")
```

## Error Handling

```kotlin
try {
    azeooUI.nutrition.showMainScreen()
} catch (e: SDKInitializationException) {
    Log.e("SDK", "SDK not properly initialized", e)
} catch (e: UIOperationException) {
    Log.e("SDK", "UI operation failed", e)
} catch (e: UserOperationException) {
    Log.e("SDK", "User operation failed", e)
} catch (e: Exception) {
    Log.e("SDK", "Unexpected error", e)
}
```

## Lifecycle Management

The SDK automatically manages Flutter engine lifecycle, but you should dispose resources when done:

```kotlin
override fun onDestroy() {
    super.onDestroy()
    
    // Dispose SDK resources
    if (::azeooClient.isInitialized) {
        azeooClient.dispose()
    }
}
```

## Proguard Rules

Add these rules to your `proguard-rules.pro` file:

```proguard
# Keep Azeoo SDK classes
-keep class com.azeoo.sdk.** { *; }

# Keep Flutter classes
-keep class io.flutter.** { *; }

# Keep Gson classes for JSON serialization
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
```

## Permissions

The SDK requires these permissions in your `AndroidManifest.xml`:

```xml
<!-- Network permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Camera permission for barcode scanning -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Storage permissions for caching -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Troubleshooting

### Common Issues

1. **SDK Initialization Failed**
   - Verify your API key is correct
   - Check network connectivity
   - Ensure all required permissions are granted

2. **Flutter Engine Issues**
   - Clear app data and cache
   - Restart the app
   - Check device available memory

3. **Navigation Not Working**
   - Ensure UI is properly initialized before navigation
   - Check that the target route exists

4. **Cache Issues**
   - Clear app cache in device settings
   - Reduce cache size limits
   - Check available storage space

### Debug Logging

Enable debug logging to get more information:

```kotlin
val config = SDKConfig.Builder()
    .enableLogging(true)
    .build()
```

## API Reference

For detailed API documentation, see the individual class documentation:

- [`AzeooClient`](src/main/kotlin/com/azeoo/sdk/AzeooClient.kt) - Main SDK entry point
- [`AzeooUser`](src/main/kotlin/com/azeoo/sdk/AzeooUser.kt) - User management
- [`AzeooUI`](src/main/kotlin/com/azeoo/sdk/AzeooUI.kt) - UI management
- [`SDKConfig`](src/main/kotlin/com/azeoo/sdk/SDKConfig.kt) - Configuration classes
- [`FlutterEngineManager`](src/main/kotlin/com/azeoo/sdk/FlutterEngineManager.kt) - Engine lifecycle
- [`CacheManager`](src/main/kotlin/com/azeoo/sdk/CacheManager.kt) - Cache management

## Requirements

- Android API level 21 (Android 5.0) or higher
- Kotlin 1.8.0 or higher
- AndroidX libraries

## Support

For support, please contact:
- Email: dev@azeoo.com
- Documentation: [Azeoo SDK Docs](https://docs.azeoo.com)
- Issues: [GitHub Issues](https://github.com/azeoo/mobile-sdk/issues)

## License

This SDK is licensed under the MIT License. See [LICENSE](LICENSE) file for details.