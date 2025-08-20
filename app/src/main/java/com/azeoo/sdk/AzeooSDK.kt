package com.azeoo.sdk

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.azeoo.sdk.core.FlutterEngineManager
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main entry point for the Azeoo SDK
 * 
 * This serves as the primary namespace and initialization point for the SDK.
 * The actual functionality is provided through three main classes:
 * 
 * - AzeooClient: API authentication and subscription management
 * - AzeooUser: User management, profile data, and health metrics  
 * - AzeooUI: Screen navigation, theme management, and Flutter view integration
 * 
 * Following Android best practices with lifecycle awareness and proper initialization flow.
 */
object AzeooSDK : DefaultLifecycleObserver {
    
    private const val TAG = "AzeooSDK"
    
    // Initialization state
    private val isInitialized = AtomicBoolean(false)
    private var application: Application? = null
    
    // Coroutine scope for SDK operations
    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Initialize the SDK with the application context
     * This should be called in your Application.onCreate() method
     * 
     * @param application The Application instance
     */
    fun initialize(application: Application) {
        if (isInitialized.compareAndSet(false, true)) {
            this.application = application
            
            // Register lifecycle observer
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
            
            // Initialize Flutter engine in background
            sdkScope.launch {
                try {
                    val success = FlutterEngineManager.initialize(application)
                    if (!success) {
                        android.util.Log.e(TAG, "Failed to initialize Flutter engine")
                        isInitialized.set(false)
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Exception during Flutter engine initialization", e)
                    isInitialized.set(false)
                }
            }
        }
    }
    
    /**
     * Check if the SDK has been initialized
     * 
     * @return True if initialized, false otherwise
     */
    fun isInitialized(): Boolean = isInitialized.get()
    
    /**
     * Get SDK version information
     * 
     * @return Map containing version details
     */
    fun getVersionInfo(): Map<String, String> = mapOf(
        "version" to "1.0.0",
        "buildNumber" to "1",
        "platform" to "Android",
        "flutterVersion" to FlutterEngineManager.getFlutterVersion()
    )
    
    /**
     * Lifecycle callback - called when app goes to background
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Handle app backgrounding if needed
    }
    
    /**
     * Lifecycle callback - called when app is destroyed
     */
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        cleanup()
    }
    
    /**
     * Clean up resources when SDK is no longer needed
     */
    private fun cleanup() {
        sdkScope.cancel()
        FlutterEngineManager.cleanup()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        
        isInitialized.set(false)
        application = null
    }
}