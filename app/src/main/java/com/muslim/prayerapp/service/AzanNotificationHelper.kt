package com.muslim.prayerapp.service

import android.R
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.muslim.prayerapp.MainActivity
import com.muslim.prayerapp.MuslimApplication
import com.muslim.prayerapp.data.model.AzanSoundMode
import com.muslim.prayerapp.data.model.PrayerType

object AzanNotificationHelper {

    fun buildAzanNotification(
        context: Context,
        prayerType: PrayerType,
        soundMode: AzanSoundMode = AzanSoundMode.FULL_AZAN,
        isAudioPlaying: Boolean = true
    ): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Azan Intent Action
        val stopIntent = Intent(context, AzanAudioService::class.java).apply {
            action = AzanAudioService.ACTION_STOP_AZAN
        }
        val pendingStop = PendingIntent.getService(
            context,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val modeSubtitle = when (soundMode) {
            AzanSoundMode.FULL_AZAN -> "حي على الصلاة.. حي على الفلاح"
            AzanSoundMode.TAKBEER_ONLY -> "الله أكبر.. الله أكبر"
            AzanSoundMode.NOTIFICATION_ONLY -> "حان الآن موعد الصلاة"
        }

        val builder = NotificationCompat.Builder(context, MuslimApplication.AZAN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("حان الآن موعد أذان ${prayerType.arabicName}")
            .setContentText(modeSubtitle)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingOpenApp)
            .setOngoing(isAudioPlaying && soundMode != AzanSoundMode.NOTIFICATION_ONLY)

        if (soundMode != AzanSoundMode.NOTIFICATION_ONLY) {
            builder.setSound(null)
        }

        if (isAudioPlaying && soundMode != AzanSoundMode.NOTIFICATION_ONLY) {
            builder.addAction(R.drawable.ic_media_pause, "إيقاف الصوت", pendingStop)
        }

        return builder.build()
    }
}
