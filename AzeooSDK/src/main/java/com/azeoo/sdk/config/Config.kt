package com.azeoo.sdk.config

import android.graphics.Color

/**
 * Configuration classes for the Azeoo SDK
 * Equivalent to iOS Config.swift
 */

/**
 * Main configuration class for SDK initialization
 */
data class Config(
    val userId: String,
    val authToken: String,
    val locale: String = "en",
    val showBottomNavigation: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val offlineSupport: Boolean = true,
    val appLinkHosts: List<String> = emptyList(),
    val safeArea: SafeAreaConfig = SafeAreaConfig.all(),
    val theme: ThemeConfig? = null
) {
    
    /**
     * Convert to dictionary for Flutter method channel
     */
    fun toDictionary(): Map<String, Any> {
        val dict = mutableMapOf<String, Any>()
        dict["userId"] = userId
        dict["authToken"] = authToken
        dict["locale"] = locale
        dict["showBottomNavigation"] = showBottomNavigation
        dict["analyticsEnabled"] = analyticsEnabled
        dict["offlineSupport"] = offlineSupport
        dict["appLinkHosts"] = appLinkHosts
        dict["safeArea"] = safeArea.toDictionary()
        
        theme?.let { dict["theme"] = it.toDictionary() }
        
        return dict
    }
    
    companion object {
        /**
         * Create a builder for Config
         */
        fun builder(): ConfigBuilder = ConfigBuilder()
    }
}

/**
 * Builder pattern for Config construction
 */
class ConfigBuilder {
    private var userId: String? = null
    private var authToken: String? = null
    private var locale: String = "en"
    private var showBottomNavigation: Boolean = true
    private var analyticsEnabled: Boolean = true
    private var offlineSupport: Boolean = true
    private var appLinkHosts: List<String> = emptyList()
    private var safeArea: SafeAreaConfig = SafeAreaConfig.all()
    private var theme: ThemeConfig? = null
    
    fun setUserId(userId: String): ConfigBuilder {
        this.userId = userId
        return this
    }
    
    fun setAuthToken(authToken: String): ConfigBuilder {
        this.authToken = authToken
        return this
    }
    
    fun setLocale(locale: String): ConfigBuilder {
        this.locale = locale
        return this
    }
    
    fun setShowBottomNavigation(show: Boolean): ConfigBuilder {
        this.showBottomNavigation = show
        return this
    }
    
    fun setAnalyticsEnabled(enabled: Boolean): ConfigBuilder {
        this.analyticsEnabled = enabled
        return this
    }
    
    fun setOfflineSupport(enabled: Boolean): ConfigBuilder {
        this.offlineSupport = enabled
        return this
    }
    
    fun setAppLinkHosts(hosts: List<String>): ConfigBuilder {
        this.appLinkHosts = hosts
        return this
    }
    
    fun setSafeArea(safeArea: SafeAreaConfig): ConfigBuilder {
        this.safeArea = safeArea
        return this
    }
    
    fun setTheme(theme: ThemeConfig): ConfigBuilder {
        this.theme = theme
        return this
    }
    
    fun build(): Result<Config> {
        val userId = this.userId ?: return Result.failure(
            IllegalArgumentException("User ID is required")
        )
        val authToken = this.authToken ?: return Result.failure(
            IllegalArgumentException("Auth token is required")
        )
        
        return Result.success(
            Config(
                userId = userId,
                authToken = authToken,
                locale = locale,
                showBottomNavigation = showBottomNavigation,
                analyticsEnabled = analyticsEnabled,
                offlineSupport = offlineSupport,
                appLinkHosts = appLinkHosts,
                safeArea = safeArea,
                theme = theme
            )
        )
    }
}

/**
 * Safe area configuration for UI layout
 */
data class SafeAreaConfig(
    val top: Boolean = true,
    val bottom: Boolean = true,
    val left: Boolean = true,
    val right: Boolean = true
) {
    
    fun toDictionary(): Map<String, Boolean> {
        return mapOf(
            "top" to top,
            "bottom" to bottom,
            "left" to left,
            "right" to right
        )
    }
    
    companion object {
        fun all(): SafeAreaConfig = SafeAreaConfig(true, true, true, true)
        fun none(): SafeAreaConfig = SafeAreaConfig(false, false, false, false)
        fun vertical(): SafeAreaConfig = SafeAreaConfig(true, true, false, false)
        fun horizontal(): SafeAreaConfig = SafeAreaConfig(false, false, true, true)
    }
}

/**
 * Theme configuration for customizing UI appearance
 */
data class ThemeConfig(
    // Light theme colors
    val lightPrimaryColor: Int? = null,
    val lightSecondaryColor: Int? = null,
    val lightTertiaryColor: Int? = null,
    val lightBackgroundColor: Int? = null,
    
    // Dark theme colors
    val darkPrimaryColor: Int? = null,
    val darkSecondaryColor: Int? = null,
    val darkTertiaryColor: Int? = null,
    val darkBackgroundColor: Int? = null,
    
    // Status colors
    val success: Int? = null,
    val error: Int? = null,
    val warning: Int? = null
) {
    
    fun toDictionary(): Map<String, String> {
        val dict = mutableMapOf<String, String>()
        
        lightPrimaryColor?.let { dict["lightPrimaryColor"] = colorToHex(it) }
        lightSecondaryColor?.let { dict["lightSecondaryColor"] = colorToHex(it) }
        lightTertiaryColor?.let { dict["lightTertiaryColor"] = colorToHex(it) }
        lightBackgroundColor?.let { dict["lightBackgroundColor"] = colorToHex(it) }
        
        darkPrimaryColor?.let { dict["darkPrimaryColor"] = colorToHex(it) }
        darkSecondaryColor?.let { dict["darkSecondaryColor"] = colorToHex(it) }
        darkTertiaryColor?.let { dict["darkTertiaryColor"] = colorToHex(it) }
        darkBackgroundColor?.let { dict["darkBackgroundColor"] = colorToHex(it) }
        
        success?.let { dict["success"] = colorToHex(it) }
        error?.let { dict["error"] = colorToHex(it) }
        warning?.let { dict["warning"] = colorToHex(it) }
        
        return dict
    }
    
    private fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
    
    companion object {
        /**
         * Helper to create color from hex string
         */
        fun fromHexString(hex: String): Int {
            return Color.parseColor(hex)
        }
    }
}
