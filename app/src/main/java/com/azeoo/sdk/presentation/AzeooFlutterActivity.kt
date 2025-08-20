package com.azeoo.sdk.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.OnBackPressedCallback
import io.flutter.embedding.android.FlutterFragment
import com.azeoo.sdk.core.FlutterEngineManager
import com.azeoo.sdk.data.model.SDKConfiguration
import kotlinx.coroutines.launch

/**
 * Flutter Activity for the Azeoo Nutrition SDK
 * 
 * This activity hosts the Flutter UI components using Android best practices:
 * - Extends ComponentActivity for modern Android lifecycle
 * - Uses FlutterFragment for embedding Flutter content
 * - Handles different screen types via intent extras
 * - Proper lifecycle management with coroutines
 * - Intent factory methods for type-safe launching
 */
class AzeooFlutterActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "AzeooFlutterActivity"
        private const val EXTRA_SCREEN_TYPE = "screen_type"
        private const val EXTRA_CONFIG = "config"
        private const val SCREEN_TYPE_MAIN = "main"
        private const val SCREEN_TYPE_PERMISSION_TEST = "permission_test"
        
        /**
         * Launch the main screen
         */
        fun launchMainScreen(context: Context, configuration: SDKConfiguration) {
            val intent = createIntent(context, SCREEN_TYPE_MAIN, configuration)
            context.startActivity(intent)
        }
        
        /**
         * Launch the permission test screen
         */
        fun launchPermissionTest(context: Context, configuration: SDKConfiguration) {
            val intent = createIntent(context, SCREEN_TYPE_PERMISSION_TEST, configuration)
            context.startActivity(intent)
        }
        
        private fun createIntent(
            context: Context,
            screenType: String,
            configuration: SDKConfiguration
        ): Intent {
            return Intent(context, AzeooFlutterActivity::class.java).apply {
                putExtra(EXTRA_SCREEN_TYPE, screenType)
                putExtra(EXTRA_CONFIG, configuration)
                // Add flags for proper task management
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
    
    private var flutterFragment: FlutterFragment? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val screenType = intent.getStringExtra(EXTRA_SCREEN_TYPE) ?: SCREEN_TYPE_MAIN
        val configuration = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_CONFIG, SDKConfiguration::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_CONFIG)
        }
        
        if (configuration == null) {
            Log.e(TAG, "No configuration provided")
            finish()
            return
        }
        
        setupBackButtonHandling()
        setupFlutterFragment(screenType, configuration)
    }
    
    /**
     * Setup Flutter fragment based on screen type
     */
    private fun setupFlutterFragment(screenType: String, configuration: SDKConfiguration) {
        val flutterEngine = FlutterEngineManager.getFlutterEngine()
        
        if (flutterEngine == null) {
            Log.e(TAG, "Flutter engine not available")
            finish()
            return
        }
        
        // Create FlutterFragment with cached engine
        flutterFragment = FlutterFragment.withCachedEngine("main_engine")
            .destroyEngineWithFragment(false) // Keep engine alive
            .build<FlutterFragment>()
        
        // Add fragment to activity
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, flutterFragment!!)
            .commit()
        
        // Launch appropriate screen after fragment is ready
        lifecycleScope.launch {
            when (screenType) {
                SCREEN_TYPE_MAIN -> {
                    FlutterEngineManager.launchMainScreen()
                }
                SCREEN_TYPE_PERMISSION_TEST -> {
                    FlutterEngineManager.launchPermissionTestScreen()
                }
                else -> {
                    Log.w(TAG, "Unknown screen type: $screenType")
                    FlutterEngineManager.launchMainScreen()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Activity resumed")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Activity paused")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Activity destroyed")
        flutterFragment = null
    }
    
    /**
     * Setup back button handling
     */
    private fun setupBackButtonHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Let Flutter handle back button first
                flutterFragment?.let { fragment ->
                    if (fragment.isAdded && !fragment.isDetached) {
                        // Flutter fragment is active, let it handle back press
                        // For now, just finish the activity
                        finish()
                        return
                    }
                }
                
                // Default behavior
                finish()
            }
        })
    }
}