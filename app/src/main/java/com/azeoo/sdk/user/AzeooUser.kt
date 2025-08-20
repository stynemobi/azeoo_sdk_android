package com.azeoo.sdk.user

import com.azeoo.sdk.client.AzeooClient
import com.azeoo.sdk.core.FlutterEngineManager
import com.azeoo.sdk.core.FlutterMethod
import com.azeoo.sdk.data.exception.SDKException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AzeooUser class for managing user-specific functionality
 * 
 * Handles user authentication, profile management, and user data operations.
 * Communicates with Flutter AzeooUser via method channels.
 * 
 * Following Android best practices:
 * - Coroutines for async operations
 * - StateFlow for reactive state management
 * - Lifecycle awareness
 * - Data classes for immutable state
 */
class AzeooUser private constructor(
    private val client: AzeooClient,
    val userId: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val isInitialized = AtomicBoolean(false)
    
    // Published state using StateFlow
    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _lastSync = MutableStateFlow<Date?>(null)
    val lastSync: StateFlow<Date?> = _lastSync.asStateFlow()
    
    companion object {
        /**
         * Initialize AzeooUser with client and user ID
         */
        suspend fun create(client: AzeooClient, userId: String): Result<AzeooUser> = withContext(Dispatchers.Main) {
            try {
                if (userId.isBlank()) {
                    return@withContext Result.failure(SDKException.InvalidConfiguration("userId", "User ID cannot be blank"))
                }
                
                if (!client.isInitialized()) {
                    return@withContext Result.failure(SDKException.NotInitialized())
                }
                
                val user = AzeooUser(client, userId)
                
                // Initialize with Flutter
                val success = FlutterEngineManager.invokeFlutterMethod(
                    method = FlutterMethod.UserInitialize,
                    arguments = mapOf("userId" to userId)
                )
                
                if (success) {
                    user.isInitialized.set(true)
                    Result.success(user)
                } else {
                    Result.failure(SDKException.ConfigurationFailed("Failed to initialize user with Flutter"))
                }
            } catch (e: Exception) {
                Result.failure(SDKException.General("User initialization failed", e))
            }
        }
    }
    
    /**
     * Get authentication token
     */
    suspend fun getToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UserGetToken,
                arguments = mapOf("userId" to userId)
            )
            
            if (success) {
                // In real implementation, get actual token from method channel response
                Result.success("mock_token_${userId}")
            } else {
                Result.failure(SDKException.MethodChannelError("getToken", null, "Failed to get token"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to get token", e))
        }
    }
    
    /**
     * Logout user
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UserLogout,
                arguments = mapOf("userId" to userId)
            )
            
            if (success) {
                _isLoggedIn.value = false
                _profile.value = null
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("logout", null, "Failed to logout"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to logout", e))
        }
    }
    
    /**
     * Get user profile
     */
    suspend fun getProfile(): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UserGetProfile,
                arguments = mapOf("userId" to userId)
            )
            
            if (success) {
                // In real implementation, parse actual profile from method channel response
                val mockProfile = UserProfile(
                    id = userId,
                    email = "user@example.com",
                    name = "Mock User",
                    height = 175.0,
                    weight = 70.0,
                    age = 25,
                    phone = "+1234567890"
                )
                _profile.value = mockProfile
                Result.success(mockProfile)
            } else {
                Result.failure(SDKException.MethodChannelError("getProfile", null, "Failed to get profile"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to get profile", e))
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UserUpdate,
                arguments = mapOf(
                    "userId" to userId,
                    "profile" to profile.toMap()
                )
            )
            
            if (success) {
                _profile.value = profile
                _lastSync.value = Date()
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("update", null, "Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to update profile", e))
        }
    }
    
    /**
     * Delete user account
     */
    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UserDelete,
                arguments = mapOf("userId" to userId)
            )
            
            if (success) {
                _isLoggedIn.value = false
                _profile.value = null
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("delete", null, "Failed to delete account"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to delete account", e))
        }
    }
    
    /**
     * Change user height
     */
    suspend fun changeHeight(height: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UserChangeHeight,
                arguments = mapOf(
                    "userId" to userId,
                    "height" to height
                )
            )
            
            if (success) {
                _profile.value = _profile.value?.copy(height = height)
                _lastSync.value = Date()
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("changeHeight", null, "Failed to change height"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to change height", e))
        }
    }
    
    /**
     * Change user weight
     */
    suspend fun changeWeight(weight: Double): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!isInitialized.get()) {
                return@withContext Result.failure(SDKException.NotInitialized())
            }
            
            val success = FlutterEngineManager.invokeFlutterMethod(
                method = FlutterMethod.UserChangeWeight,
                arguments = mapOf(
                    "userId" to userId,
                    "weight" to weight
                )
            )
            
            if (success) {
                _profile.value = _profile.value?.copy(weight = weight)
                _lastSync.value = Date()
                Result.success(Unit)
            } else {
                Result.failure(SDKException.MethodChannelError("changeWeight", null, "Failed to change weight"))
            }
        } catch (e: Exception) {
            Result.failure(SDKException.General("Failed to change weight", e))
        }
    }
    
    /**
     * Get user email
     */
    fun getEmail(): String? = _profile.value?.email
    
    /**
     * Get user phone
     */
    fun getPhone(): String? = _profile.value?.phone
    
    /**
     * Check if user is initialized
     */
    fun isInitialized(): Boolean = isInitialized.get()
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        scope.cancel()
        isInitialized.set(false)
        _profile.value = null
        _isLoggedIn.value = false
        _lastSync.value = null
    }
}

/**
 * User profile data class
 */
data class UserProfile(
    val id: String,
    val email: String?,
    val name: String?,
    val height: Double? = null,
    val weight: Double? = null,
    val age: Int? = null,
    val phone: String? = null,
    val address: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "email" to email,
        "name" to name,
        "height" to height,
        "weight" to weight,
        "age" to age,
        "phone" to phone,
        "address" to address
    )
}