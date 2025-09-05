package com.azeoo.sdk.ui

import androidx.fragment.app.Fragment
import com.azeoo.sdk.client.AzeooClient
import com.azeoo.sdk.config.Config
import com.azeoo.sdk.config.SafeAreaConfig
import com.azeoo.sdk.core.AzeooCore
import com.azeoo.sdk.core.FlutterCommandExecutor
import com.azeoo.sdk.core.FlutterMethod
import com.azeoo.sdk.core.AzeooModule
import com.azeoo.sdk.ui.modules.NutritionModule
import com.azeoo.sdk.ui.modules.TrainingModule
import io.flutter.embedding.android.FlutterFragment
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * AzeooUI class for managing UI-related functionality
 * Equivalent to iOS AzeooUI.swift
 */
class AzeooUI private constructor(
    private val client: AzeooClient,
    private val config: Config
) {

    private val executor = FlutterCommandExecutor()

    // Properties matching iOS implementation
    var isScreenVisible: Boolean = false
        private set

    var currentTheme: String = "light"
        private set

    var safeAreaConfig: SafeAreaConfig = config.safeArea
        private set

    var isInitialized: Boolean = false
        private set

    // Queue for pending operations
    private val pendingOperations = mutableListOf<() -> Unit>()

    // Module properties
    val nutrition: NutritionModule
    val training: TrainingModule

    init {
        // Initialize modules without parent reference first
        // Create module-specific executors for multiple instances support
        val nutritionExecutor = FlutterCommandExecutor(AzeooModule.NUTRITION)
        val trainingExecutor = FlutterCommandExecutor(AzeooModule.TRAINING)

        nutrition = NutritionModule(client, config, nutritionExecutor)
        training = TrainingModule(client, config, trainingExecutor)

        // Set parent reference after initialization is complete
        nutrition.setParentUI(this)
        training.setParentUI(this)
    }

    /**
     * Initialize AzeooUI with client and configuration
     */
    constructor(
        client: AzeooClient,
        config: Config,
        callback: (Result<Unit>) -> Unit
    ) : this(client, config) {
        // Initialize asynchronously
        initialize(config, callback)
    }

    /**
     * Create using coroutines
     */
    companion object {
        suspend fun createAsync(client: AzeooClient, config: Config): AzeooUI =
            suspendCancellableCoroutine { continuation ->
                AzeooUI(client, config) { result ->
                    when {
                        result.isSuccess -> {
                            val ui = AzeooUI(client, config) { }  // Silent initialization
                            continuation.resume(ui)
                        }
                        result.isFailure -> continuation.resumeWithException(
                            result.exceptionOrNull() ?: Exception("UI initialization failed")
                        )
                    }
                }
            }

        /**
         * Convenience method for immediate use (initialization happens in background)
         */
        fun createSilent(client: AzeooClient, config: Config): AzeooUI {
            return AzeooUI(client, config) { }  // Silent initialization
        }
    }

    private fun initialize(config: Config, callback: (Result<Unit>) -> Unit) {
        // Check if we're in multiple instances mode and engines are already initialized
        if (AzeooCore.shared.enableMultipleInstances) {
            println("AzeooUI: Multiple instances mode detected, skipping Flutter initialization (already done)")
            isInitialized = true
            processPendingOperations()
            callback(Result.success(Unit))
            return
        }

        // Single instance mode: perform Flutter initialization
        executor.executeOnMainQueue(
            method = FlutterMethod.UI_INITIALIZE,
            arguments = config.toDictionary()
        ) { result ->
            when {
                result.isSuccess -> {
                    isInitialized = true
                    println("✅ AzeooUI initialized successfully")
                    processPendingOperations()
                    callback(Result.success(Unit))
                }
                result.isFailure -> {
                    val error = result.exceptionOrNull() ?: Exception("UI initialization failed")
                    println("❌ AzeooUI initialization failed: ${error.message}")
                    callback(Result.failure(error))
                }
            }
        }
    }

    /**
     * Process any operations that were queued during initialization
     */
    private fun processPendingOperations() {
        val operations = pendingOperations.toList()
        pendingOperations.clear()

        for (operation in operations) {
            operation()
        }
    }

    /**
     * Queue an operation to execute when initialization is complete
     */
    internal fun queueOperation(operation: () -> Unit) {
        if (isInitialized) {
            operation()
        } else {
            pendingOperations.add(operation)
        }
    }

    // MARK: - Screen Navigation Methods

    /**
     * Show the permission test screen
     */
    fun showPermissionTestScreen(callback: (Result<Unit>) -> Unit) {
        val arguments = mapOf("screen" to "permission_test")

        executor.executeOnMainQueue(
            method = FlutterMethod.UI_SHOW_SCREEN,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    isScreenVisible = true
                    callback(Result.success(Unit))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Failed to show permission test screen")))
                }
            }
        }
    }

    /**
     * Show analytics screen
     */
    fun showAnalyticsScreen(callback: (Result<Unit>) -> Unit) {
        val arguments = mapOf("screen" to "analytics")

        executor.executeOnMainQueue(
            method = FlutterMethod.UI_SHOW_SCREEN,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    isScreenVisible = true
                    callback(Result.success(Unit))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Failed to show analytics screen")))
                }
            }
        }
    }

    /**
     * Show settings screen
     */
    fun showSettingsScreen(callback: (Result<Unit>) -> Unit) {
        val arguments = mapOf("screen" to "settings")

        executor.executeOnMainQueue(
            method = FlutterMethod.UI_SHOW_SCREEN,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    isScreenVisible = true
                    callback(Result.success(Unit))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Failed to show settings screen")))
                }
            }
        }
    }

    // MARK: - Theme Management

    /**
     * Update theme configuration
     */
    fun updateTheme(theme: String, callback: (Result<Unit>) -> Unit) {
        val arguments = mapOf("theme" to theme)

        executor.executeOnMainQueue(
            method = FlutterMethod.UI_UPDATE_THEME,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    currentTheme = theme
                    callback(Result.success(Unit))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Theme update failed")))
                }
            }
        }
    }

    /**
     * Update theme using coroutines
     */
    suspend fun updateThemeAsync(theme: String) = suspendCancellableCoroutine { continuation ->
        updateTheme(theme) { result ->
            when {
                result.isSuccess -> continuation.resume(Unit)
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Theme update failed")
                )
            }
        }
    }

    // MARK: - Flutter Fragment Management

    /**
     * Get default Flutter fragment (single instance mode)
     */
    fun getDefaultFlutterFragment(): FlutterFragment? {
        val engineId = AzeooCore.shared.getEngineIdForModule(null)
        return engineId?.let { createFlutterFragmentWithCachedEngine(it) }
    }

    /**
     * Get new Flutter fragment with specific name
     */
    fun getNewFlutterFragment(name: String): FlutterFragment? {
        val module = AzeooModule.fromString(name) ?: AzeooModule.SHARED
        val engineId = AzeooCore.shared.getEngineIdForModule(module)
        return engineId?.let { createFlutterFragmentWithCachedEngine(it) }
    }

    /**
     * Create Flutter fragment using cached engine
     */
    private fun createFlutterFragmentWithCachedEngine(engineId: String): FlutterFragment {
        println("AzeooUI: Creating FlutterFragment with cached engine ID: $engineId")
        return FlutterFragment.withCachedEngine(engineId)
            .build<FlutterFragment>()
    }

    // MARK: - State Management

    /**
     * Check if the UI is properly initialized
     */
    val isUIInitialized: Boolean
        get() = isInitialized

    /**
     * Get current UI state
     */
    fun getCurrentState(): Map<String, Any> {
        return mapOf(
            "isScreenVisible" to isScreenVisible,
            "currentTheme" to currentTheme,
            "safeArea" to safeAreaConfig.toDictionary(),
            "clientConfig" to client.getConfiguration(),
            "isInitialized" to isInitialized,
            "nutrition" to nutrition.getState(),
            "training" to training.getState()
        )
    }

    /**
     * Reset UI state
     */
    fun resetState() {
        isScreenVisible = false
        currentTheme = "light"
        safeAreaConfig = config.safeArea
        isInitialized = false
        pendingOperations.clear()
        nutrition.resetState()
        training.resetState()
    }

    /**
     * Get UI state from Flutter
     */
    fun getState(callback: (Result<Map<String, Any>>) -> Unit) {
        executor.executeOnMainQueue(
            method = FlutterMethod.UI_GET_STATE,
            arguments = null
        ) { result ->
            when {
                result.isSuccess -> {
                    @Suppress("UNCHECKED_CAST")
                    val state = result.getOrNull() as? Map<String, Any> ?: emptyMap()
                    callback(Result.success(state))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Failed to get UI state")))
                }
            }
        }
    }
}

/**
 * UI Error types - equivalent to iOS AzeooUIError
 */
sealed class AzeooUIError : Exception() {
    data class InitializationFailed(override val cause: Throwable) : AzeooUIError() {
        override val message: String = "UI initialization failed: ${cause.message}"
    }

    object ScreenNotAvailable : AzeooUIError() {
        override val message: String = "Screen is not available"
    }

    data class NavigationFailed(val screen: String) : AzeooUIError() {
        override val message: String = "Navigation to $screen failed"
    }

    object ThemeUpdateFailed : AzeooUIError() {
        override val message: String = "Failed to update theme"
    }

    object ConfigurationError : AzeooUIError() {
        override val message: String = "Configuration error occurred"
    }
}
