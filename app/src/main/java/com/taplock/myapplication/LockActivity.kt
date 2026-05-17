package com.taplock.myapplication

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast

class LockActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (isAccessibilityServiceEnabled(this, LockAccessibilityService::class.java)) {
            // Service is ON: Lock the screen
            val intent = Intent(this, LockAccessibilityService::class.java).apply {
                action = LockAccessibilityService.ACTION_LOCK
            }
            startService(intent)
        } else {
            // Service is OFF: Go directly to settings
            Toast.makeText(this, "Please enable 'tap tap lock' service", Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
        
        finish()
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (info in enabledServices) {
            if (info.resolveInfo.serviceInfo.packageName == context.packageName &&
                info.resolveInfo.serviceInfo.name == service.name) {
                return true
            }
        }
        return false
    }
}
