package com.taplock.myapplication

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class LockWidgetText : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val ACTION_WIDGET_TAP = "com.taplock.myapplication.WIDGET_TAP"

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.lock_widget_text_layout)
            
            val intent = Intent(context, LockWidget::class.java).apply {
                action = ACTION_WIDGET_TAP
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 2000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.lock_text, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
