package com.azeoo.sdk.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Flutter method commands - equivalent to iOS FlutterMethodCommand.swift
 */
enum class FlutterMethod(val methodName: String) {
    CLIENT_INITIALIZE("AzeooClient.initialize"),
    CLIENT_VALIDATE_API_KEY("AzeooClient.validateApiKey"),
    CLIENT_GET_SUBSCRIPTIONS("AzeooClient.getSubscriptions"),
    CLIENT_UPDATE_CONFIGURATION("AzeooClient.updateConfiguration"),
    CLIENT_GET_STATE("AzeooClient.getState"),
    
    USER_INITIALIZE("AzeooUser.initialize"),
    USER_GET_PROFILE("AzeooUser.getProfile"),
    USER_UPDATE_PROFILE("AzeooUser.updateProfile"),
    USER_LOGOUT("AzeooUser.logout"),
    USER_DELETE_ACCOUNT("AzeooUser.deleteAccount"),
    
    UI_INITIALIZE("AzeooUI.initialize"),
    UI_SHOW_SCREEN("AzeooUI.showScreen"),
    UI_UPDATE_THEME("AzeooUI.updateTheme"),
    UI_GET_STATE("AzeooUI.getState"),
    
    // Module-specific methods
    NUTRITION_DISPLAY("AzeooUI.nutrition"),
    TRAINING_DISPLAY("AzeooUI.training")
}

/**
 * Command executor for Flutter method channel communication
 * Equivalent to iOS FlutterCommandExecutor
 */
class FlutterCommandExecutor(private val module: AzeooModule? = null) {


    
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    /**
     * Execute command on main thread
     */
    fun executeOnMainQueue(
        method: FlutterMethod,
        arguments: Map<String, Any>?,
        callback: (Result<Any?>) -> Unit
    ) {
        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.Main) {
                    executeCommand(method, arguments)
                }
                callback(Result.success(result))
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }
    }
    
    /**
     * Execute command synchronously
     */
    private suspend fun executeCommand(
        method: FlutterMethod,
        arguments: Map<String, Any>?
    ): Any? {
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                AzeooCore.shared.invokeFlutterMethod(
                    method.methodName,
                    arguments,
                    module
                ) { result ->
                    when {
                        result.isSuccess -> continuation.resume(result.getOrNull())
                        result.isFailure -> continuation.resumeWithException(
                            result.exceptionOrNull() ?: Exception("Unknown error")
                        )
                    }
                }
            }
        }
    }
}

/**
 * Builder for Flutter request arguments
 * Equivalent to iOS FlutterRequestBuilder
 */
class FlutterRequestBuilder {
    private val arguments = mutableMapOf<String, Any>()
    
    fun apiKey(apiKey: String): FlutterRequestBuilder {
        arguments["apiKey"] = apiKey
        return this
    }
    
    fun userId(userId: String): FlutterRequestBuilder {
        arguments["userId"] = userId
        return this
    }
    
    fun config(config: Map<String, Any>): FlutterRequestBuilder {
        arguments.putAll(config)
        return this
    }
    
    fun theme(theme: Map<String, Any>): FlutterRequestBuilder {
        arguments["theme"] = theme
        return this
    }
    
    fun locale(locale: String): FlutterRequestBuilder {
        arguments["locale"] = locale
        return this
    }
    
    fun authToken(token: String): FlutterRequestBuilder {
        arguments["authToken"] = token
        return this
    }
    
    fun showBottomNavigation(show: Boolean): FlutterRequestBuilder {
        arguments["showBottomNavigation"] = show
        return this
    }
    
    fun analyticsEnabled(enabled: Boolean): FlutterRequestBuilder {
        arguments["analyticsEnabled"] = enabled
        return this
    }
    
    fun offlineSupport(enabled: Boolean): FlutterRequestBuilder {
        arguments["offlineSupport"] = enabled
        return this
    }
    
    fun safeArea(safeArea: Map<String, Boolean>): FlutterRequestBuilder {
        arguments["safeArea"] = safeArea
        return this
    }
    
    fun appLinkHosts(hosts: List<String>): FlutterRequestBuilder {
        arguments["appLinkHosts"] = hosts
        return this
    }
    
    fun build(): Map<String, Any> {
        return arguments.toMap()
    }
}
