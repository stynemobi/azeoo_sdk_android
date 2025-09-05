# Azeoo SDK ProGuard Rules
# Keep all public APIs
-keep public class com.azeoo.sdk.** { *; }

# Keep Flutter related classes
-keep class io.flutter.** { *; }
-dontwarn io.flutter.**

# Keep method channel classes
-keep class io.flutter.plugin.common.** { *; }
-keep class io.flutter.embedding.** { *; }

# Keep Gson classes for JSON serialization
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Keep reflection-based serialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep config data classes
-keep class com.azeoo.sdk.config.** { *; }

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep LiveData and ViewModel
-keep class androidx.lifecycle.** { *; }

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# General Android optimizations
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}