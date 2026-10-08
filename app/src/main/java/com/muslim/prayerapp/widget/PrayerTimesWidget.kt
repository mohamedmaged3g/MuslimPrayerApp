package com.muslim.prayerapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.muslim.prayerapp.MainActivity
import com.muslim.prayerapp.R
import com.muslim.prayerapp.data.calculator.PrayerTimesCalculator
import com.muslim.prayerapp.data.repository.SettingsRepository
import java.util.Calendar

class PrayerTimesWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val settings = SettingsRepository(context)
            val coords = PrayerTimesCalculator.Coordinates(settings.getLatitude(), settings.getLongitude())
            val method = settings.getCalculationMethod()
            val juristic = settings.getJuristicMethod()
            val offsets = settings.getAllPrayerOffsets()

            val schedule = PrayerTimesCalculator.calculateDailySchedule(coords, Calendar.getInstance(), method, juristic, offsets)
            val nextPrayer = schedule.nextPrayer ?: schedule.prayers.first()

            val views = RemoteViews(context.packageName, R.layout.widget_prayer_times)
            views.setTextViewText(R.id.widget_city_name, settings.getCityName())
            views.setTextViewText(R.id.widget_date, schedule.hijriDateFormatted)
            views.setTextViewText(R.id.widget_next_prayer_name, nextPrayer.type.arabicName)
            views.setTextViewText(R.id.widget_next_prayer_time, nextPrayer.formattedTime)

            // Open MainActivity on click
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, PrayerTimesWidget::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }
}
