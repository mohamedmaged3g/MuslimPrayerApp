package com.muslim.prayerapp.data.model

data class Surah(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val numberOfAyahs: Int,
    val revelationType: String,
    val startPage: Int
)

data class Ayah(
    val numberInSurah: Int,
    val arabicText: String
)
