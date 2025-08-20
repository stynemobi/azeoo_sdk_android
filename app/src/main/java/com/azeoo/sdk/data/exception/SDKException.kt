package com.azeoo.sdk.data.exception

/**
 * Sealed class hierarchy for SDK exceptions
 * 
 * Provides structured error handling for different SDK failure scenarios:
 * - Initialization errors
 * - Configuration errors
 * - Flutter engine errors
 * - Validation errors
 * 
 * This follows Android and Kotlin best practices for exception handling.
 */
sealed class SDKException(message: String) : Exception(message) {
    
    /**
     * Thrown when SDK methods are called before initialization
     */
    class NotInitialized : SDKException("SDK not initialized. Call AzeooSDK.initialize() first")
    
    /**
     * Thrown when SDK methods are called before configuration
     */
    class NotConfigured : SDKException("SDK not configured. Call AzeooSDK.configure() first")
    
    /**
     * Thrown when Flutter configuration fails
     */
    class ConfigurationFailed(reason: String? = null) : SDKException(
        "Failed to configure SDK with Flutter module${reason?.let { ": $it" } ?: ""}"
    )
    
    /**
     * Thrown when configuration validation fails
     */
    class InvalidConfiguration(field: String, reason: String? = null) : SDKException(
        "Invalid configuration: $field${reason?.let { " - $it" } ?: ""}"
    )
    
    /**
     * Thrown when Flutter engine operations fail
     */
    class FlutterEngineError(message: String, cause: Throwable? = null) : SDKException(
        "Flutter engine error: $message"
    ) {
        init {
            if (cause != null) initCause(cause)
        }
    }
    
    /**
     * Thrown when Flutter method calls fail
     */
    class MethodChannelError(method: String, errorCode: String?, errorMessage: String?) : 
        SDKException("Method channel error in '$method': ${errorCode ?: "unknown"} - ${errorMessage ?: "no details"}")
    
    /**
     * Thrown when required permissions are missing
     */
    class MissingPermission(permission: String) : SDKException("Missing required permission: $permission")
    
    /**
     * Thrown when network operations fail
     */
    class NetworkError(message: String, cause: Throwable? = null) : SDKException(
        "Network error: $message"
    ) {
        init {
            if (cause != null) initCause(cause)
        }
    }
    
    /**
     * General SDK error for unexpected situations
     */
    class General(message: String, cause: Throwable? = null) : SDKException(message) {
        init {
            if (cause != null) initCause(cause)
        }
    }
}