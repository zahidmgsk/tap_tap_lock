package com.taplock.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class DisableActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show Toast from foreground to guarantee visibility
        Toast.makeText(this, "❌ Killing Accessibility Service...", Toast.LENGTH_SHORT).show()
        
        // Command service to disable itself
        val serviceIntent = Intent(this, LockAccessibilityService::class.java).apply {
            action = LockAccessibilityService.ACTION_DISABLE
        }
        try {
            startService(serviceIntent)
        } catch (e: Exception) {
            // Ignore
        }
        
        finish()
    }
}
