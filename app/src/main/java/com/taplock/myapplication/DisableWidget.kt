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

class DisableWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_disable_layout)
            
            // Dynamic Color
            val isEnabled = isAccessibilityServiceEnabled(context)
            val crossColor = if (isEnabled) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
            views.setInt(R.id.widget_background, "setColorFilter", crossColor)

            val intent = Intent(context, DisableActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId + 5000, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
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
