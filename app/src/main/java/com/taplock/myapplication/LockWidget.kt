package com.taplock.myapplication

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews

class LockWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_TAP) {
            handleTap(context)
        }
    }

    private fun handleTap(context: Context) {
        val prefs = context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE)
        val isDoubleTapEnabled = prefs.getBoolean("double_tap_enabled", false)

        if (isDoubleTapEnabled) {
            val lastTapTime = prefs.getLong("last_tap_time", 0L)
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastTapTime < 500) {
                prefs.edit().putLong("last_tap_time", 0L).apply()
                launchLockActivity(context)
            } else {
                prefs.edit().putLong("last_tap_time", currentTime).apply()
            }
        } else {
            launchLockActivity(context)
        }
    }

    private fun launchLockActivity(context: Context) {
        val intent = Intent(context, LockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
    }

    companion object {
        const val ACTION_WIDGET_TAP = "com.taplock.myapplication.WIDGET_TAP"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.lock_widget_layout)
            
            val prefs = context.getSharedPreferences("LockSettings", Context.MODE_PRIVATE)
            val themeColor = prefs.getInt("app_theme_color", Color.parseColor("#6750A4"))
            
            // Correctly tint the background image
            views.setInt(R.id.widget_background, "setColorFilter", themeColor)
            
            val intent = Intent(context, LockWidget::class.java).apply {
                action = ACTION_WIDGET_TAP
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
