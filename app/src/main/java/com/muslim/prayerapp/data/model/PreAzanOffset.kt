package com.muslim.prayerapp.data.model

enum class PreAzanOffset(
    val titleArabic: String,
    val minutes: Int
) {
    NONE("بدون تنبيه مسبق", 0),
    FIVE_MIN("قبل الأذان بـ 5 دقائق", 5),
    TEN_MIN("قبل الأذان بـ 10 دقائق", 10),
    FIFTEEN_MIN("قبل الأذان بـ 15 دقيقة", 15),
    TWENTY_MIN("قبل الأذان بـ 20 دقيقة", 20)
}
