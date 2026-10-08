package com.muslim.prayerapp.data.model

import java.util.Date

enum class PrayerType(val arabicName: String, val iconRes: String) {
    FAJR("الفجر", "ic_fajr"),
    SUNRISE("الشروق", "ic_sunrise"),
    DHUHR("الظهر", "ic_dhuhr"),
    ASR("العصر", "ic_asr"),
    MAGHRIB("المغرب", "ic_maghrib"),
    ISHA("العشاء", "ic_isha")
}

data class PrayerTime(
    val type: PrayerType,
    val timeMillis: Long,
    val formattedTime: String,
    val isNext: Boolean = false,
    val isCurrent: Boolean = false,
    val isNotificationEnabled: Boolean = true
)

data class DayPrayerSchedule(
    val date: Date,
    val hijriDateFormatted: String,
    val gregorianDateFormatted: String,
    val prayers: List<PrayerTime>,
    val nextPrayer: PrayerTime?,
    val remainingMillisUntilNext: Long
)
