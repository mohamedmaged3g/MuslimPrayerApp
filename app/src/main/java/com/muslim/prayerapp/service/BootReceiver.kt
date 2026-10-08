package com.muslim.prayerapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.muslim.prayerapp.widget.PrayerTimesWidget

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            // Re-schedule all prayers & azkar reminders after phone restart
            AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)
            AzkarReminderScheduler.scheduleReminders(context)
            PrayerTimesWidget.updateWidget(context)
        }
    }
}
