package com.muslim.prayerapp.data.model

enum class CalculationMethod(
    val titleArabic: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int = 0
) {
    EGYPTIAN(
        titleArabic = "الهيئة المصرية العامة للمساحة",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    UMM_AL_QURA(
        titleArabic = "جامعة أم القرى (مكة المكرمة)",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaIntervalMinutes = 90
    ),
    MUSLIM_WORLD_LEAGUE(
        titleArabic = "رابطة العالم الإسلامي",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    DUBAI(
        titleArabic = "دائرة الشؤون الإسلامية بدبي (الإمارات)",
        fajrAngle = 18.2,
        ishaAngle = 18.2
    ),
    KARACHI(
        titleArabic = "جامعة العلوم الإسلامية بكراتشي",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    ISNA(
        titleArabic = "الجمعية الإسلامية لأمريكا الشمالية (ISNA)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    TURKEY(
        titleArabic = "رئاسة الشؤون الدينية التركية (Diyanet)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    )
}

enum class JuristicMethod(val titleArabic: String, val shadowMultiplier: Double) {
    STANDARD("الجمهور (الشافعي / المالكي / الحنبلي)", 1.0),
    HANAFI("المذهب الحنفي", 2.0)
}
