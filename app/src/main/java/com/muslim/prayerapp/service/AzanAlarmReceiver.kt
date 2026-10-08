package com.muslim.prayerapp.service

import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.muslim.prayerapp.MuslimApplication
import com.muslim.prayerapp.data.model.PrayerType
import com.muslim.prayerapp.data.repository.SettingsRepository

class AzanAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: return
        val prayerType = try {
            PrayerType.valueOf(prayerTypeName)
        } catch (e: Exception) {
            return
        }

        val isPreAlarm = intent.getBooleanExtra(EXTRA_IS_PRE_ALARM, false)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MuslimPrayerApp:AzanAlarmWakeLock"
        )
        wakeLock.acquire(10_000L)

        try {
            val settings = SettingsRepository(context)
            if (settings.isPrayerNotificationEnabled(prayerType)) {
                if (isPreAlarm) {
                    showPreAzanNotification(context, prayerType)
                } else {
                    AzanAudioService.startAzan(context, prayerType)
                    if (settings.isAutoSilentEnabled()) {
                        SilentModeScheduler.scheduleAutoSilent(context, settings.getSilentDurationMinutes())
                    }
                }
            }

            AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    private fun showPreAzanNotification(context: Context, prayerType: PrayerType) {
        val settings = SettingsRepository(context)
        val minutes = settings.getPreAzanOffset().minutes

        val notification = NotificationCompat.Builder(context, MuslimApplication.AZAN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("اقترب موعد أذان ${prayerType.arabicName}")
            .setContentText("المتبقي $minutes دقائق على رفع أذان ${prayerType.arabicName}.. استعد للصلاة")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(2000 + prayerType.ordinal, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val ACTION_AZAN_ALARM = "com.muslim.prayerapp.ACTION_AZAN_ALARM"
        const val EXTRA_PRAYER_TYPE = "EXTRA_PRAYER_TYPE"
        const val EXTRA_IS_PRE_ALARM = "EXTRA_IS_PRE_ALARM"
    }
}

