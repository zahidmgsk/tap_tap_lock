package com.taplock.myapplication

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews

class LockWidgetMin : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.lock_widget_min_layout)
            
            val prefs = context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE)
            val themeColor = prefs.getInt("app_theme_color", Color.parseColor("#6750A4"))
            
            // For minimal, we apply color to both the outline and the icon
            views.setInt(R.id.widget_background, "setColorFilter", themeColor)
            views.setInt(R.id.widget_icon, "setColorFilter", themeColor)
            
            val intent = Intent(context, LockWidget::class.java).apply {
                action = "com.taplock.myapplication.WIDGET_TAP"
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
