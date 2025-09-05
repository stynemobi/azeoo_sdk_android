package com.azeoo.sdk.client

import android.content.Context
import com.azeoo.sdk.core.AzeooCore
import com.azeoo.sdk.core.FlutterCommandExecutor
import com.azeoo.sdk.core.FlutterMethod
import com.azeoo.sdk.core.FlutterRequestBuilder
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Main client class for the Azeoo SDK
 * Equivalent to iOS AzeooClient.swift
 */
class AzeooClient private constructor(val apiKey: String) {
    
    private val executor = FlutterCommandExecutor()
    
    // Properties matching iOS implementation
    var subscriptions: List<String> = emptyList()
        private set
    
    var isAuthenticated: Boolean = false
        private set
    
    companion object {
        /**
         * Initialize the AzeooClient with an API key
         * Calls Flutter AzeooClient.initialize() via method channel
         */
        fun initialize(
            context: Context,
            apiKey: String,
            enableMultipleInstances: Boolean = false,
            callback: (Result<AzeooClient>) -> Unit
        ) {
            // Initialize AzeooCore with the specified configuration
            AzeooCore.shared.initialize(context, enableMultipleInstances)
            
            val client = AzeooClient(apiKey)
            
            // Build arguments for Flutter method call
            val arguments = FlutterRequestBuilder()
                .apiKey(apiKey)
                .build()
            
            // Execute initialization command
            client.executor.executeOnMainQueue(
                method = FlutterMethod.CLIENT_INITIALIZE,
                arguments = arguments
            ) { result ->
                when {
                    result.isSuccess -> {
                        client.isAuthenticated = true
                        println("✅ AzeooClient initialized successfully")
                        callback(Result.success(client))
                    }
                    result.isFailure -> {
                        val error = result.exceptionOrNull() ?: Exception("Unknown initialization error")
                        println("❌ AzeooClient initialization failed: ${error.message}")
                        callback(Result.failure(error))
                    }
                }
            }
        }
        
        /**
         * Initialize synchronously using coroutines
         */
        suspend fun initializeAsync(
            context: Context,
            apiKey: String,
            enableMultipleInstances: Boolean = false
        ): AzeooClient = suspendCancellableCoroutine { continuation ->
            initialize(context, apiKey, enableMultipleInstances) { result ->
                when {
                    result.isSuccess -> continuation.resume(result.getOrThrow())
                    result.isFailure -> continuation.resumeWithException(
                        result.exceptionOrNull() ?: Exception("Unknown error")
                    )
                }
            }
        }
    }
    
    /**
     * Validate the API key with the server
     */
    fun validateApiKey(callback: (Result<Boolean>) -> Unit) {
        val arguments = FlutterRequestBuilder()
            .apiKey(apiKey)
            .build()
        
        executor.executeOnMainQueue(
            method = FlutterMethod.CLIENT_VALIDATE_API_KEY,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    val isValid = result.getOrNull() as? Boolean ?: false
                    callback(Result.success(isValid))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Validation failed")))
                }
            }
        }
    }
    
    /**
     * Validate API key using coroutines
     */
    suspend fun validateApiKeyAsync(): Boolean = suspendCancellableCoroutine { continuation ->
        validateApiKey { result ->
            when {
                result.isSuccess -> continuation.resume(result.getOrThrow())
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Validation failed")
                )
            }
        }
    }
    
    /**
     * Get user subscriptions
     */
    fun getSubscriptions(callback: (Result<List<String>>) -> Unit) {
        val arguments = FlutterRequestBuilder()
            .apiKey(apiKey)
            .build()
        
        executor.executeOnMainQueue(
            method = FlutterMethod.CLIENT_GET_SUBSCRIPTIONS,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    @Suppress("UNCHECKED_CAST")
                    val subs = result.getOrNull() as? List<String> ?: emptyList()
                    subscriptions = subs
                    callback(Result.success(subs))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Failed to get subscriptions")))
                }
            }
        }
    }
    
    /**
     * Get subscriptions using coroutines
     */
    suspend fun getSubscriptionsAsync(): List<String> = suspendCancellableCoroutine { continuation ->
        getSubscriptions { result ->
            when {
                result.isSuccess -> continuation.resume(result.getOrThrow())
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Failed to get subscriptions")
                )
            }
        }
    }
    
    /**
     * Update client configuration
     */
    fun updateConfiguration(config: Map<String, Any>, callback: (Result<Unit>) -> Unit) {
        val arguments = FlutterRequestBuilder()
            .apiKey(apiKey)
            .config(config)
            .build()
        
        executor.executeOnMainQueue(
            method = FlutterMethod.CLIENT_UPDATE_CONFIGURATION,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> callback(Result.success(Unit))
                result.isFailure -> callback(Result.failure(
                    result.exceptionOrNull() ?: Exception("Configuration update failed")
                ))
            }
        }
    }
    
    /**
     * Update configuration using coroutines
     */
    suspend fun updateConfigurationAsync(config: Map<String, Any>) = suspendCancellableCoroutine { continuation ->
        updateConfiguration(config) { result ->
            when {
                result.isSuccess -> continuation.resume(Unit)
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Configuration update failed")
                )
            }
        }
    }
    
    /**
     * Get current client state
     */
    fun getState(callback: (Result<Map<String, Any>>) -> Unit) {
        val arguments = FlutterRequestBuilder()
            .apiKey(apiKey)
            .build()
        
        executor.executeOnMainQueue(
            method = FlutterMethod.CLIENT_GET_STATE,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    @Suppress("UNCHECKED_CAST")
                    val state = result.getOrNull() as? Map<String, Any> ?: emptyMap()
                    callback(Result.success(state))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Failed to get state")))
                }
            }
        }
    }
    
    /**
     * Get state using coroutines
     */
    suspend fun getStateAsync(): Map<String, Any> = suspendCancellableCoroutine { continuation ->
        getState { result ->
            when {
                result.isSuccess -> continuation.resume(result.getOrThrow())
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Failed to get state")
                )
            }
        }
    }
    
    /**
     * Get current configuration
     */
    fun getConfiguration(): Map<String, Any> {
        return mapOf(
            "apiKey" to apiKey,
            "isAuthenticated" to isAuthenticated,
            "subscriptions" to subscriptions
        )
    }
    
    /**
     * Check if client is properly initialized
     */
    fun isClientInitialized(): Boolean {
        return isAuthenticated && AzeooCore.shared.isInitialized()
    }
}
