package com.azeoo.sdk.util

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Extension functions for the Azeoo SDK
 * 
 * Provides Kotlin idiomatic extensions for:
 * - Color handling
 * - Permission checking
 * - Async operations
 * - Logging utilities
 */

// Color Extensions
/**
 * Convert Android color int to hex string for Flutter
 */
@ColorInt
fun Int.toHexColor(): String = String.format("#%06X", 0xFFFFFF and this)

/**
 * Parse hex color string to Android color int
 */
fun String.parseColor(): Int? = try {
    Color.parseColor(this)
} catch (e: IllegalArgumentException) {
    null
}

// Context Extensions
/**
 * Check if permission is granted
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Get application name
 */
fun Context.getAppName(): String {
    val applicationInfo = applicationInfo
    val stringId = applicationInfo.labelRes
    return if (stringId == 0) {
        applicationInfo.nonLocalizedLabel.toString()
    } else {
        getString(stringId)
    }
}

// Coroutine Extensions
/**
 * Safe async operation with error handling
 */
inline fun <T> CoroutineScope.safeAsync(
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend () -> T
): Deferred<Result<T>> = async(context) {
    try {
        Result.success(block())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/**
 * Safe launch with error handling
 */
inline fun CoroutineScope.safeLaunch(
    context: CoroutineContext = Dispatchers.Main,
    crossinline onError: (Throwable) -> Unit = { Log.e("SafeLaunch", "Error in coroutine", it) },
    crossinline block: suspend () -> Unit
): Job = launch(context) {
    try {
        block()
    } catch (e: Exception) {
        onError(e)
    }
}

// Result Extensions
/**
 * Execute different blocks based on Result success/failure
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (isSuccess) action(getOrNull()!!)
    return this
}

inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (isFailure) action(exceptionOrNull()!!)
    return this
}

// String Extensions
/**
 * Validate if string is a valid locale code (2 letters)
 */
fun String.isValidLocale(): Boolean = matches(Regex("^[a-z]{2}$"))

/**
 * Validate if string is a valid URL scheme
 */
fun String.isValidUrlScheme(): Boolean = matches(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*$"))

// Map Extensions
/**
 * Safe get with type casting
 */
inline fun <reified T> Map<String, Any>.safeGet(key: String): T? = this[key] as? T

/**
 * Get string value safely
 */
fun Map<String, Any>.getString(key: String, default: String = ""): String = 
    safeGet<String>(key) ?: default

/**
 * Get boolean value safely
 */
fun Map<String, Any>.getBoolean(key: String, default: Boolean = false): Boolean = 
    safeGet<Boolean>(key) ?: default

/**
 * Get int value safely
 */
fun Map<String, Any>.getInt(key: String, default: Int = 0): Int = 
    safeGet<Int>(key) ?: default

// Collection Extensions
/**
 * Safe first element
 */
fun <T> List<T>.safeFirst(): T? = if (isEmpty()) null else first()

/**
 * Safe last element  
 */
fun <T> List<T>.safeLast(): T? = if (isEmpty()) null else last()

// Logging Extensions
/**
 * Debug log with tag
 */
fun Any.logd(message: String, tag: String = this::class.java.simpleName) {
    Log.d(tag, message)
}

/**
 * Error log with tag
 */
fun Any.loge(message: String, throwable: Throwable? = null, tag: String = this::class.java.simpleName) {
    if (throwable != null) {
        Log.e(tag, message, throwable)
    } else {
        Log.e(tag, message)
    }
}

/**
 * Warning log with tag
 */
fun Any.logw(message: String, tag: String = this::class.java.simpleName) {
    Log.w(tag, message)
}

/**
 * Info log with tag
 */
fun Any.logi(message: String, tag: String = this::class.java.simpleName) {
    Log.i(tag, message)
}