package com.muslim.prayerapp.data.model

enum class AzkarCategory(val titleArabic: String, val badgeColorHex: String) {
    MORNING("أذكار الصباح", "#10B981"),
    EVENING("أذكار المساء", "#6366F1"),
    SLEEP("أذكار النوم", "#8B5CF6"),
    WAKE_UP("أذكار الاستيقاظ", "#F59E0B"),
    AFTER_PRAYER("أذكار بعد الصلاة", "#06B6D4"),
    QURANIC_DUA("أدعية قرآنية", "#EC4899"),
    DAILY_TASBEEH("تسابيح واستغفار", "#D4AF37")
}

data class AzkarItem(
    val id: Int,
    val category: AzkarCategory,
    val arabicText: String,
    val targetCount: Int,
    var currentCount: Int = 0,
    val benefit: String = "",
    val reference: String = "",
    val isFavorite: Boolean = false
) {
    val isCompleted: Boolean
        get() = currentCount >= targetCount
}
