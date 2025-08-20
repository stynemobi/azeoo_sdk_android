package com.azeoo.sdk.data.model

import androidx.annotation.ColorInt
import com.azeoo.sdk.data.exception.SDKException
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

/**
 * Configuration data class for the Azeoo Nutrition SDK
 * 
 * Uses modern Android patterns:
 * - Data class for immutability
 * - Builder pattern for flexible configuration
 * - Parcelable for efficient serialization
 * - Validation logic
 * - JSON serialization support
 */
@Parcelize
data class SDKConfiguration(
    val apiKey: String,
    val userId: String,
    val authToken: String,
    val locale: String = "en",
    val analyticsEnabled: Boolean = true,
    val offlineMode: Boolean = false,
    val theme: ThemeConfiguration? = null,
    val safeArea: SafeAreaConfiguration = SafeAreaConfiguration(),
    val deepLinks: DeepLinkConfiguration? = null,
    val debugMode: Boolean = false
) : Parcelable {
    
    /**
     * Validate configuration parameters
     * @throws SDKException.InvalidConfiguration if validation fails
     */
    fun validate() {
        when {
            apiKey.isBlank() -> throw SDKException.InvalidConfiguration("apiKey cannot be blank")
            userId.isBlank() -> throw SDKException.InvalidConfiguration("userId cannot be blank")
            authToken.isBlank() -> throw SDKException.InvalidConfiguration("authToken cannot be blank")
            locale.length != 2 -> throw SDKException.InvalidConfiguration("locale must be 2-letter code")
        }
        
        theme?.validate()
        deepLinks?.validate()
    }
    
    /**
     * Convert to JSON for Flutter communication
     */
    fun toJson(): String = Gson().toJson(this)
    
    /**
     * Convert to Map for Flutter method channel
     */
    fun toMap(): Map<String, Any> = mapOf(
        "apiKey" to apiKey,
        "userId" to userId,
        "authToken" to authToken,
        "locale" to locale,
        "analyticsEnabled" to analyticsEnabled,
        "offlineMode" to offlineMode,
        "debugMode" to debugMode,
        "theme" to (theme?.toMap() ?: emptyMap<String, Any>()),
        "safeArea" to safeArea.toMap(),
        "deepLinks" to (deepLinks?.toMap() ?: emptyMap<String, Any>())
    )
    

    
    /**
     * Builder class for creating SDKConfiguration instances
     */
    class Builder {
        private var apiKey: String? = null
        private var userId: String? = null
        private var authToken: String? = null
        private var locale: String = "en"
        private var analyticsEnabled: Boolean = true
        private var offlineMode: Boolean = false
        private var theme: ThemeConfiguration? = null
        private var safeArea: SafeAreaConfiguration = SafeAreaConfiguration()
        private var deepLinks: DeepLinkConfiguration? = null
        private var debugMode: Boolean = false
        
        fun apiKey(apiKey: String) = apply { this.apiKey = apiKey }
        fun userId(userId: String) = apply { this.userId = userId }
        fun authToken(authToken: String) = apply { this.authToken = authToken }
        fun locale(locale: String) = apply { this.locale = locale }
        fun enableAnalytics(enabled: Boolean = true) = apply { this.analyticsEnabled = enabled }
        fun enableOfflineMode(enabled: Boolean = true) = apply { this.offlineMode = enabled }
        fun theme(theme: ThemeConfiguration) = apply { this.theme = theme }
        fun safeArea(safeArea: SafeAreaConfiguration) = apply { this.safeArea = safeArea }
        fun deepLinks(deepLinks: DeepLinkConfiguration) = apply { this.deepLinks = deepLinks }
        fun enableDebugMode(enabled: Boolean = true) = apply { this.debugMode = enabled }
        
        /**
         * Build the configuration
         * @throws SDKException.InvalidConfiguration if required fields are missing
         */
        fun build(): SDKConfiguration {
            val config = SDKConfiguration(
                apiKey = apiKey ?: throw SDKException.InvalidConfiguration("apiKey is required"),
                userId = userId ?: throw SDKException.InvalidConfiguration("userId is required"),
                authToken = authToken ?: throw SDKException.InvalidConfiguration("authToken is required"),
                locale = locale,
                analyticsEnabled = analyticsEnabled,
                offlineMode = offlineMode,
                theme = theme,
                safeArea = safeArea,
                deepLinks = deepLinks,
                debugMode = debugMode
            )
            
            config.validate()
            return config
        }
    }
    
    companion object {
        /**
         * Create configuration from JSON
         */
        fun fromJson(json: String): SDKConfiguration? {
            return try {
                Gson().fromJson(json, SDKConfiguration::class.java)
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Create a builder instance
         */
        fun builder() = Builder()
    }
}

/**
 * Theme configuration for the SDK
 */
@Parcelize
data class ThemeConfiguration(
    @ColorInt val primaryColor: Int? = null,
    @ColorInt val backgroundColor: Int? = null,
    @ColorInt val surfaceColor: Int? = null,
    @ColorInt val textColor: Int? = null,
    val isDarkMode: Boolean = false,
    val fontFamily: String? = null
) : Parcelable {
    
    fun validate() {
        // Add any theme-specific validation if needed
    }
    
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "isDarkMode" to isDarkMode
        )
        
        primaryColor?.let { map["primaryColor"] = String.format("#%06X", 0xFFFFFF and it) }
        backgroundColor?.let { map["backgroundColor"] = String.format("#%06X", 0xFFFFFF and it) }
        surfaceColor?.let { map["surfaceColor"] = String.format("#%06X", 0xFFFFFF and it) }
        textColor?.let { map["textColor"] = String.format("#%06X", 0xFFFFFF and it) }
        fontFamily?.let { map["fontFamily"] = it }
        
        return map
    }
    
    companion object {
        fun light(primaryColor: Int? = null) = ThemeConfiguration(
            primaryColor = primaryColor,
            isDarkMode = false
        )
        
        fun dark(primaryColor: Int? = null) = ThemeConfiguration(
            primaryColor = primaryColor,
            isDarkMode = true
        )
    }
}

