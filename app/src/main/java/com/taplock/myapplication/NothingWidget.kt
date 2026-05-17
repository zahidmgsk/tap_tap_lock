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

class NothingWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val ACTION_WIDGET_TAP = "com.taplock.myapplication.WIDGET_TAP"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.nothing_widget_layout)
            
            val prefs = context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE)
            val transparency = prefs.getInt("widget_transparency", 255)
            
            // Nothing Phone black background
            views.setInt(R.id.widget_background, "setColorFilter", Color.BLACK)
            views.setInt(R.id.widget_background, "setImageAlpha", transparency)
            
            // DOTTED SMILEY LOGIC for Nothing Widget
            val isEnabled = isAccessibilityServiceEnabled(context)
            val iconRes = if (isEnabled) R.drawable.ic_face_happy_dotted else R.drawable.ic_face_sad_dotted
            views.setImageViewResource(R.id.widget_icon, iconRes)
            
            val intent = Intent(context, LockWidget::class.java).apply {
                action = ACTION_WIDGET_TAP
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 4000,
                intent,
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
