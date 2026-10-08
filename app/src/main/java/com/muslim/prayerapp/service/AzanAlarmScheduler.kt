package com.muslim.prayerapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.muslim.prayerapp.data.calculator.PrayerTimesCalculator
import com.muslim.prayerapp.data.model.PrayerType
import com.muslim.prayerapp.data.model.PreAzanOffset
import com.muslim.prayerapp.data.repository.SettingsRepository
import java.util.Calendar

object AzanAlarmScheduler {

    fun scheduleAllUpcomingPrayers(context: Context) {
        val settings = SettingsRepository(context)
        val coords = PrayerTimesCalculator.Coordinates(settings.getLatitude(), settings.getLongitude())
        val method = settings.getCalculationMethod()
        val juristic = settings.getJuristicMethod()
        val offsets = settings.getAllPrayerOffsets()
        val preOffset = settings.getPreAzanOffset()

        val todaySchedule = PrayerTimesCalculator.calculateDailySchedule(coords, Calendar.getInstance(), method, juristic, offsets)
        val now = System.currentTimeMillis()

        for (prayer in todaySchedule.prayers) {
            // Do not alarm for Sunrise
            if (prayer.type == PrayerType.SUNRISE) continue

            if (settings.isPrayerNotificationEnabled(prayer.type)) {
                if (prayer.timeMillis > now) {
                    scheduleExactPrayerAlarm(context, prayer.type, prayer.timeMillis, false)
                }

                // Schedule Pre-Azan alert if enabled
                if (preOffset != PreAzanOffset.NONE) {
                    val preTimeMillis = prayer.timeMillis - (preOffset.minutes * 60 * 1000L)
                    if (preTimeMillis > now) {
                        scheduleExactPrayerAlarm(context, prayer.type, preTimeMillis, true)
                    }
                }
            }
        }

        // Also schedule tomorrow's Fajr
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowSchedule = PrayerTimesCalculator.calculateDailySchedule(coords, tomorrowCal, method, juristic, offsets)
        val tomorrowFajr = tomorrowSchedule.prayers.first { it.type == PrayerType.FAJR }
        if (settings.isPrayerNotificationEnabled(PrayerType.FAJR)) {
            scheduleExactPrayerAlarm(context, PrayerType.FAJR, tomorrowFajr.timeMillis, false)
        }
    }

    private fun scheduleExactPrayerAlarm(
        context: Context,
        prayerType: PrayerType,
        triggerAtMillis: Long,
        isPreAlarm: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AzanAlarmReceiver::class.java).apply {
            action = AzanAlarmReceiver.ACTION_AZAN_ALARM
            putExtra(AzanAlarmReceiver.EXTRA_PRAYER_TYPE, prayerType.name)
            putExtra(AzanAlarmReceiver.EXTRA_IS_PRE_ALARM, isPreAlarm)
        }

        val requestCode = (prayerType.ordinal * 10) + (if (isPreAlarm) 1 else 0)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

