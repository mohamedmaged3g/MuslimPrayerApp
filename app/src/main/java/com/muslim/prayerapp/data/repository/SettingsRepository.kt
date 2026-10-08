package com.muslim.prayerapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.muslim.prayerapp.data.model.AzanSoundMode
import com.muslim.prayerapp.data.model.CalculationMethod
import com.muslim.prayerapp.data.model.JuristicMethod
import com.muslim.prayerapp.data.model.MoazenVoice
import com.muslim.prayerapp.data.model.PrayerType
import com.muslim.prayerapp.data.model.PreAzanOffset

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("muslim_app_prefs", Context.MODE_PRIVATE)

    // Calculation Method
    fun getCalculationMethod(): CalculationMethod {
        val name = prefs.getString(KEY_CALC_METHOD, CalculationMethod.EGYPTIAN.name) ?: CalculationMethod.EGYPTIAN.name
        return try {
            CalculationMethod.valueOf(name)
        } catch (e: Exception) {
            CalculationMethod.EGYPTIAN
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString(KEY_CALC_METHOD, method.name).apply()
    }

    // Juristic Method (Asr)
    fun getJuristicMethod(): JuristicMethod {
        val name = prefs.getString(KEY_JURISTIC_METHOD, JuristicMethod.STANDARD.name) ?: JuristicMethod.STANDARD.name
        return try {
            JuristicMethod.valueOf(name)
        } catch (e: Exception) {
            JuristicMethod.STANDARD
        }
    }

    fun setJuristicMethod(method: JuristicMethod) {
        prefs.edit().putString(KEY_JURISTIC_METHOD, method.name).apply()
    }

    // Location (Default: Cairo, Egypt: 30.0444, 31.2357)
    fun getLatitude(): Double = prefs.getFloat(KEY_LAT, 30.0444f).toDouble()
    fun getLongitude(): Double = prefs.getFloat(KEY_LNG, 31.2357f).toDouble()
    fun getCityName(): String = prefs.getString(KEY_CITY, "القاهرة، مصر") ?: "القاهرة، مصر"

    fun setLocation(lat: Double, lng: Double, cityName: String) {
        prefs.edit()
            .putFloat(KEY_LAT, lat.toFloat())
            .putFloat(KEY_LNG, lng.toFloat())
            .putString(KEY_CITY, cityName)
            .apply()
    }

    // Azan Notifications per Prayer
    fun isPrayerNotificationEnabled(prayerType: PrayerType): Boolean {
        return prefs.getBoolean("notif_${prayerType.name}", true)
    }

    fun setPrayerNotificationEnabled(prayerType: PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean("notif_${prayerType.name}", enabled).apply()
    }

    // Manual Minute Offsets Per Prayer (+/- minutes)
    fun getPrayerMinuteOffset(prayerType: PrayerType): Int {
        return prefs.getInt("offset_${prayerType.name}", 0)
    }

    fun setPrayerMinuteOffset(prayerType: PrayerType, offset: Int) {
        prefs.edit().putInt("offset_${prayerType.name}", offset).apply()
    }

    fun getAllPrayerOffsets(): Map<PrayerType, Int> {
        return PrayerType.entries.associateWith { getPrayerMinuteOffset(it) }
    }

    // Moazen Voice Preference
    fun getMoazenVoice(): MoazenVoice {
        val name = prefs.getString(KEY_MOAZEN_VOICE, MoazenVoice.MAKKAH.name) ?: MoazenVoice.MAKKAH.name
        return try {
            MoazenVoice.valueOf(name)
        } catch (e: Exception) {
            MoazenVoice.MAKKAH
        }
    }

    fun setMoazenVoice(voice: MoazenVoice) {
        prefs.edit().putString(KEY_MOAZEN_VOICE, voice.name).apply()
    }

    // Pre-Azan Notification Offset
    fun getPreAzanOffset(): PreAzanOffset {
        val name = prefs.getString(KEY_PRE_AZAN_OFFSET, PreAzanOffset.NONE.name) ?: PreAzanOffset.NONE.name
        return try {
            PreAzanOffset.valueOf(name)
        } catch (e: Exception) {
            PreAzanOffset.NONE
        }
    }

    fun setPreAzanOffset(offset: PreAzanOffset) {
        prefs.edit().putString(KEY_PRE_AZAN_OFFSET, offset.name).apply()
    }

    // Smart Azkar Reminders
    fun isMorningAzkarReminderEnabled(): Boolean = prefs.getBoolean(KEY_AZKAR_MORNING_REMINDER, true)
    fun setMorningAzkarReminderEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AZKAR_MORNING_REMINDER, enabled).apply()

    fun isEveningAzkarReminderEnabled(): Boolean = prefs.getBoolean(KEY_AZKAR_EVENING_REMINDER, true)
    fun setEveningAzkarReminderEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AZKAR_EVENING_REMINDER, enabled).apply()

    fun isSleepAzkarReminderEnabled(): Boolean = prefs.getBoolean(KEY_AZKAR_SLEEP_REMINDER, true)
    fun setSleepAzkarReminderEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AZKAR_SLEEP_REMINDER, enabled).apply()

    // Azkar Font Size (1: Small/17sp, 2: Medium/20sp, 3: Large/24sp)
    fun getAzkarFontSizeScale(): Int = prefs.getInt(KEY_AZKAR_FONT_SCALE, 2)
    fun setAzkarFontSizeScale(scale: Int) = prefs.edit().putInt(KEY_AZKAR_FONT_SCALE, scale).apply()

    // Global Azan Sound Mode (FULL_AZAN, TAKBEER_ONLY, NOTIFICATION_ONLY)
    fun getGlobalAzanSoundMode(): AzanSoundMode {
        val name = prefs.getString(KEY_GLOBAL_AZAN_MODE, AzanSoundMode.FULL_AZAN.name) ?: AzanSoundMode.FULL_AZAN.name
        return try {
            AzanSoundMode.valueOf(name)
        } catch (e: Exception) {
            AzanSoundMode.FULL_AZAN
        }
    }

    fun setGlobalAzanSoundMode(mode: AzanSoundMode) {
        prefs.edit().putString(KEY_GLOBAL_AZAN_MODE, mode.name).apply()
    }

    // Custom Prayer Azan Sound Mode Toggle
    fun isPrayerCustomSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_CUSTOM_PRAYER_AZAN_ENABLED, false)
    }

    fun setPrayerCustomSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CUSTOM_PRAYER_AZAN_ENABLED, enabled).apply()
    }

    // Azan Sound Mode for a Specific Prayer (with fallback to global mode)
    fun getPrayerAzanSoundMode(prayerType: PrayerType): AzanSoundMode {
        if (!isPrayerCustomSoundEnabled()) {
            return getGlobalAzanSoundMode()
        }
        val name = prefs.getString("azan_sound_mode_${prayerType.name}", null) ?: return getGlobalAzanSoundMode()
        return try {
            AzanSoundMode.valueOf(name)
        } catch (e: Exception) {
            getGlobalAzanSoundMode()
        }
    }

    fun setPrayerAzanSoundMode(prayerType: PrayerType, mode: AzanSoundMode) {
        prefs.edit().putString("azan_sound_mode_${prayerType.name}", mode.name).apply()
    }

    // Theme Mode (Default: Light/Bright theme as requested)
    fun isLightTheme(): Boolean = prefs.getBoolean(KEY_LIGHT_THEME, true)
    fun setLightTheme(isLight: Boolean) = prefs.edit().putBoolean(KEY_LIGHT_THEME, isLight).apply()

    // Tasbeeh preferences
    fun isTasbeehVibrationEnabled(): Boolean = prefs.getBoolean(KEY_TASBEEH_VIBRATE, true)
    fun setTasbeehVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_TASBEEH_VIBRATE, enabled).apply()

    fun getTasbeehTotalCount(): Int = prefs.getInt(KEY_TASBEEH_TOTAL, 0)
    fun saveTasbeehTotalCount(total: Int) = prefs.edit().putInt(KEY_TASBEEH_TOTAL, total).apply()

    fun getCustomPraises(): List<String> {
        val stringListJson = prefs.getString(KEY_CUSTOM_PRAISES, null) ?: return emptyList()
        return stringListJson.split("|||").filter { it.isNotBlank() }
    }

    fun addCustomPraise(praise: String) {
        val trimmed = praise.trim()
        if (trimmed.isBlank()) return
        val current = getCustomPraises().toMutableList()
        if (!current.contains(trimmed)) {
            current.add(trimmed)
            prefs.edit().putString(KEY_CUSTOM_PRAISES, current.joinToString("|||")).apply()
        }
    }

    fun removeCustomPraise(praise: String) {
        val current = getCustomPraises().toMutableList()
        current.remove(praise)
        prefs.edit().putString(KEY_CUSTOM_PRAISES, current.joinToString("|||")).apply()
    }

    // Auto Silent Mode Preferences
    fun isAutoSilentEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SILENT, false)
    fun setAutoSilentEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_SILENT, enabled).apply()

    fun getSilentDurationMinutes(): Int = prefs.getInt(KEY_SILENT_DURATION, 20)
    fun setSilentDurationMinutes(minutes: Int) = prefs.edit().putInt(KEY_SILENT_DURATION, minutes).apply()

    // Favorite Azkar Preferences
    fun getFavoriteAzkarIds(): Set<String> = prefs.getStringSet(KEY_FAVORITE_AZKAR, emptySet()) ?: emptySet()
    fun toggleFavoriteAzkar(azkarId: String) {
        val current = getFavoriteAzkarIds().toMutableSet()
        if (current.contains(azkarId)) {
            current.remove(azkarId)
        } else {
            current.add(azkarId)
        }
        prefs.edit().putStringSet(KEY_FAVORITE_AZKAR, current).apply()
    }

    // Custom Azan File Preferences
    fun getCustomAzanFilePath(): String? = prefs.getString(KEY_CUSTOM_AZAN_FILE_PATH, null)
    fun getCustomAzanTitle(): String? = prefs.getString(KEY_CUSTOM_AZAN_TITLE, null)

    fun setCustomAzanFile(filePath: String, title: String) {
        prefs.edit()
            .putString(KEY_CUSTOM_AZAN_FILE_PATH, filePath)
            .putString(KEY_CUSTOM_AZAN_TITLE, title)
            .apply()
    }

    fun clearCustomAzanFile() {
        prefs.edit()
            .remove(KEY_CUSTOM_AZAN_FILE_PATH)
            .remove(KEY_CUSTOM_AZAN_TITLE)
            .apply()
    }

    companion object {
        private const val KEY_CALC_METHOD = "pref_calc_method"
        private const val KEY_JURISTIC_METHOD = "pref_juristic_method"
        private const val KEY_LAT = "pref_lat"
        private const val KEY_LNG = "pref_lng"
        private const val KEY_CITY = "pref_city"
        private const val KEY_MOAZEN_VOICE = "pref_moazen_voice"
        private const val KEY_PRE_AZAN_OFFSET = "pref_pre_azan_offset"
        private const val KEY_AZKAR_MORNING_REMINDER = "pref_azkar_morning_reminder"
        private const val KEY_AZKAR_EVENING_REMINDER = "pref_azkar_evening_reminder"
        private const val KEY_AZKAR_SLEEP_REMINDER = "pref_azkar_sleep_reminder"
        private const val KEY_AZKAR_FONT_SCALE = "pref_azkar_font_scale"
        private const val KEY_GLOBAL_AZAN_MODE = "pref_global_azan_mode"
        private const val KEY_CUSTOM_PRAYER_AZAN_ENABLED = "pref_custom_prayer_azan_enabled"
        private const val KEY_LIGHT_THEME = "pref_light_theme"
        private const val KEY_TASBEEH_VIBRATE = "pref_tasbeeh_vibrate"
        private const val KEY_TASBEEH_TOTAL = "pref_tasbeeh_total"
        private const val KEY_CUSTOM_PRAISES = "pref_custom_praises"
        private const val KEY_AUTO_SILENT = "pref_auto_silent"
        private const val KEY_SILENT_DURATION = "pref_silent_duration"
        private const val KEY_FAVORITE_AZKAR = "pref_favorite_azkar"
        private const val KEY_CUSTOM_AZAN_FILE_PATH = "pref_custom_azan_file_path"
        private const val KEY_CUSTOM_AZAN_TITLE = "pref_custom_azan_title"
    }
}
