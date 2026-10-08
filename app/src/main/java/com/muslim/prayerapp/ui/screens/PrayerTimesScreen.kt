package com.muslim.prayerapp.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muslim.prayerapp.data.calculator.PrayerTimesCalculator
import com.muslim.prayerapp.data.model.PrayerTime
import com.muslim.prayerapp.data.model.PrayerType
import com.muslim.prayerapp.data.repository.SettingsRepository
import com.muslim.prayerapp.location.LocationHelper
import com.muslim.prayerapp.service.AzanAlarmScheduler
import com.muslim.prayerapp.service.AzanAudioService
import com.muslim.prayerapp.ui.components.DailyWisdomCard
import com.muslim.prayerapp.ui.components.HijriCalendarCard
import com.muslim.prayerapp.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun PrayerTimesScreen(
    context: Context = LocalContext.current
) {
    val settingsRepo = remember { SettingsRepository(context) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val coordinates = remember(refreshTrigger) {
        PrayerTimesCalculator.Coordinates(settingsRepo.getLatitude(), settingsRepo.getLongitude())
    }
    val method = remember(refreshTrigger) { settingsRepo.getCalculationMethod() }
    val juristic = remember(refreshTrigger) { settingsRepo.getJuristicMethod() }
    val offsets = remember(refreshTrigger) { settingsRepo.getAllPrayerOffsets() }

    val schedule = remember(refreshTrigger, coordinates, method, juristic, offsets) {
        PrayerTimesCalculator.calculateDailySchedule(coordinates, Calendar.getInstance(), method, juristic, offsets)
    }

    // Live countdown timer state
    var remainingMillis by remember { mutableStateOf(schedule.remainingMillisUntilNext) }

    LaunchedEffect(schedule) {
        remainingMillis = schedule.remainingMillisUntilNext
        while (remainingMillis > 0) {
            delay(1000L)
            remainingMillis -= 1000L
        }
        refreshTrigger++
    }

    val hours = (remainingMillis / 3600000).toInt()
    val minutes = ((remainingMillis % 3600000) / 60000).toInt()
    val seconds = ((remainingMillis % 60000) / 1000).toInt()
    val formattedCountdown = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    val bg = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header: Location & Dates
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(cardBorder, GoldAccent.copy(alpha = 0.3f))))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "الموقع",
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = settingsRepo.getCityName(),
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // GPS Auto Detect Button
                            IconButton(
                                onClick = {
                                    LocationHelper.fetchCurrentLocation(
                                        context = context,
                                        onSuccess = { cityName, _, _ ->
                                            Toast.makeText(context, "تم تحديث موقعك: $cityName", Toast.LENGTH_SHORT).show()
                                            refreshTrigger++
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = "تحديد موقعي بالـ GPS",
                                    tint = GoldAccent
                                )
                            }

                            IconButton(
                                onClick = {
                                    AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)
                                    refreshTrigger++
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "تحديث",
                                    tint = EmeraldPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = schedule.hijriDateFormatted,
                        color = GoldAccent,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = schedule.gregorianDateFormatted,
                        color = textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Hero Card: Next Prayer & Countdown
        item {
            val next = schedule.nextPrayer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(EmeraldPrimary, EmeraldMedium)
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.horizontalGradient(listOf(GoldLight, EmeraldLight)),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الصلاة القادمة",
                            color = GoldLight,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = next?.type?.arabicName ?: "الفجر",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Countdown digits box
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldLight.copy(alpha = 0.6f), Color.Transparent)))
                        ) {
                            Text(
                                text = formattedCountdown,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                                color = GoldLight,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "المتبقي حتى رفع الأذان",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Azan Audio Button
                        FilledTonalButton(
                            onClick = {
                                AzanAudioService.startAzan(context, next?.type ?: PrayerType.FAJR)
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = GoldLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = "تجربة الأذان",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تجربة صوت الأذان")
                        }
                    }
                }
            }
        }

        // Section Title: Daily Prayers
        item {
            Text(
                text = "مواقيت اليوم",
                color = textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Prayers List
        items(schedule.prayers) { prayer ->
            PrayerRowItem(
                prayer = prayer,
                isNotificationEnabled = settingsRepo.isPrayerNotificationEnabled(prayer.type),
                onToggleNotification = { enabled ->
                    settingsRepo.setPrayerNotificationEnabled(prayer.type, enabled)
                    AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)
                    refreshTrigger++
                }
            )
        }

        // Daily Wisdom (Ayah & Hadith) Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            DailyWisdomCard()
        }

        // Hijri Calendar & Upcoming Events Card
        item {
            Spacer(modifier = Modifier.height(10.dp))
            HijriCalendarCard()
        }

        item {
            Spacer(modifier = Modifier.height(60.dp)) // padding for bottom bar
        }
    }
}

@Composable
fun PrayerRowItem(
    prayer: PrayerTime,
    isNotificationEnabled: Boolean,
    onToggleNotification: (Boolean) -> Unit
) {
    val isHighlighted = prayer.isNext || prayer.isCurrent
    val backgroundColor = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (prayer.isNext) GoldAccent else if (prayer.isCurrent) EmeraldPrimary else MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(borderColor, Color.Transparent)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isHighlighted) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(prayer.type),
                        contentDescription = prayer.type.arabicName,
                        tint = if (isHighlighted) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = prayer.type.arabicName,
                        color = if (isHighlighted) GoldAccent else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
                    )
                    if (prayer.isNext) {
                        Text(
                            text = "الصلاة القادمة",
                            color = EmeraldPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (prayer.isCurrent) {
                        Text(
                            text = "الصلاة الحالية",
                            color = AccentCyan,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = prayer.formattedTime,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Toggle Notification (Except Sunrise, which is not an Azan time)
                if (prayer.type != PrayerType.SUNRISE) {
                    IconButton(
                        onClick = { onToggleNotification(!isNotificationEnabled) }
                    ) {
                        Icon(
                            imageVector = if (isNotificationEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsOff,
                            contentDescription = "تنبيه الأذان",
                            tint = if (isNotificationEnabled) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

fun getPrayerIcon(type: PrayerType) = when (type) {
    PrayerType.FAJR -> Icons.Filled.NightsStay
    PrayerType.SUNRISE -> Icons.Filled.WbSunny
    PrayerType.DHUHR -> Icons.Filled.LightMode
    PrayerType.ASR -> Icons.Filled.WbTwilight
    PrayerType.MAGHRIB -> Icons.Filled.Bedtime
    PrayerType.ISHA -> Icons.Filled.Brightness2
}
