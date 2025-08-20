package com.azeoo.sdk.client

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.azeoo.sdk.core.FlutterEngineManager
import com.azeoo.sdk.core.FlutterMethod
import com.azeoo.sdk.data.exception.SDKException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Main client class for the Azeoo SDK
 * 
 * Handles API authentication, subscription management, and client state.
 * Communicates with Flutter AzeooClient via method channels.
 * 
 * Following Android best practices:
 * - Coroutines for async operations
 * - StateFlow for reactive state management
 * - Lifecycle awareness
 * - Thread-safe operations
 */
class AzeooClient private constructor(
    val apiKey: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val isInitialized = AtomicBoolean(false)
    
    // Published state using StateFlow
    private val _subscriptions = MutableStateFlow<List<String>>(emptyList())
    val subscriptions: StateFlow<List<String>> = _subscriptions.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    
    private val _clientState = MutableStateFlow<ClientState>(ClientState.NotInitialized)
    val clientState: StateFlow<ClientState> = _clientState.asStateFlow()
    
    companion object {
        /**
         * Initialize the AzeooClient with an API key
         * Calls Flutter AzeooClient.initialize() via method channel
         */
        suspend fun initialize(apiKey: String): Result<AzeooClient> = withContext(Dispatchers.Main) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(SDKException.InvalidConfiguration("apiKey", "API key cannot be blank"))
                }
                
                val client = AzeooClient(apiKey)
                
                // Initialize with Flutter
                val success = FlutterEngineManager.invokeFlutterMethod(
                    method = FlutterMethod.ClientInitialize,
                    arguments = mapOf("apiKey" to apiKey)
                )
                
                if (success) {
                    client.isInitialized.set(true)
                    client._clientState.value = ClientState.Initialized
                    Result.success(client)
                } else {
                    Result.failure(SDKException.ConfigurationFailed("Failed to initialize client with Flutter"))
                }
            } catch (e: Exception) {
                Result.failure(SDKException.General("Client initialization failed", e))
            }
        }
    }
    
    /**
     * Get user subscriptions
     */
    suspend fun getSubscriptions(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.ClientGetSubscriptions
            )
            
            if (success) {
                // In a real implementation, we'd get the actual data from the method channel
                // For now, return success and update state
                val mockSubscriptions = listOf("premium", "basic")
                _subscriptions.value = mockSubscriptions
                Result.success(mockSubscriptions)
            } else {
                Result.failure(SDKException.MethodChannelError("getSubscriptions", null, "Failed to get subscriptions"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to get subscriptions", e))
        }
    }
    
    /**
     * Validate API key
     */
    suspend fun validateApiKey(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.ClientValidateApiKey,
                arguments = mapOf("apiKey" to apiKey)
            )
            
            if (success) {
                _isAuthenticated.value = true
                _clientState.value = ClientState.Authenticated
                Result.success(true)
            } else {
                _isAuthenticated.value = false
                _clientState.value = ClientState.Error("Invalid API key")
                Result.failure(SDKException.General("API key validation failed"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to validate API key", e))
        }
    }
    
    /**
     * Update configuration
     */
    suspend fun updateConfiguration(config: Map<String, Any>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.ClientUpdateConfiguration,
                arguments = config
            )
            
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(SDKException.ConfigurationFailed("Failed to update configuration"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to update configuration", e))
        }
    }
    
    /**
     * Get current client state
     */
    fun getCurrentState(): ClientState = _clientState.value
    
    /**
     * Check if client is initialized
     */
    fun isInitialized(): Boolean = isInitialized.get()
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        isInitialized.set(false)
        _clientState.value = ClientState.NotInitialized
        _isAuthenticated.value = false
        _subscriptions.value = emptyList()
    }
}

/**
 * Client state enum
 */
sealed class ClientState {
    object NotInitialized : ClientState()
    object Initialized : ClientState()
    object Authenticated : ClientState()
    data class Error(val message: String) : ClientState()
}