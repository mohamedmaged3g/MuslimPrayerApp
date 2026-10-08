package com.muslim.prayerapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.muslim.prayerapp.data.repository.SettingsRepository
import java.util.Calendar

object AzkarReminderScheduler {

    fun scheduleReminders(context: Context) {
        val settings = SettingsRepository(context)

        if (settings.isMorningAzkarReminderEnabled()) {
            scheduleDailyAlarm(context, AzkarReminderReceiver.TYPE_MORNING, 7, 0)
        }
        if (settings.isEveningAzkarReminderEnabled()) {
            scheduleDailyAlarm(context, AzkarReminderReceiver.TYPE_EVENING, 17, 0)
        }
        if (settings.isSleepAzkarReminderEnabled()) {
            scheduleDailyAlarm(context, AzkarReminderReceiver.TYPE_SLEEP, 22, 0)
        }
    }

    private fun scheduleDailyAlarm(context: Context, type: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AzkarReminderReceiver::class.java).apply {
            action = AzkarReminderReceiver.ACTION_AZKAR_REMINDER
            putExtra(AzkarReminderReceiver.EXTRA_AZKAR_TYPE, type)
        }

        val requestCode = when (type) {
            AzkarReminderReceiver.TYPE_MORNING -> 5001
            AzkarReminderReceiver.TYPE_EVENING -> 5002
            else -> 5003
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
