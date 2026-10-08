package com.muslim.prayerapp.data.model

import com.muslim.prayerapp.R

enum class AzanSoundMode(
    val titleArabic: String,
    val descriptionArabic: String,
    val defaultRawResId: Int?
) {
    FULL_AZAN(
        titleArabic = "أذان كامل",
        descriptionArabic = "صوت الأذان المبارك كاملاً عند دخول وقت الصلاة",
        defaultRawResId = R.raw.azan_makkah
    ),
    TAKBEER_ONLY(
        titleArabic = "تكبيرات فقط",
        descriptionArabic = "التكبيرات الأولى فقط (الله أكبر.. الله أكبر)",
        defaultRawResId = R.raw.azan_takbeer
    ),
    NOTIFICATION_ONLY(
        titleArabic = "تنبيه هادئ (بدون صوت أذان)",
        descriptionArabic = "إشعار هادئ بنغمة الهاتف بدون رفع صوت الأذان",
        defaultRawResId = null
    )
}
