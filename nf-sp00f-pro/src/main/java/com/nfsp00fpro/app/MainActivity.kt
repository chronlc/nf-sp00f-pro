package com.nfsp00fpro.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.nfsp00fpro.app.modules.ModMainNfsp00f

class MainActivity : AppCompatActivity() {
    private lateinit var moduleManager: ModMainNfsp00f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Use edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_main)
        
        // Initialize module manager
        moduleManager = ModMainNfsp00f(this)
        moduleManager.initialize()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shutdown modules when activity is destroyed
        moduleManager.shutdown()
    }
}
