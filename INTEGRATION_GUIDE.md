# Azeoo Android SDK - Integration Guide

This guide helps you integrate the Azeoo Android SDK into your project and resolve common compilation issues.

## Prerequisites

1. **Android SDK 21+** (Android 5.0 Lollipop)
2. **Kotlin 1.8.0+**
3. **Flutter AAR files** (built from the Flutter SDK)

## Step 1: Add Dependencies

Add these dependencies to your app's `build.gradle`:

```gradle
dependencies {
    // Azeoo SDK
    implementation 'com.azeoo.sdk:azeoo-sdk-android:1.0.0'
    
    // Required dependencies
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.fragment:fragment-ktx:1.6.2'
    implementation 'androidx.viewpager2:viewpager2:1.0.0'
    implementation 'com.google.android.material:material:1.11.0'
    
    // Lifecycle components
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-process:2.7.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    
    // JSON processing
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

## Step 2: Add Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<!-- Network permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Camera permission for barcode scanning -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Storage permissions for caching (optional) -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## Step 3: Basic Integration

### Initialize the SDK

```kotlin
import com.azeoo.sdk.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var azeooClient: AzeooClient
    private lateinit var azeooUser: AzeooUser  
    private lateinit var azeooUI: AzeooUI
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        lifecycleScope.launch {
            initializeSDK()
        }
    }
    
    private suspend fun initializeSDK() {
        try {
            // 1. Initialize client
            val config = SDKConfig.Builder()
                .enableAnalytics(true)
                .enableOfflineSupport(true)
                .build()
                
            azeooClient = AzeooClient.initialize(this@MainActivity, "YOUR_API_KEY", config)
            
            // 2. Create user
            azeooUser = azeooClient.createUser("user_123")
            
            // 3. Initialize UI
            val uiConfig = UIConfig.Builder()
                .userId("user_123")
                .locale("en")
                .build()
                
            azeooUI = azeooClient.createUI(uiConfig)
            
            // 4. Show nutrition screen
            azeooUI.nutrition.showMainScreen()
            
        } catch (e: Exception) {
            Log.e("AzeooSDK", "SDK initialization failed", e)
        }
    }
}
```

## Step 4: Common Issue Fixes

### Issue 1: "Cannot resolve symbol" errors

**Solution**: Make sure you have the correct imports:

```kotlin
import com.azeoo.sdk.AzeooClient
import com.azeoo.sdk.AzeooUser
import com.azeoo.sdk.AzeooUI
import com.azeoo.sdk.SDKConfig
import com.azeoo.sdk.UIConfig
```

### Issue 2: ViewPager2 and Material Components errors

**Solution**: Add the missing dependencies:

```gradle
implementation 'androidx.viewpager2:viewpager2:1.0.0'
implementation 'com.google.android.material:material:1.11.0'
```

### Issue 3: "Return type mismatch" for FlutterFragment

**Solution**: The method has been fixed to return `FlutterFragment` properly. Update to latest SDK version.

### Issue 4: Coroutines "suspension point in critical section"

**Solution**: The code has been refactored to avoid this issue. Update to latest SDK version.

### Issue 5: Kotlin compilation target mismatch

**Solution**: Ensure your `build.gradle` has consistent Java/Kotlin versions:

```gradle
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = '1.8'
    }
}
```

## Step 5: Advanced Usage

### Fragment Integration

```kotlin
class NutritionFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_nutrition, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val azeooUI = AzeooClient.getInstance().getUI()
        val flutterFragment = azeooUI?.createFlutterFragment("/nutrition")
        
        flutterFragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.flutter_container, it)
                .commit()
        }
    }
}
```

### Custom Theme

```kotlin
val theme = ThemeConfig.Builder()
    .lightPrimaryColor(Color.parseColor("#2196F3"))
    .darkPrimaryColor(Color.parseColor("#1976D2"))  
    .successColor(Color.parseColor("#4CAF50"))
    .errorColor(Color.parseColor("#F44336"))
    .build()

val uiConfig = UIConfig.Builder()
    .theme(theme)
    .build()
```

### User Management

```kotlin
// Update user profile
lifecycleScope.launch {
    azeooUser.changeHeight(175.0)
    azeooUser.changeWeight(70.5)
    
    val profile = azeooUser.getProfile()
    Log.d("Profile", "Height: ${profile.height}, Weight: ${profile.weight}")
}
```

## Step 6: Testing

Create a test to verify integration:

```kotlin
@Test
fun testSDKInitialization() {
    val config = SDKConfig.Builder().build()
    
    assertNotNull(config)
    assertTrue(config.enableAnalytics)
    assertTrue(config.enableOfflineSupport)
}
```

## Step 7: Troubleshooting

### Build Issues

1. **Clean and rebuild**: `./gradlew clean build`
2. **Check dependencies**: Make sure all required dependencies are included
3. **Verify API key**: Ensure your API key is correct
4. **Check network**: SDK requires internet connection for initialization

### Runtime Issues

1. **Check permissions**: Ensure all required permissions are granted
2. **Verify initialization**: Make sure SDK is initialized before use
3. **Check logs**: Enable debug logging to see detailed error messages

### ProGuard Issues

Add these rules to your `proguard-rules.pro`:

```proguard
-keep class com.azeoo.sdk.** { *; }
-keep class io.flutter.** { *; }
-keep class com.google.gson.** { *; }
```

## Step 8: Migration Guide

If upgrading from an older version:

1. Update import statements
2. Replace deprecated methods
3. Update configuration builders
4. Test all functionality

## Support

For additional support:

- **Documentation**: Check the main README.md
- **Examples**: Look at ExampleActivity.kt and ExampleFragment.kt
- **Issues**: Report bugs with detailed logs and reproduction steps

## Next Steps

1. Explore nutrition and training modules
2. Implement custom themes
3. Add analytics tracking
4. Set up offline support
5. Configure deep linking

---

For complete API reference, see the individual class documentation in the SDK source code.