package com.azeoo.sdk.user

import com.azeoo.sdk.client.AzeooClient
import com.azeoo.sdk.core.AzeooCore
import com.azeoo.sdk.core.FlutterCommandExecutor
import com.azeoo.sdk.core.FlutterMethod
import com.azeoo.sdk.core.FlutterRequestBuilder
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * User profile data class
 */
data class UserProfile(
    val id: String,
    val email: String? = null,
    val name: String? = null,
    val avatarUrl: String? = null,
    val preferences: Map<String, Any> = emptyMap()
)

/**
 * AzeooUser class for managing user-specific functionality
 * Equivalent to iOS AzeooUser.swift
 */
class AzeooUser private constructor(
    private val client: AzeooClient,
    val userId: String
) {

    private val executor = FlutterCommandExecutor()

    // Properties matching iOS implementation
    var profile: UserProfile? = null
        private set

    var isLoggedIn: Boolean = false
        private set

    var lastSync: Date? = null
        private set

    /**
     * Initialize AzeooUser with client and user ID
     */
    constructor(
        client: AzeooClient,
        userId: String,
        callback: (Result<Unit>) -> Unit
    ) : this(client, userId) {

        // Check initial authentication state
        checkAuthenticationState()

        // Initialize with Flutter and report result
        initializeWithFlutter(callback)
    }

    /**
     * Initialize using coroutines
     */
    companion object {
        suspend fun createAsync(client: AzeooClient, userId: String): AzeooUser =
            suspendCancellableCoroutine { continuation ->
                AzeooUser(client, userId) { result ->
                    when {
                        result.isSuccess -> {
                            val user = createSilent(client, userId)
                            continuation.resume(user)
                        }
                        result.isFailure -> continuation.resumeWithException(
                            result.exceptionOrNull() ?: Exception("User initialization failed")
                        )
                    }
                }
            }

        /**
         * Convenience method for immediate use (initialization happens in background)
         */
        fun createSilent(client: AzeooClient, userId: String): AzeooUser {
            val user = AzeooUser(client, userId)
            user.checkAuthenticationState()
            user.initializeWithFlutter { }  // Silent initialization
            return user
        }
    }

    internal fun initializeWithFlutter(callback: (Result<Unit>) -> Unit) {
        // Check if we're in multiple instances mode and engines are already initialized
        if (AzeooCore.shared.enableMultipleInstances) {
            println("AzeooUser: Multiple instances mode detected, skipping Flutter initialization (already done)")
            callback(Result.success(Unit))
            return
        }

        val arguments = FlutterRequestBuilder()
            .userId(userId)
            .build()

        executor.executeOnMainQueue(
            method = FlutterMethod.USER_INITIALIZE,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    println("✅ AzeooUser initialized successfully")
                    loadUserProfile { }  // Load profile in background
                    callback(Result.success(Unit))
                }
                result.isFailure -> {
                    val error = result.exceptionOrNull() ?: Exception("User initialization failed")
                    println("❌ AzeooUser initialization failed: ${error.message}")
                    callback(Result.failure(error))
                }
            }
        }
    }

    internal fun checkAuthenticationState() {
        isLoggedIn = client.isAuthenticated
    }

    /**
     * Get user profile
     */
    fun getProfile(callback: (Result<UserProfile>) -> Unit) {
        val arguments = FlutterRequestBuilder()
            .userId(userId)
            .build()

        executor.executeOnMainQueue(
            method = FlutterMethod.USER_GET_PROFILE,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    val profileData = result.getOrNull() as? Map<*, *>
                    if (profileData != null) {
                        val userProfile = UserProfile(
                            id = profileData["id"] as? String ?: userId,
                            email = profileData["email"] as? String,
                            name = profileData["name"] as? String,
                            avatarUrl = profileData["avatarUrl"] as? String,
                            preferences = profileData["preferences"] as? Map<String, Any> ?: emptyMap()
                        )
                        profile = userProfile
                        callback(Result.success(userProfile))
                    } else {
                        callback(Result.failure(Exception("Invalid profile data")))
                    }
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Failed to get profile")))
                }
            }
        }
    }

    /**
     * Get profile using coroutines
     */
    suspend fun getProfileAsync(): UserProfile = suspendCancellableCoroutine { continuation ->
        getProfile { result ->
            when {
                result.isSuccess -> continuation.resume(result.getOrThrow())
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Failed to get profile")
                )
            }
        }
    }

    /**
     * Update user profile
     */
    fun updateProfile(profileData: Map<String, Any>, callback: (Result<UserProfile>) -> Unit) {
        val arguments = FlutterRequestBuilder()
            .userId(userId)
            .config(profileData)
            .build()

        executor.executeOnMainQueue(
            method = FlutterMethod.USER_UPDATE_PROFILE,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    // Reload profile after update
                    getProfile(callback)
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Profile update failed")))
                }
            }
        }
    }

    /**
     * Update profile using coroutines
     */
    suspend fun updateProfileAsync(profileData: Map<String, Any>): UserProfile =
        suspendCancellableCoroutine { continuation ->
            updateProfile(profileData) { result ->
                when {
                    result.isSuccess -> continuation.resume(result.getOrThrow())
                    result.isFailure -> continuation.resumeWithException(
                        result.exceptionOrNull() ?: Exception("Profile update failed")
                    )
                }
            }
        }

    /**
     * Logout user
     */
    fun logout(callback: (Result<Unit>) -> Unit) {
        val arguments = FlutterRequestBuilder()
            .userId(userId)
            .build()

        executor.executeOnMainQueue(
            method = FlutterMethod.USER_LOGOUT,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    isLoggedIn = false
                    profile = null
                    lastSync = null
                    callback(Result.success(Unit))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Logout failed")))
                }
            }
        }
    }

    /**
     * Logout using coroutines
     */
    suspend fun logoutAsync() = suspendCancellableCoroutine { continuation ->
        logout { result ->
            when {
                result.isSuccess -> continuation.resume(Unit)
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Logout failed")
                )
            }
        }
    }

    /**
     * Delete user account
     */
    fun deleteAccount(callback: (Result<Unit>) -> Unit) {
        val arguments = FlutterRequestBuilder()
            .userId(userId)
            .build()

        executor.executeOnMainQueue(
            method = FlutterMethod.USER_DELETE_ACCOUNT,
            arguments = arguments
        ) { result ->
            when {
                result.isSuccess -> {
                    isLoggedIn = false
                    profile = null
                    lastSync = null
                    callback(Result.success(Unit))
                }
                result.isFailure -> {
                    callback(Result.failure(result.exceptionOrNull() ?: Exception("Account deletion failed")))
                }
            }
        }
    }

    /**
     * Delete account using coroutines
     */
    suspend fun deleteAccountAsync() = suspendCancellableCoroutine { continuation ->
        deleteAccount { result ->
            when {
                result.isSuccess -> continuation.resume(Unit)
                result.isFailure -> continuation.resumeWithException(
                    result.exceptionOrNull() ?: Exception("Account deletion failed")
                )
            }
        }
    }

    /**
     * Load user profile in background
     */
    private fun loadUserProfile(callback: (Result<UserProfile?>) -> Unit) {
        getProfile { result ->
            when {
                result.isSuccess -> {
                    lastSync = Date()
                    callback(Result.success(result.getOrNull()))
                }
                result.isFailure -> {
                    // Don't fail initialization if profile loading fails
                    println("Warning: Failed to load user profile: ${result.exceptionOrNull()?.message}")
                    callback(Result.success(null))
                }
            }
        }
    }

    /**
     * Sync user data
     */
    fun syncUserData(callback: (Result<Unit>) -> Unit) {
        loadUserProfile { result ->
            lastSync = Date()
            when {
                result.isSuccess -> callback(Result.success(Unit))
                result.isFailure -> callback(Result.failure(
                    result.exceptionOrNull() ?: Exception("Sync failed")
                ))
            }
        }
    }

    /**
     * Get user state
     */
    fun getState(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "isLoggedIn" to isLoggedIn,
            "profile" to (profile?.let { mapOf(
                "id" to it.id,
                "email" to it.email,
                "name" to it.name,
                "avatarUrl" to it.avatarUrl,
                "preferences" to it.preferences
            ) } ?: emptyMap<String, Any>()),
            "lastSync" to (lastSync?.time ?: 0L)
        )
    }
}
