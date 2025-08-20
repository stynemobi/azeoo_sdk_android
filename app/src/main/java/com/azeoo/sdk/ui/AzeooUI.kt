package com.azeoo.sdk.ui

import android.content.Context
import android.graphics.Color
import com.azeoo.sdk.client.AzeooClient
import com.azeoo.sdk.core.FlutterEngineManager
import com.azeoo.sdk.core.FlutterMethod
import com.azeoo.sdk.data.exception.SDKException
import com.azeoo.sdk.data.model.SafeAreaConfiguration
import com.azeoo.sdk.data.model.SDKConfiguration
import com.azeoo.sdk.presentation.AzeooFlutterActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AzeooUI class for managing UI-related functionality
 * 
 * Handles screen navigation, theme management, and UI interactions.
 * Communicates with Flutter AzeooUI via method channels.
 * 
 * Following Android best practices:
 * - Context-aware operations
 * - Coroutines for async operations
 * - StateFlow for reactive state management
 * - Activity/Fragment integration
 */
class AzeooUI private constructor(
    private val client: AzeooClient,
    private val config: SDKConfiguration
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val isInitialized = AtomicBoolean(false)
    
    // Published state using StateFlow
    private val _isScreenVisible = MutableStateFlow(false)
    val isScreenVisible: StateFlow<Boolean> = _isScreenVisible.asStateFlow()
    
    private val _currentTheme = MutableStateFlow("light")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()
    
    private val _safeAreaConfig = MutableStateFlow(config.safeArea)
    val safeAreaConfig: StateFlow<SafeAreaConfiguration> = _safeAreaConfig.asStateFlow()
    
    companion object {
        /**
         * Initialize AzeooUI with client and configuration
         */
        suspend fun create(client: AzeooClient, config: SDKConfiguration): Result<AzeooUI> = withContext(Dispatchers.Main) {
            try {
                if (!client.isInitialized()) {
                    return@withContext Result.failure(SDKException.NotInitialized())
                }
                
                val ui = AzeooUI(client, config)
                
                // Initialize with Flutter
                val success = FlutterEngineManager.invokeFlutterMethod(
                    method = FlutterMethod.UIInitialize,
                    arguments = config.toMap()
                )
                
                if (success) {
                    ui.isInitialized.set(true)
                    
                    // Set initial theme
                    config.theme?.let { theme ->
                        ui._currentTheme.value = if (theme.isDarkMode) "dark" else "light"
                    }
                    
                    Result.success(ui)
                } else {
                    Result.failure(SDKException.ConfigurationFailed("Failed to initialize UI with Flutter"))
                }
            } catch (e: Exception) {
                Result.failure(SDKException.General("UI initialization failed", e))
            }
        }
    }
    
    /**
     * Show main nutrition screen
     */
    suspend fun showMainScreen(context: Context): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            // Launch Flutter activity
            AzeooFlutterActivity.launchMainScreen(context, config)
            
            // Update state
            _isScreenVisible.value = true
            
            // Notify Flutter
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UIShowMainScreen,
                arguments = mapOf("bottomSafeArea" to config.safeArea.bottom)
            )
            
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("showMainScreen", null, "Failed to show main screen"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to show main screen", e))
        }
    }
    
    /**
     * Show permission test screen
     */
    suspend fun showPermissionTestScreen(context: Context): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            // Launch Flutter activity
            AzeooFlutterActivity.launchPermissionTest(context, config)
            
            // Update state
            _isScreenVisible.value = true
            
            // Notify Flutter
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UIShowPermissionTestScreen
            )
            
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("showPermissionTestScreen", null, "Failed to show permission test screen"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to show permission test screen", e))
        }
    }
    
    /**
     * Navigate to specific screen
     */
    suspend fun navigateToScreen(screenName: String, parameters: Map<String, Any>? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val args = mutableMapOf<String, Any>("screenName" to screenName)
            parameters?.let { args["parameters"] = it }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UINavigateToScreen,
                arguments = args
            )
            
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("navigateToScreen", null, "Failed to navigate to screen"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to navigate to screen", e))
        }
    }
    
    /**
     * Hide/dismiss screen
     */
    suspend fun hideScreen(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UIHideScreen
            )
            
            if (success) {
                _isScreenVisible.value = false
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("hideScreen", null, "Failed to hide screen"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to hide screen", e))
        }
    }
    
    /**
     * Change primary color
     */
    suspend fun changePrimaryColor(color: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val colorHex = String.format("#%06X", 0xFFFFFF and color)
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UIChangePrimaryColor,
                arguments = mapOf("color" to colorHex)
            )
            
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("changePrimaryColor", null, "Failed to change primary color"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to change primary color", e))
        }
    }
    
    /**
     * Set theme (light/dark)
     */
    suspend fun setTheme(isDark: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val themeName = if (isDark) "dark" else "light"
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UISetTheme,
                arguments = mapOf("themeName" to themeName)
            )
            
            if (success) {
                _currentTheme.value = themeName
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("setTheme", null, "Failed to set theme"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to set theme", e))
        }
    }
    
    /**
     * Update UI configuration
     */
    suspend fun updateConfiguration(newConfig: SDKConfiguration): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UIUpdateConfiguration,
                arguments = newConfig.toMap()
            )
            
            if (success) {
                _safeAreaConfig.value = newConfig.safeArea
                
                // Update theme if changed
                newConfig.theme?.let { theme ->
                    _currentTheme.value = if (theme.isDarkMode) "dark" else "light"
                }
                
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("updateConfiguration", null, "Failed to update configuration"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to update configuration", e))
        }
    }
    
    /**
     * Get current screen visibility state
     */
    fun getScreenVisibility(): Boolean = _isScreenVisible.value
    
    /**
     * Get current theme
     */
    fun getCurrentTheme(): String = _currentTheme.value
    
    /**
     * Check if UI is initialized
     */
    fun isInitialized(): Boolean = isInitialized.get()
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        isInitialized.set(false)
        _isScreenVisible.value = false
        _currentTheme.value = "light"
    }
}