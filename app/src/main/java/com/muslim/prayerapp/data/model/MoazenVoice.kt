package com.muslim.prayerapp.data.model

import com.muslim.prayerapp.R

enum class MoazenVoice(
    val titleArabic: String,
    val descriptionArabic: String,
    val rawResId: Int,
    val fajrRawResId: Int
) {
    MAKKAH(
        titleArabic = "أذان الحرم المكي الشريف",
        descriptionArabic = "بصوت مؤذني المسجد الحرام بمكة المكرمة",
        rawResId = R.raw.azan_makkah,
        fajrRawResId = R.raw.azan_fajr
    ),
    MADINAH(
        titleArabic = "أذان الحرم المدني الشريف",
        descriptionArabic = "بصوت مؤذني المسجد النبوي الشريف بالمدينة المنورة",
        rawResId = R.raw.azan_madinah,
        fajrRawResId = R.raw.azan_fajr
    ),
    MENSHAWY(
        titleArabic = "أذان الشيخ محمد صديق المنشاوي",
        descriptionArabic = "الأذان المبارك بصوت القارئ الشيخ محمد صديق المنشاوي",
        rawResId = R.raw.azan_menshawy,
        fajrRawResId = R.raw.azan_fajr
    ),
    ABDELBASET(
        titleArabic = "أذان الشيخ عبد الباسط عبد الصمد",
        descriptionArabic = "الأذان الخاشع بصوت كروان القراء الشيخ عبد الباسط عبد الصمد",
        rawResId = R.raw.azan_abdelbaset,
        fajrRawResId = R.raw.azan_fajr
    ),
    ALAFASY(
        titleArabic = "أذان الشيخ مشاري العفاسي",
        descriptionArabic = "الأذان المتميز بصوت الشيخ مشاري بن راشد العفاسي",
        rawResId = R.raw.azan_alafasy,
        fajrRawResId = R.raw.azan_fajr
    ),
    AL_AQSA(
        titleArabic = "أذان المسجد الأقصى المبارك",
        descriptionArabic = "الأذان المبارك من رحاب المسجد الأقصى الشريف",
        rawResId = R.raw.azan_makkah,
        fajrRawResId = R.raw.azan_fajr
    );

    companion object {
        fun getRawResId(voice: MoazenVoice, isFajr: Boolean): Int {
            return if (isFajr) voice.fajrRawResId else voice.rawResId
        }
    }
}
