package com.muslim.prayerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muslim.prayerapp.data.model.AppThemeState
import com.muslim.prayerapp.data.repository.SettingsRepository
import com.muslim.prayerapp.service.AzanAlarmScheduler
import com.muslim.prayerapp.service.AzkarReminderScheduler
import com.muslim.prayerapp.ui.components.GlobalMiniPlayerBar
import com.muslim.prayerapp.ui.navigation.MuslimBottomNavBar
import com.muslim.prayerapp.widget.PrayerTimesWidget
import com.muslim.prayerapp.ui.navigation.Screen
import com.muslim.prayerapp.ui.screens.*
import com.muslim.prayerapp.ui.theme.MuslimPrayerAppTheme

class MainActivity : ComponentActivity() {

    // Permission launcher for runtime permissions (Android 13+ notifications & GPS)
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // When notification permission granted, ensure alarms are scheduled
        AzanAlarmScheduler.scheduleAllUpcomingPrayers(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestRequiredPermissions()
        AzanAlarmScheduler.scheduleAllUpcomingPrayers(this)
        AzkarReminderScheduler.scheduleReminders(this)
        PrayerTimesWidget.updateWidget(this)

        setContent {
            val context = LocalContext.current
            val settingsRepo = remember { SettingsRepository(context) }

            LaunchedEffect(Unit) {
                AppThemeState.isLightTheme = settingsRepo.isLightTheme()
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MuslimPrayerAppTheme(isLightTheme = AppThemeState.isLightTheme) {
                    MainAppScaffold()
                }
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val notGranted = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            requestPermissionsLauncher.launch(notGranted.toTypedArray())
        }
    }
}

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                GlobalMiniPlayerBar()
                MuslimBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.PrayerTimes.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.PrayerTimes.route) {
                PrayerTimesScreen()
            }
            composable(Screen.Quran.route) {
                QuranScreen()
            }
            composable(Screen.Azkar.route) {
                AzkarScreen()
            }
            composable(Screen.Tasbeeh.route) {
                TasbeehScreen()
            }
            composable(Screen.Qibla.route) {
                QiblaScreen()
            }
            composable(Screen.Radio.route) {
                RadioScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
