package com.taplock.myapplication

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class LockWidgetMin : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Delegate tap handling to the shared logic if needed, 
        // but here we just use the same broadcast action as the main widget.
        // Actually, let's use the same tap action logic.
    }

    companion object {
        private const val ACTION_WIDGET_TAP = "com.taplock.myapplication.WIDGET_TAP"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.lock_widget_min_layout)
            
            val intent = Intent(context, LockWidget::class.java).apply {
                action = ACTION_WIDGET_TAP
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.lock_image_min, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
