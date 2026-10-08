package com.muslim.prayerapp.data.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AudioState {
    var isPlaying by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var currentTitle by mutableStateOf("")
    var currentSubtitle by mutableStateOf("")
    var isRadioMode by mutableStateOf(false)
    var currentRadioUrl by mutableStateOf("")

    fun setRadioPlaying(title: String, subtitle: String, url: String) {
        currentTitle = title
        currentSubtitle = subtitle
        currentRadioUrl = url
        isRadioMode = true
        isPlaying = true
        isPaused = false
    }

    fun setAzanPlaying(title: String, subtitle: String) {
        currentTitle = title
        currentSubtitle = subtitle
        isRadioMode = false
        isPlaying = true
        isPaused = false
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
        isPlaying = true
    }

    fun stop() {
        isPlaying = false
        isPaused = false
        currentTitle = ""
        currentSubtitle = ""
        currentRadioUrl = ""
    }
}

object AppThemeState {
    var isLightTheme by mutableStateOf(true)
}