/**
 * Safe area configuration for the SDK
 */
@Parcelize
data class SafeAreaConfiguration(
    val top: Boolean = true,
    val bottom: Boolean = true,
    val left: Boolean = true,
    val right: Boolean = true
) : Parcelable {
    
    fun toMap(): Map<String, Any> = mapOf(
        "top" to top,
        "bottom" to bottom,
        "left" to left,
        "right" to right
    )
    
    companion object {
        fun all() = SafeAreaConfiguration()
        fun none() = SafeAreaConfiguration(false, false, false, false)
        fun vertical() = SafeAreaConfiguration(top = true, bottom = true, left = false, right = false)
        fun horizontal() = SafeAreaConfiguration(top = false, bottom = false, left = true, right = true)
    }
}

/**
 * Deep link configuration for the SDK
 */
@Parcelize
data class DeepLinkConfiguration(
    val scheme: String,
    val host: String,
    val enabledPaths: List<String> = emptyList(),
    val enableUniversalLinks: Boolean = false
) : Parcelable {
    
    fun validate() {
        when {
            scheme.isBlank() -> throw SDKException.InvalidConfiguration("deeplink scheme cannot be blank")
            host.isBlank() -> throw SDKException.InvalidConfiguration("deeplink host cannot be blank")
            !scheme.matches(Regex("^[a-zA-Z][a-zA-Z0-9+.-]*$")) -> 
                throw SDKException.InvalidConfiguration("invalid deeplink scheme format")
        }
    }
    
    fun toMap(): Map<String, Any> = mapOf(
        "scheme" to scheme,
        "host" to host,
        "enabledPaths" to enabledPaths,
        "enableUniversalLinks" to enableUniversalLinks
    )
    
    fun buildDeepLink(path: String): String {
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$scheme://$host$cleanPath"
    }
    
    fun buildUniversalLink(path: String): String? {
        if (!enableUniversalLinks) return null
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "https://$host$cleanPath"
    }
}