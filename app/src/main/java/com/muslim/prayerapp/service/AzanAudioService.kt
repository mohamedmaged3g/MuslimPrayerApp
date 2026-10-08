package com.muslim.prayerapp.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import java.io.File
import com.muslim.prayerapp.MainActivity
import com.muslim.prayerapp.MuslimApplication
import com.muslim.prayerapp.R
import com.muslim.prayerapp.data.model.AudioState
import com.muslim.prayerapp.data.model.AzanSoundMode
import com.muslim.prayerapp.data.model.MoazenVoice
import com.muslim.prayerapp.data.model.PrayerType
import com.muslim.prayerapp.data.repository.SettingsRepository

class AzanAudioService : Service(), AudioManager.OnAudioFocusChangeListener {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlayingRadio = false
    private var audioManager: AudioManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as? AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_AZAN -> {
                val prayerType = try {
                    PrayerType.valueOf(intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: PrayerType.FAJR.name)
                } catch (e: Exception) {
                    PrayerType.FAJR
                }
                playAzan(prayerType)
            }
            ACTION_PREVIEW_AZAN -> {
                val modeName = intent.getStringExtra(EXTRA_AZAN_MODE) ?: AzanSoundMode.FULL_AZAN.name
                val soundMode = try {
                    AzanSoundMode.valueOf(modeName)
                } catch (e: Exception) {
                    AzanSoundMode.FULL_AZAN
                }
                val prayerTypeName = intent.getStringExtra(EXTRA_PRAYER_TYPE) ?: PrayerType.FAJR.name
                val prayerType = try {
                    PrayerType.valueOf(prayerTypeName)
                } catch (e: Exception) {
                    PrayerType.FAJR
                }
                previewAzanSound(prayerType, soundMode)
            }
            ACTION_STOP_AZAN -> {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_PLAY_RADIO -> {
                val streamUrl = intent.getStringExtra(EXTRA_RADIO_URL) ?: ""
                val stationTitle = intent.getStringExtra(EXTRA_RADIO_TITLE) ?: "إذاعة القرآن الكريم"
                val stationSubtitle = intent.getStringExtra(EXTRA_RADIO_SUBTITLE) ?: "البث المباشر لإذاعة القرآن الكريم"
                playRadio(streamUrl, stationTitle, stationSubtitle)
            }
            ACTION_STOP_RADIO, ACTION_STOP_AZAN, ACTION_STOP_AUDIO -> {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_PAUSE_AUDIO -> {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.pause()
                        AudioState.pause()
                    }
                }
            }
            ACTION_RESUME_AUDIO -> {
                mediaPlayer?.let {
                    it.start()
                    AudioState.resume()
                }
            }
            ACTION_SEEK_RELATIVE -> {
                val offsetMs = intent.getIntExtra(EXTRA_SEEK_OFFSET, 10_000)
                mediaPlayer?.let {
                    val target = (it.currentPosition + offsetMs).coerceIn(0, it.duration)
                    it.seekTo(target)
                }
            }
        }
        return START_STICKY
    }

    private fun requestAudioFocus(): Boolean {
        val result = audioManager?.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    AudioState.isPlaying = false
                }
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                    mediaPlayer?.start()
                    AudioState.isPlaying = true
                }
            }
        }
    }

    private fun playAzan(prayerType: PrayerType) {
        stopPlayback()
        isPlayingRadio = false
        requestAudioFocus()

        val settingsRepo = SettingsRepository(this)
        val soundMode = settingsRepo.getPrayerAzanSoundMode(prayerType)
        val moazenVoice = settingsRepo.getMoazenVoice()

        val notification = AzanNotificationHelper.buildAzanNotification(this, prayerType, soundMode, true)
        startForeground(NOTIFICATION_ID_AZAN, notification)

        val customPath = settingsRepo.getCustomAzanFilePath()
        val customTitle = settingsRepo.getCustomAzanTitle()

        if (soundMode == AzanSoundMode.FULL_AZAN && !customPath.isNullOrBlank() && File(customPath).exists()) {
            AudioState.setAzanPlaying("أذان ${prayerType.arabicName}", customTitle ?: "أذان مخصص")
            playLocalFilePath(customPath, isAlarmUsage = true)
            return
        }

        AudioState.setAzanPlaying("أذان ${prayerType.arabicName}", "حي على الصلاة.. حي على الفلاح")

        if (soundMode == AzanSoundMode.NOTIFICATION_ONLY) {
            return
        }

        val isFajr = (prayerType == PrayerType.FAJR)
        val rawResId = if (soundMode == AzanSoundMode.TAKBEER_ONLY) {
            R.raw.azan_takbeer
        } else {
            MoazenVoice.getRawResId(moazenVoice, isFajr)
        }

        playLocalRawAudio(rawResId, isAlarmUsage = true)
    }

    private fun previewAzanSound(prayerType: PrayerType, soundMode: AzanSoundMode) {
        stopPlayback()
        isPlayingRadio = false
        requestAudioFocus()

        if (soundMode == AzanSoundMode.NOTIFICATION_ONLY) {
            stopSelf()
            return
        }

        val settingsRepo = SettingsRepository(this)
        val moazenVoice = settingsRepo.getMoazenVoice()

        val customPath = settingsRepo.getCustomAzanFilePath()
        val customTitle = settingsRepo.getCustomAzanTitle()

        val notification = AzanNotificationHelper.buildAzanNotification(this, prayerType, soundMode, true)
        startForeground(NOTIFICATION_ID_AZAN, notification)

        if (soundMode == AzanSoundMode.FULL_AZAN && !customPath.isNullOrBlank() && File(customPath).exists()) {
            AudioState.setAzanPlaying("معاينة أذان ${prayerType.arabicName}", customTitle ?: "أذان مخصص")
            playLocalFilePath(customPath, isAlarmUsage = false)
            return
        }

        val isFajr = (prayerType == PrayerType.FAJR)
        val rawResId = if (soundMode == AzanSoundMode.TAKBEER_ONLY) {
            R.raw.azan_takbeer
        } else {
            MoazenVoice.getRawResId(moazenVoice, isFajr)
        }

        AudioState.setAzanPlaying("معاينة أذان ${prayerType.arabicName}", "صوت ${moazenVoice.titleArabic}")

        playLocalRawAudio(rawResId, isAlarmUsage = false)
    }

    private fun playLocalFilePath(filePath: String, isAlarmUsage: Boolean) {
        try {
            val usage = if (isAlarmUsage) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(usage)
                        .build()
                )
                setDataSource(filePath)
                setOnPreparedListener { mp -> mp.start() }
                setOnErrorListener { _, _, _ ->
                    playSystemAlarmFallback()
                    true
                }
                setOnCompletionListener {
                    AudioState.stop()
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playSystemAlarmFallback()
        }
    }

    private fun playLocalRawAudio(rawResId: Int, isAlarmUsage: Boolean) {
        try {
            val usage = if (isAlarmUsage) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_MEDIA
            val soundUri = Uri.parse("android.resource://$packageName/$rawResId")

            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(usage)
                        .build()
                )
                setDataSource(applicationContext, soundUri)
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnErrorListener { _, _, _ ->
                    playSystemAlarmFallback()
                    true
                }
                setOnCompletionListener {
                    AudioState.stop()
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playSystemAlarmFallback()
        }
    }

    private fun playSystemAlarmFallback() {
        try {
            stopPlayback()
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(applicationContext, alarmUri)
                setOnPreparedListener { mp -> mp.start() }
                setOnCompletionListener {
                    AudioState.stop()
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AudioState.stop()
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
    }

    private fun playRadio(url: String, title: String, subtitle: String = "البث المباشر لإذاعة القرآن الكريم") {
        stopPlayback()
        isPlayingRadio = true
        requestAudioFocus()

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopRadioIntent = Intent(this, AzanAudioService::class.java).apply {
            action = ACTION_STOP_RADIO
        }
        val pendingStopRadio = PendingIntent.getService(
            this,
            2,
            stopRadioIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, MuslimApplication.RADIO_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingOpenApp)
            .addAction(android.R.drawable.ic_media_pause, "إيقاف الإذاعة", pendingStopRadio)
            .build()

        startForeground(NOTIFICATION_ID_RADIO, notification)

        AudioState.setRadioPlaying(title, subtitle, url)

        try {
            mediaPlayer = MediaPlayer().apply {
                setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(applicationContext, Uri.parse(url))
                setOnPreparedListener { mp ->
                    mp.start()
                }
                setOnErrorListener { _, _, _ ->
                    AudioState.stop()
                    stopSelf()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AudioState.stop()
            stopSelf()
        }
    }

    private fun stopPlayback() {
        audioManager?.abandonAudioFocus(this)
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
        AudioState.stop()
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }

    companion object {
        const val ACTION_PLAY_AZAN = "ACTION_PLAY_AZAN"
        const val ACTION_PREVIEW_AZAN = "ACTION_PREVIEW_AZAN"
        const val ACTION_STOP_AZAN = "ACTION_STOP_AZAN"
        const val ACTION_PLAY_RADIO = "ACTION_PLAY_RADIO"
        const val ACTION_STOP_RADIO = "ACTION_STOP_RADIO"
        const val ACTION_PAUSE_AUDIO = "ACTION_PAUSE_AUDIO"
        const val ACTION_RESUME_AUDIO = "ACTION_RESUME_AUDIO"
        const val ACTION_SEEK_RELATIVE = "ACTION_SEEK_RELATIVE"
        const val ACTION_STOP_AUDIO = "ACTION_STOP_AUDIO"

        const val EXTRA_PRAYER_NAME = "EXTRA_PRAYER_NAME"
        const val EXTRA_PRAYER_TYPE = "EXTRA_PRAYER_TYPE"
        const val EXTRA_AZAN_MODE = "EXTRA_AZAN_MODE"
        const val EXTRA_RADIO_URL = "EXTRA_RADIO_URL"
        const val EXTRA_RADIO_TITLE = "EXTRA_RADIO_TITLE"
        const val EXTRA_RADIO_SUBTITLE = "EXTRA_RADIO_SUBTITLE"
        const val EXTRA_SEEK_OFFSET = "EXTRA_SEEK_OFFSET"

        private const val NOTIFICATION_ID_AZAN = 1001
        private const val NOTIFICATION_ID_RADIO = 1002

        fun startAzan(context: Context, prayerType: PrayerType) {
            val intent = Intent(context, AzanAudioService::class.java).apply {
                action = ACTION_PLAY_AZAN
                putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                putExtra(EXTRA_PRAYER_NAME, prayerType.arabicName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun previewAzanSound(context: Context, prayerType: PrayerType, mode: AzanSoundMode) {
            val intent = Intent(context, AzanAudioService::class.java).apply {
                action = ACTION_PREVIEW_AZAN
                putExtra(EXTRA_PRAYER_TYPE, prayerType.name)
                putExtra(EXTRA_AZAN_MODE, mode.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun playRadioStation(context: Context, stationTitle: String, url: String, subtitle: String = "البث المباشر لإذاعة القرآن الكريم") {
            val intent = Intent(context, AzanAudioService::class.java).apply {
                action = ACTION_PLAY_RADIO
                putExtra(EXTRA_RADIO_TITLE, stationTitle)
                putExtra(EXTRA_RADIO_URL, url)
                putExtra(EXTRA_RADIO_SUBTITLE, subtitle)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseAudio(context: Context) {
            val intent = Intent(context, AzanAudioService::class.java).apply {
                action = ACTION_PAUSE_AUDIO
            }
            context.startService(intent)
        }

        fun resumeAudio(context: Context) {
            val intent = Intent(context, AzanAudioService::class.java).apply {
                action = ACTION_RESUME_AUDIO
            }
            context.startService(intent)
        }

        fun seekRelative(context: Context, offsetMs: Int) {
            val intent = Intent(context, AzanAudioService::class.java).apply {
                action = ACTION_SEEK_RELATIVE
                putExtra(EXTRA_SEEK_OFFSET, offsetMs)
            }
            context.startService(intent)
        }

        fun stopAudio(context: Context) {
            val intent = Intent(context, AzanAudioService::class.java).apply {
                action = ACTION_STOP_AUDIO
            }
            context.startService(intent)
        }
    }
}
