package com.muslim.prayerapp.service

import android.R
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.muslim.prayerapp.MainActivity
import com.muslim.prayerapp.MuslimApplication

class AzkarReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_AZKAR_TYPE) ?: TYPE_MORNING

        val (title, content, id) = when (type) {
            TYPE_MORNING -> Triple("أذكار الصباح ☀️", "أصبحنا وأصبح الملك لله.. حان وقت قراءة أذكار الصباح والتحصين", 3001)
            TYPE_EVENING -> Triple("أذكار المساء 🌙", "أمسينا وأمسى الملك لله.. حان وقت قراءة أذكار المساء", 3002)
            else -> Triple("أذكار النوم 💤", "باسمك اللهم أموت وأحيا.. أذكار النوم والمغفرة", 3003)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            context,
            id,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MuslimApplication.AZAN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingOpenApp)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    companion object {
        const val ACTION_AZKAR_REMINDER = "com.muslim.prayerapp.ACTION_AZKAR_REMINDER"
        const val EXTRA_AZKAR_TYPE = "EXTRA_AZKAR_TYPE"
        const val TYPE_MORNING = "TYPE_MORNING"
        const val TYPE_EVENING = "TYPE_EVENING"
        const val TYPE_SLEEP = "TYPE_SLEEP"
    }
}
