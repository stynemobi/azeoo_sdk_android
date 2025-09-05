package com.azeoo.sdk.core

import android.content.Context
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.ConcurrentHashMap

/**
 * Core class for managing Flutter engines and method channel communication
 * Equivalent to iOS AzeooCore.swift
 */
class AzeooCore private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: AzeooCore? = null

        val shared: AzeooCore
            get() = INSTANCE ?: synchronized(this) {
                INSTANCE ?: AzeooCore().also { INSTANCE = it }
            }
    }

    // MARK: - Properties
    private var applicationContext: Context? = null
    private var flutterEngine: FlutterEngine? = null
    private var flutterEngineGroup: FlutterEngineGroup? = null
    private var methodChannel: MethodChannel? = null
    
    // Engine ID for cached engine access
    private var mainEngineId: String? = null

    // Multiple instances support
    var enableMultipleInstances: Boolean = false
        private set

    private val engineInstances = ConcurrentHashMap<AzeooModule, FlutterEngine>()
    private val methodChannels = ConcurrentHashMap<AzeooModule, MethodChannel>()
    private val engineIds = ConcurrentHashMap<AzeooModule, String>()

    // Coroutine scope for async operations
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // MARK: - Initialization

    /**
     * Initialize AzeooCore with context and multiple instances configuration
     */
    fun initialize(context: Context, enableMultipleInstances: Boolean = false) {
        this.applicationContext = context.applicationContext
        this.enableMultipleInstances = enableMultipleInstances

        if (enableMultipleInstances) {
            setupMultipleInstances()
        } else {
            setupSingleInstance()
        }

        println("AzeooCore: Initialized in ${if (enableMultipleInstances) "multiple" else "single"} instance mode")
    }

    private fun setupSingleInstance() {
        requireNotNull(applicationContext) { "Application context must be initialized" }

        // Generate unique engine ID for caching
        mainEngineId = "azeoo_main_engine_${System.currentTimeMillis()}"

        // Create single Flutter engine
        flutterEngine = FlutterEngine(applicationContext!!)
        
        // Cache the engine with Flutter's engine cache for fragment access
        io.flutter.embedding.engine.FlutterEngineCache
            .getInstance()
            .put(mainEngineId!!, flutterEngine!!)
        
        // Start the Flutter engine before accessing dartExecutor
        flutterEngine!!.dartExecutor.executeDartEntrypoint(
            io.flutter.embedding.engine.dart.DartExecutor.DartEntrypoint.createDefault()
        )

        // Initialize method channel after engine is started
        methodChannel = MethodChannel(
            flutterEngine!!.dartExecutor.binaryMessenger,
            "com.azeoo.sdk"
        )

        println("AzeooCore: Single Flutter engine created and started with ID: $mainEngineId")
    }

    private fun setupMultipleInstances() {
        requireNotNull(applicationContext) { "Application context must be initialized" }

        // Create Flutter engine group for better performance
        flutterEngineGroup = FlutterEngineGroup(applicationContext!!)

        println("AzeooCore: Flutter engine group created for multiple instances")
    }

    // MARK: - Engine Management

    /**
     * Get the main Flutter engine (single instance mode)
     */
    fun getFlutterEngine(): FlutterEngine? {
        return flutterEngine
    }

    /**
     * Get or create a Flutter engine for specific module (multiple instances mode)
     * In single instance mode, returns the main shared engine
     */
    fun getOrCreateEngineForModule(module: AzeooModule): FlutterEngine {
        return if (enableMultipleInstances) {
            engineInstances.getOrPut(module) {
                requireNotNull(flutterEngineGroup) { "Flutter engine group not initialized" }
                requireNotNull(applicationContext) { "Application context not initialized" }

                val engine = flutterEngineGroup!!.createAndRunDefaultEngine(applicationContext!!)
                
                // Generate unique engine ID for this module
                val engineId = "azeoo_${module.displayName}_engine_${System.currentTimeMillis()}"
                engineIds[module] = engineId
                
                // Cache the engine with Flutter's engine cache for fragment access
                io.flutter.embedding.engine.FlutterEngineCache
                    .getInstance()
                    .put(engineId, engine)

                // Create method channel for this module after engine is started
                methodChannels[module] = MethodChannel(
                    engine.dartExecutor.binaryMessenger,
                    "com.azeoo.sdk"
                )

                println("AzeooCore: Created Flutter engine for module: ${module.displayName} with ID: $engineId")
                engine
            }
        } else {
            // In single instance mode, all modules share the main engine
            requireNotNull(flutterEngine) { "Flutter engine not initialized in single instance mode" }
            flutterEngine!!
        }
    }

    /**
     * Get method channel for Flutter communication
     */
    fun getMethodChannel(module: AzeooModule? = null): MethodChannel? {
        return if (enableMultipleInstances && module != null) {
            methodChannels[module]
        } else {
            methodChannel
        }
    }
    
    /**
     * Get engine ID for FlutterFragment creation
     * In single instance mode, returns the main engine ID
     * In multiple instances mode, returns the module-specific engine ID
     */
    fun getEngineIdForModule(module: AzeooModule? = null): String? {
        return if (enableMultipleInstances && module != null) {
            // Ensure engine is created first
            getOrCreateEngineForModule(module)
            engineIds[module]
        } else {
            mainEngineId
        }
    }

    // MARK: - Method Channel Communication

    /**
     * Invoke Flutter method with arguments and callback
     */
    fun invokeFlutterMethod(
        method: String,
        arguments: Map<String, Any>?,
        module: AzeooModule? = null,
        callback: (Result<Any?>) -> Unit
    ) {
        val channel = getMethodChannel(module)

        if (channel == null) {
            callback(Result.failure(IllegalStateException("Method channel not initialized")))
            return
        }

        try {
            channel.invokeMethod(method, arguments, object : MethodChannel.Result {
                override fun success(result: Any?) {
                    callback(Result.success(result))
                }

                override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                    val exception = Exception("Flutter error [$errorCode]: $errorMessage")
                    callback(Result.failure(exception))
                }

                override fun notImplemented() {
                    val exception = Exception("Method $method not implemented")
                    callback(Result.failure(exception))
                }
            })
        } catch (e: Exception) {
            callback(Result.failure(e))
        }
    }

    // MARK: - Lifecycle Management

    /**
     * Dispose of Flutter engines and clean up resources
     */
    fun dispose() {
        val engineCache = io.flutter.embedding.engine.FlutterEngineCache.getInstance()
        
        // Dispose individual module engines and remove from cache
        engineInstances.values.forEach { engine ->
            try {
                engine.destroy()
            } catch (e: Exception) {
                println("AzeooCore: Error disposing engine: ${e.message}")
            }
        }
        
        // Remove module engine IDs from cache
        engineIds.values.forEach { engineId ->
            try {
                engineCache.remove(engineId)
            } catch (e: Exception) {
                println("AzeooCore: Error removing engine from cache: ${e.message}")
            }
        }
        
        engineInstances.clear()
        methodChannels.clear()
        engineIds.clear()

        // Dispose main engine and remove from cache
        flutterEngine?.destroy()
        flutterEngine = null
        methodChannel = null
        
        mainEngineId?.let { engineId ->
            try {
                engineCache.remove(engineId)
            } catch (e: Exception) {
                println("AzeooCore: Error removing main engine from cache: ${e.message}")
            }
        }
        mainEngineId = null

        println("AzeooCore: Disposed all Flutter engines and cleared cache")
    }

    /**
     * Get initialization state
     */
    fun isInitialized(): Boolean {
        return if (enableMultipleInstances) {
            flutterEngineGroup != null
        } else {
            flutterEngine != null && methodChannel != null
        }
    }
}

/**
 * Enum for module types - matches iOS AzeooModule
 */
enum class AzeooModule(val displayName: String) {
    NUTRITION("nutrition"),
    TRAINING("training"),
    SHARED("shared");

    companion object {
        fun fromString(value: String): AzeooModule? {
            return values().find { it.displayName == value }
        }
    }
}
