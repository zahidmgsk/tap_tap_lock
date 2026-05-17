package com.taplock.myapplication

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.accessibility.AccessibilityManager
import android.widget.RemoteViews

class CombinedWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.lock_disable_combined_layout)
            
            val prefs = context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE)
            val themeColor = prefs.getInt("app_theme_color", Color.parseColor("#6750A4"))
            
            views.setInt(R.id.lock_bg, "setColorFilter", themeColor)

            // Dynamic Cross Color based on status
            val isEnabled = isAccessibilityServiceEnabled(context)
            val crossColor = if (isEnabled) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            views.setInt(R.id.disable_bg, "setColorFilter", crossColor)

            // Lock Action - Use Broadcast to LockWidget for Double Tap logic support
            val lockIntent = Intent(context, LockWidget::class.java).apply {
                action = "com.taplock.myapplication.WIDGET_TAP"
            }
            val lockPendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId + 6000, lockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_lock, lockPendingIntent)

            // Disable Action - Launches DisableActivity for immediate toast and action
            val disableIntent = Intent(context, DisableActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            val disablePendingIntent = PendingIntent.getActivity(
                context, appWidgetId + 7000, disableIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_disable, disablePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun isAccessibilityServiceEnabled(context: Context): Boolean {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            for (info in enabledServices) {
                if (info.resolveInfo.serviceInfo.packageName == context.packageName &&
                    info.resolveInfo.serviceInfo.name == LockAccessibilityService::class.java.name) {
                    return true
                }
            }
            return false
        }
    }
}
