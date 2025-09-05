package com.azeoo.sdk

/**
 * Main namespace object for the Azeoo SDK
 * Equivalent to iOS AzeooSDK.swift
 * 
 * This object serves as the main entry point and provides
 * access to all SDK components and utilities.
 */
object AzeooSDK {
    
    /**
     * SDK version information
     */
    const val VERSION = "1.0.0"
    const val BUILD_NUMBER = "1"
    
    /**
     * Get SDK version
     */
    fun getVersion(): String = VERSION
    
    /**
     * Get SDK build number
     */
    fun getBuildNumber(): String = BUILD_NUMBER
    
    /**
     * Get full version string
     */
    fun getFullVersion(): String = "$VERSION ($BUILD_NUMBER)"
    
    /**
     * SDK information
     */
    fun getSDKInfo(): Map<String, String> {
        return mapOf(
            "name" to "Azeoo SDK",
            "version" to VERSION,
            "buildNumber" to BUILD_NUMBER,
            "platform" to "Android"
        )
    }
}
