package com.muslim.prayerapp.data.model

data class IslamicEvent(
    val titleArabic: String,
    val descriptionArabic: String,
    val hijriDay: Int,
    val hijriMonth: Int, // 1 to 12
    val hijriMonthNameArabic: String,
    val iconName: String = "Event"
)

data class HijriDate(
    val day: Int,
    val month: Int,
    val monthNameArabic: String,
    val year: Int
) {
    fun formatArabic(): String = "$day $monthNameArabic $year هـ"
}
