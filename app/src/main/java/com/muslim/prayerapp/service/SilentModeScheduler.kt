package com.muslim.prayerapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build

class SilentModeScheduler : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val action = intent.action ?: return

        when (action) {
            ACTION_ENABLE_SILENT -> {
                try {
                    // Save current ringer mode
                    val prefs = context.getSharedPreferences("pref_silent_mode", Context.MODE_PRIVATE)
                    prefs.edit().putInt("previous_ringer_mode", audioManager.ringerMode).apply()

                    audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            ACTION_RESTORE_RINGER -> {
                try {
                    val prefs = context.getSharedPreferences("pref_silent_mode", Context.MODE_PRIVATE)
                    val prevMode = prefs.getInt("previous_ringer_mode", AudioManager.RINGER_MODE_NORMAL)
                    audioManager.ringerMode = prevMode
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val ACTION_ENABLE_SILENT = "com.muslim.prayerapp.ACTION_ENABLE_SILENT"
        const val ACTION_RESTORE_RINGER = "com.muslim.prayerapp.ACTION_RESTORE_RINGER"

        fun scheduleAutoSilent(context: Context, durationMinutes: Int = 20) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val silentIntent = Intent(context, SilentModeScheduler::class.java).apply {
                action = ACTION_ENABLE_SILENT
            }
            val silentPendingIntent = PendingIntent.getBroadcast(
                context, 3001, silentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val restoreIntent = Intent(context, SilentModeScheduler::class.java).apply {
                action = ACTION_RESTORE_RINGER
            }
            val restorePendingIntent = PendingIntent.getBroadcast(
                context, 3002, restoreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val now = System.currentTimeMillis()
            val restoreTime = now + (durationMinutes * 60 * 1000L)

            // Trigger silent now
            context.sendBroadcast(silentIntent)

            // Schedule restore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, restoreTime, restorePendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, restoreTime, restorePendingIntent)
            }
        }
    }
}
