package com.muslim.prayerapp.data.model

data class RadioStation(
    val id: String,
    val nameArabic: String,
    val streamUrl: String,
    val description: String,
    val country: String,
    val category: String = "القرآن الكريم"
)
