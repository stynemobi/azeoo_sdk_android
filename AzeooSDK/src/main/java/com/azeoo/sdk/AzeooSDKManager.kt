package com.azeoo.sdk

import com.azeoo.sdk.client.AzeooClient
import com.azeoo.sdk.ui.AzeooUI
import com.azeoo.sdk.user.AzeooUser

/**
 * Singleton SDK Manager for managing all Azeoo SDK components
 * Equivalent to iOS AzeooSDKManager.swift
 * 
 * This singleton ensures that SDK components are globally accessible
 * and prevents reference passing issues between activities/fragments.
 */
class AzeooSDKManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: AzeooSDKManager? = null
        
        /**
         * Get the shared singleton instance
         */
        val shared: AzeooSDKManager
            get() = INSTANCE ?: synchronized(this) {
                INSTANCE ?: AzeooSDKManager().also { INSTANCE = it }
            }
    }
    
    // SDK Components (nullable until configured)
    var client: AzeooClient? = null
        private set
    
    var user: AzeooUser? = null
        private set
    
    var ui: AzeooUI? = null
        private set
    
    /**
     * Configure the SDK manager with client and user
     */
    fun configure(client: AzeooClient, user: AzeooUser) {
        this.client = client
        this.user = user
        println("✅ AzeooSDKManager configured with client and user")
    }
    
    /**
     * Set the UI component after initialization
     */
    fun setUI(ui: AzeooUI) {
        this.ui = ui
        println("✅ AzeooSDKManager UI component set")
    }
    
    /**
     * Check if all components are fully initialized
     */
    val isFullyInitialized: Boolean
        get() = client != null && user != null && ui != null
    
    /**
     * Check if client is initialized
     */
    val isClientInitialized: Boolean
        get() = client?.isClientInitialized() == true
    
    /**
     * Check if user is initialized
     */
    val isUserInitialized: Boolean
        get() = user != null
    
    /**
     * Check if UI is initialized
     */
    val isUIInitialized: Boolean
        get() = ui?.isUIInitialized == true
    
    /**
     * Get current SDK state
     */
    fun getSDKState(): Map<String, Any> {
        return mapOf(
            "isFullyInitialized" to isFullyInitialized,
            "client" to (client?.getConfiguration() ?: emptyMap<String, Any>()),
            "user" to (user?.getState() ?: emptyMap<String, Any>()),
            "ui" to (ui?.getCurrentState() ?: emptyMap<String, Any>()),
            "components" to mapOf(
                "client" to isClientInitialized,
                "user" to isUserInitialized,
                "ui" to isUIInitialized
            )
        )
    }
    
    /**
     * Reset all SDK components
     */
    fun reset() {
        ui?.resetState()
        ui = null
        user = null
        client = null
        println("🔄 AzeooSDKManager reset")
    }
    
    /**
     * Dispose of SDK resources
     */
    fun dispose() {
        reset()
        // Additional cleanup if needed
        println("🗑️ AzeooSDKManager disposed")
    }
}
