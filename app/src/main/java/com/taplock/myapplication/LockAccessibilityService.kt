package com.taplock.myapplication

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class LockAccessibilityService : AccessibilityService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_LOCK) {
            // This is the "Soft Power Key" action
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    companion object {
        const val ACTION_LOCK = "com.taplock.myapplication.ACTION_LOCK"
    }
}
