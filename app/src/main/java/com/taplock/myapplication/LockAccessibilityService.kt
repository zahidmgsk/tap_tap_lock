package com.taplock.myapplication

import android.accessibilityservice.AccessibilityService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class LockAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        updateWidgets()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_LOCK -> {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
            ACTION_DISABLE -> {
                disableSelf()
                updateWidgets()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateWidgets()
    }

    private fun updateWidgets() {
        val widgetClasses = listOf(
            LockWidget::class.java,
            LockWidgetMin::class.java,
            LockWidgetText::class.java,
            LockWidgetTile::class.java,
            NothingWidget::class.java,
            DisableWidget::class.java,
            CombinedWidget::class.java,
            CombinedWidgetVertical::class.java
        )
        for (widgetClass in widgetClasses) {
            val intent = Intent(this, widgetClass).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(ComponentName(this, widgetClass))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(intent)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    companion object {
        const val ACTION_LOCK = "com.taplock.myapplication.ACTION_LOCK"
        const val ACTION_DISABLE = "com.taplock.myapplication.ACTION_DISABLE"
    }
}
