package com.muslim.prayerapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val titleArabic: String, val icon: ImageVector) {
    object PrayerTimes : Screen("prayer_times", "المواقيت", Icons.Filled.AccessTime)
    object Quran : Screen("quran", "القرآن", Icons.Filled.Book)
    object Azkar : Screen("azkar", "الأذكار", Icons.Filled.MenuBook)
    object Tasbeeh : Screen("tasbeeh", "السبحة", Icons.Filled.AutoAwesome)
    object Qibla : Screen("qibla", "القبلة", Icons.Filled.Explore)
    object Radio : Screen("radio", "الإذاعة", Icons.Filled.Radio)
    object Settings : Screen("settings", "الإعدادات", Icons.Filled.Settings)

    companion object {
        val navItems = listOf(PrayerTimes, Quran, Azkar, Tasbeeh, Qibla, Radio, Settings)
    }
}
