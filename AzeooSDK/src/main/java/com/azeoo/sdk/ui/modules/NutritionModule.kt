package com.azeoo.sdk.ui.modules

import androidx.fragment.app.Fragment
import com.azeoo.sdk.client.AzeooClient
import com.azeoo.sdk.config.Config
import com.azeoo.sdk.core.AzeooCore
import com.azeoo.sdk.core.AzeooModule
import com.azeoo.sdk.core.FlutterCommandExecutor
import com.azeoo.sdk.core.FlutterMethod
import io.flutter.embedding.android.FlutterFragment
import io.flutter.embedding.engine.FlutterEngine
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Nutrition module for managing nutrition-related functionality
 * Equivalent to iOS NutritionModule.swift
 */
class NutritionModule internal constructor(
    private val client: AzeooClient,
    private val config: Config,
    private val executor: FlutterCommandExecutor
) {

    // Properties
    var isDisplayed: Boolean = false
        private set

    private var parentUI: Any? = null  // Weak reference to parent UI

    /**
     * Set parent UI reference (called after parent is fully initialized)
     */
    internal fun setParentUI(parentUI: Any) {
        this.parentUI = parentUI
    }

    /**
     * Display the nutrition screen
     */
    fun display(callback: (Result<Unit>) -> Unit) {
        // Check if parent UI is initialized
        val parent = parentUI
        if (parent != null) {
            // Check if parent UI is ready (this would be implemented based on parent type)
            // For now, proceed with display
            performDisplay(callback)
        } else {
            // Queue operation until parent is ready
            queueOperation { display(callback) }
        }
    }

    /**
     * Display using coroutines
     */
    suspend fun displayAsync() = suspendCancellableCoroutine { continuation ->
        display { result ->
            when {
                result.isSuccess -> continuation.resume(Unit)
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Failed to display nutrition module")
                )
            }
        }
    }

    private fun performDisplay(callback: (Result<Unit>) -> Unit) {
        println("🍎 Displaying nutrition module...")

        executor.executeOnMainQueue(
            method = FlutterMethod.NUTRITION_DISPLAY,
            arguments = null
        ) { result ->
            when {
                result.isSuccess -> {
                    isDisplayed = true
                    println("✅ Nutrition module displayed successfully")
                    callback(Result.success(Unit))
                }

                result.isFailure -> {
                    val error = result.exceptionOrNull() ?: Exception("Failed to display nutrition")
                    println("❌ Failed to display nutrition module: ${error.message}")
                    callback(Result.failure(error))
                }
            }
        }
    }

    /**
     * Get Fragment for embedding in Android views
     */
    fun getFragment(): Fragment {
        val engineId = AzeooCore.shared.getEngineIdForModule(AzeooModule.NUTRITION)
        return createFlutterFragmentWithCachedEngine(engineId!!)
    }

    /**
     * Get Flutter engine for this module
     */
    fun getFlutterEngine(): FlutterEngine {
        return AzeooCore.shared.getOrCreateEngineForModule(AzeooModule.NUTRITION)
    }

    /**
     * Create Flutter fragment with the module's cached engine
     */
    private fun createFlutterFragmentWithCachedEngine(engineId: String): FlutterFragment {
        println("NutritionModule: Creating FlutterFragment with cached engine ID: $engineId")
        return FlutterFragment.withCachedEngine(engineId)
            .build<FlutterFragment>()
    }

    /**
     * Hide the nutrition screen
     */
    fun hide(callback: (Result<Unit>) -> Unit) {
        isDisplayed = false
        println("🍎 Nutrition module hidden")
        callback(Result.success(Unit))
    }

    /**
     * Hide using coroutines
     */
    suspend fun hideAsync() = suspendCancellableCoroutine { continuation ->
        hide { result ->
            when {
                result.isSuccess -> continuation.resume(Unit)
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Failed to hide nutrition module")
                )
            }
        }
    }

    /**
     * Reset module state
     */
    fun resetState() {
        isDisplayed = false
        println("🍎 Nutrition module state reset")
    }

    /**
     * Get current module state
     */
    fun getState(): Map<String, Any> {
        return mapOf(
            "module" to "nutrition",
            "isDisplayed" to isDisplayed,
            "engineId" to "nutrition_engine"
        )
    }

    /**
     * Queue operation until parent UI is ready
     * This is a placeholder - the actual implementation would depend on parent UI type
     */
    private fun queueOperation(operation: () -> Unit) {
        // For now, execute immediately
        // In a real implementation, this would queue operations until parent is ready
        operation()
    }
}
