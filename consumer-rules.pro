# Consumer ProGuard rules for Azeoo SDK
# These rules will be automatically applied to apps that include this SDK

# Keep all public SDK APIs that apps might use
-keep public class com.azeoo.sdk.AzeooSDK { *; }
-keep public class com.azeoo.sdk.client.AzeooClient { *; }
-keep public class com.azeoo.sdk.ui.AzeooUI { *; }
-keep public class com.azeoo.sdk.user.AzeooUser { *; }
-keep public class com.azeoo.sdk.config.** { *; }

# Keep Flutter integration classes
-keep class io.flutter.** { *; }
-keep class io.flutter.plugin.common.** { *; }
-keep class io.flutter.embedding.** { *; }

# Keep method channel communication
-keep class * implements io.flutter.plugin.common.MethodChannel$MethodCallHandler { *; }

# Keep data classes used for JSON serialization
-keep class com.azeoo.sdk.config.Config { *; }
-keep class com.azeoo.sdk.config.ThemeConfig { *; }
-keep class com.azeoo.sdk.config.SafeAreaConfig { *; }
-keep class com.azeoo.sdk.config.DeepLinkConfig { *; }

# Keep error classes that might be caught by consuming apps
-keep public class com.azeoo.sdk.core.AzeooError { *; }
-keep public class com.azeoo.sdk.client.AzeooClientError { *; }
-keep public class com.azeoo.sdk.ui.AzeooUIError { *; }
-keep public class com.azeoo.sdk.user.AzeooUserError { *; }
-keep public class com.azeoo.sdk.config.ConfigError { *; }

# Preserve enum values and methods
-keepclassmembers enum com.azeoo.sdk.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep companion objects
-keep class com.azeoo.sdk.**.Companion { *; }

# Keep Result type handling
-keep class kotlin.Result { *; }