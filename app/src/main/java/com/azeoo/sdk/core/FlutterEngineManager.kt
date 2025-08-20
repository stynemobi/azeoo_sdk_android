package com.azeoo.sdk.core

import android.content.Context
import android.util.Log
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean
import com.azeoo.sdk.data.model.SDKConfiguration
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Flutter Engine Manager
 * 
 * Manages the Flutter engine lifecycle and method channel communication
 * using Android best practices:
 * - Singleton pattern for engine management
 * - Coroutines for async operations
 * - Proper lifecycle management
 * - Thread-safe operations
 * - Resource cleanup
 */
object FlutterEngineManager {
    
    private const val TAG = "FlutterEngineManager"
    private const val METHOD_CHANNEL_NAME = "com.azeoo.sdk/nutrition"
    
    // Engine state management
    private var flutterEngine: FlutterEngine? = null
    private var methodChannel: MethodChannel? = null
    private val isInitialized = AtomicBoolean(false)
    private val isConfigured = AtomicBoolean(false)
    
    // Coroutine scope for engine operations
    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Initialize Flutter engine with application context
     * This should be called from a background thread
     */
    suspend fun initialize(context: Context): Boolean = withContext(Dispatchers.Main) {
        if (isInitialized.compareAndSet(false, true)) {
            try {
                Log.d(TAG, "Initializing Flutter engine...")
                
                // Create Flutter engine
                flutterEngine = FlutterEngine(context.applicationContext).also { engine ->
                    // Start Dart execution
                    engine.dartExecutor.executeDartEntrypoint(
                        DartExecutor.DartEntrypoint.createDefault()
                    )
                    
                    // Setup method channel
                    methodChannel = MethodChannel(
                        engine.dartExecutor.binaryMessenger,
                        METHOD_CHANNEL_NAME
                    ).also { channel ->
                        setupMethodCallHandler(channel)
                    }
                }
                
                Log.d(TAG, "Flutter engine initialized successfully")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Flutter engine", e)
                isInitialized.set(false)
                cleanup()
                false
            }
        } else {
            Log.d(TAG, "Flutter engine already initialized")
            true
        }
    }
    
    /**
     * Configure Flutter module with SDK configuration
     */
    suspend fun configure(configuration: SDKConfiguration): Boolean = withContext(Dispatchers.Main) {
        if (!isInitialized.get()) {
            Log.e(TAG, "Flutter engine not initialized")
            return@withContext false
        }
        
        try {
            val success = invokeFlutterMethod(
                method = FlutterMethod.ClientUpdateConfiguration,
                arguments = configuration.toMap()
            )
            
            if (success) {
                isConfigured.set(true)
                Log.d(TAG, "Flutter module configured successfully")
            } else {
                Log.e(TAG, "Failed to configure Flutter module")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Flutter configuration", e)
            false
        }
    }
    
    /**
     * Get the Flutter engine instance
     * @return FlutterEngine if initialized, null otherwise
     */
    fun getFlutterEngine(): FlutterEngine? = flutterEngine
    
    /**
     * Get Flutter version information
     */
    fun getFlutterVersion(): String = "3.24.0" // This should match your Flutter version
    
    /**
     * Check if engine is initialized
     */
    fun isInitialized(): Boolean = isInitialized.get()
    
    /**
     * Check if engine is configured
     */
    fun isConfigured(): Boolean = isConfigured.get()
    
    /**
     * Invoke a Flutter method via method channel
     * 
     * @param method Method to invoke (using FlutterMethod sealed class)
     * @param arguments Arguments to pass to Flutter
     * @return true if successful, false otherwise
     */
    suspend fun invokeFlutterMethod(
        method: FlutterMethod,
        arguments: Map<String, Any>? = null
    ): Boolean = suspendCoroutine { continuation ->
        
        val channel = methodChannel
        if (channel == null) {
            Log.e(TAG, "Method channel not available")
            continuation.resume(false)
            return@suspendCoroutine
        }
        
        try {
            channel.invokeMethod(method.methodName, arguments, object : MethodChannel.Result {
                override fun success(result: Any?) {
                    Log.d(TAG, "Flutter method '${method.methodName}' completed successfully")
                    continuation.resume(true)
                }
                
                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    Log.e(TAG, "Flutter method '${method.methodName}' failed: $errorCode - $errorMessage")
                    continuation.resume(false)
                }
                
                override fun notImplemented() {
                    Log.e(TAG, "Flutter method '${method.methodName}' not implemented")
                    continuation.resume(false)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception invoking Flutter method '${method.methodName}'", e)
            continuation.resume(false)
        }
    }
    
    /**
     * Launch main screen
     */
    suspend fun launchMainScreen(bottomSafeArea: Boolean = true): Boolean {
        return invokeFlutterMethod(
            method = FlutterMethod.UIShowMainScreen,
            arguments = mapOf("bottomSafeArea" to bottomSafeArea)
        )
    }
    
    /**
     * Launch permission test screen
     */
    suspend fun launchPermissionTestScreen(): Boolean {
        return invokeFlutterMethod(method = FlutterMethod.UIShowPermissionTestScreen)
    }
    
    /**
     * Navigate to specific screen
     */
    suspend fun navigateToScreen(
        screenName: String,
        parameters: Map<String, Any>? = null
    ): Boolean {
        val args = mutableMapOf<String, Any>("screenName" to screenName)
        parameters?.let { args["parameters"] = it }
        
        return invokeFlutterMethod(
            method = FlutterMethod.UINavigateToScreen,
            arguments = args
        )
    }
    
    /**
     * Update theme
     */
    suspend fun updateTheme(isDark: Boolean): Boolean {
        return invokeFlutterMethod(
            method = FlutterMethod.UISetTheme,
            arguments = mapOf("themeName" to if (isDark) "dark" else "light")
        )
    }
    
    /**
     * Clean up Flutter engine resources
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up Flutter engine...")
        
        methodChannel?.setMethodCallHandler(null)
        methodChannel = null
        
        flutterEngine?.destroy()
        flutterEngine = null
        
        engineScope.cancel()
        
        isInitialized.set(false)
        isConfigured.set(false)
        
        Log.d(TAG, "Flutter engine cleanup completed")
    }
    
    /**
     * Setup method call handler for Flutter -> Android communication
     */
    private fun setupMethodCallHandler(channel: MethodChannel) {
        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "getDeviceInfo" -> {
                    result.success(getDeviceInfo())
                }
                "logMessage" -> {
                    val message = call.arguments as? String ?: "No message"
                    Log.d(TAG, "Flutter Log: $message")
                    result.success(null)
                }
                "onScreenVisible" -> {
                    val screenName = call.arguments as? String ?: "unknown"
                    Log.d(TAG, "Screen visible: $screenName")
                    result.success(null)
                }
                "onScreenHidden" -> {
                    val screenName = call.arguments as? String ?: "unknown"
                    Log.d(TAG, "Screen hidden: $screenName")
                    result.success(null)
                }
                else -> {
                    Log.w(TAG, "Unknown method call from Flutter: ${call.method}")
                    result.notImplemented()
                }
            }
        }
    }
    
    /**
     * Get device information for Flutter
     */
    private fun getDeviceInfo(): Map<String, String> = mapOf(
        "platform" to "Android",
        "version" to android.os.Build.VERSION.RELEASE,
        "model" to android.os.Build.MODEL,
        "manufacturer" to android.os.Build.MANUFACTURER,
        "brand" to android.os.Build.BRAND,
        "device" to android.os.Build.DEVICE
    )
}