package com.azeoo.sdk.core

/**
 * Flutter Method Command Definitions
 * 
 * Centralized location for all Flutter method channel method names.
 * This prevents typos and makes maintenance easier by keeping all
 * method strings in one place.
 * 
 * Equivalent to iOS FlutterMethod enum but using Kotlin sealed class
 * for better type safety and exhaustive when statements.
 */
sealed class FlutterMethod(val methodName: String) {
    
    // Client Methods
    object ClientInitialize : FlutterMethod("AzeooClient.initialize")
    object ClientGetSubscriptions : FlutterMethod("AzeooClient.getSubscriptions")
    object ClientValidateApiKey : FlutterMethod("AzeooClient.validateApiKey")
    object ClientUpdateConfiguration : FlutterMethod("AzeooClient.updateConfiguration")
    object ClientGetState : FlutterMethod("AzeooClient.getState")
    
    // User Methods
    object UserInitialize : FlutterMethod("AzeooUser.initialize")
    object UserGetToken : FlutterMethod("AzeooUser.getToken")
    object UserLogout : FlutterMethod("AzeooUser.logout")
    object UserGetProfile : FlutterMethod("AzeooUser.getProfile")
    object UserUpdate : FlutterMethod("AzeooUser.update")
    object UserDelete : FlutterMethod("AzeooUser.delete")
    object UserGetInfo : FlutterMethod("AzeooUser.getUserInfo")
    object UserGetEmail : FlutterMethod("AzeooUser.getEmail")
    object UserGetPhone : FlutterMethod("AzeooUser.getPhone")
    object UserGetAddress : FlutterMethod("AzeooUser.getAddress")
    object UserGetGender : FlutterMethod("AzeooUser.getGender")
    object UserGetBirthday : FlutterMethod("AzeooUser.getBirthday")
    object UserChangeHeight : FlutterMethod("AzeooUser.changeHeight")
    object UserChangeWeight : FlutterMethod("AzeooUser.changeWeight")
    object UserGetWeight : FlutterMethod("AzeooUser.getWeight")
    object UserGetHeight : FlutterMethod("AzeooUser.getHeight")
    
    // UI Methods
    object UIInitialize : FlutterMethod("AzeooUI.initialize")
    object UIShowMainScreen : FlutterMethod("AzeooUI.showMainScreen")
    object UIShowPermissionTestScreen : FlutterMethod("AzeooUI.showPermissionTestScreen")
    object UINavigateToScreen : FlutterMethod("AzeooUI.navigateToScreen")
    object UIHideScreen : FlutterMethod("AzeooUI.hideScreen")
    object UIChangePrimaryColor : FlutterMethod("AzeooUI.changePrimaryColor")
    object UISetTheme : FlutterMethod("AzeooUI.setTheme")
    object UIUpdateConfiguration : FlutterMethod("AzeooUI.updateConfiguration")
    
    companion object {
        /**
         * Get all available methods
         */
        fun getAllMethods(): List<FlutterMethod> = listOf(
            // Client Methods
            ClientInitialize,
            ClientGetSubscriptions,
            ClientValidateApiKey,
            ClientUpdateConfiguration,
            ClientGetState,
            
            // User Methods
            UserInitialize,
            UserGetToken,
            UserLogout,
            UserGetProfile,
            UserUpdate,
            UserDelete,
            UserGetInfo,
            UserGetEmail,
            UserGetPhone,
            UserGetAddress,
            UserGetGender,
            UserGetBirthday,
            UserChangeHeight,
            UserChangeWeight,
            UserGetWeight,
            UserGetHeight,
            
            // UI Methods
            UIInitialize,
            UIShowMainScreen,
            UIShowPermissionTestScreen,
            UINavigateToScreen,
            UIHideScreen,
            UIChangePrimaryColor,
            UISetTheme,
            UIUpdateConfiguration
        )
        
        /**
         * Get method by string name
         */
        fun fromString(methodName: String): FlutterMethod? {
            return getAllMethods().find { it.methodName == methodName }
        }
    }
    
    override fun toString(): String = methodName
}