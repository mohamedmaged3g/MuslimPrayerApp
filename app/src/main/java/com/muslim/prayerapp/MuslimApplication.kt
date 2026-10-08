package com.muslim.prayerapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build

class MuslimApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Azan Notification Channel (High Importance with Sound)
            val azanChannel = NotificationChannel(
                AZAN_CHANNEL_ID,
                "تنبيهات أذان الصلوات",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة تنبيهات موعد الأذان وإقامة الصلاة بصوت الأذان"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            // General Azkar / Reminder Channel
            val azkarChannel = NotificationChannel(
                AZKAR_CHANNEL_ID,
                "تذكيرات الأذكار اليومية",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات تذكير بأذكار الصباح والمساء والسنن"
                enableVibration(true)
            }

            // Radio Player Notification Channel
            val radioChannel = NotificationChannel(
                RADIO_CHANNEL_ID,
                "مشغل إذاعة القرآن الكريم",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار التحكم في تشغيل البث المباشر لإذاعة القرآن"
            }

            notificationManager.createNotificationChannels(listOf(azanChannel, azkarChannel, radioChannel))
        }
    }

    companion object {
        const val AZAN_CHANNEL_ID = "azan_notification_channel"
        const val AZKAR_CHANNEL_ID = "azkar_notification_channel"
        const val RADIO_CHANNEL_ID = "radio_player_channel"
    }
}
