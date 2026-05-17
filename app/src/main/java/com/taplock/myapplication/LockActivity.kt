package com.taplock.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class LockActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Send a command to our Accessibility Service to perform the "Soft Lock"
        val intent = Intent(this, LockAccessibilityService::class.java).apply {
            action = LockAccessibilityService.ACTION_LOCK
        }
        startService(intent)
        
        finish()
    }
}
