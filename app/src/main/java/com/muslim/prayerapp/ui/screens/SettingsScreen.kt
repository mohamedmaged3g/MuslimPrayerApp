package com.muslim.prayerapp.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.muslim.prayerapp.data.model.*
import com.muslim.prayerapp.data.model.AppThemeState
import com.muslim.prayerapp.data.repository.CustomAzanRepository
import com.muslim.prayerapp.data.repository.SettingsRepository
import com.muslim.prayerapp.location.LocationHelper
import com.muslim.prayerapp.service.AzanAlarmScheduler
import com.muslim.prayerapp.service.AzanAudioService
import com.muslim.prayerapp.service.AzkarReminderScheduler
import com.muslim.prayerapp.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    context: Context = LocalContext.current
) {
    val settingsRepo = remember { SettingsRepository(context) }
    val customAzanRepo = remember { CustomAzanRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var isLightTheme by remember { mutableStateOf(settingsRepo.isLightTheme()) }
    var selectedCalcMethod by remember { mutableStateOf(settingsRepo.getCalculationMethod()) }
    var selectedJuristicMethod by remember { mutableStateOf(settingsRepo.getJuristicMethod()) }

    var selectedMoazenVoice by remember { mutableStateOf(settingsRepo.getMoazenVoice()) }
    var customAzanTitle by remember { mutableStateOf(settingsRepo.getCustomAzanTitle()) }
    var selectedPreAzanOffset by remember { mutableStateOf(settingsRepo.getPreAzanOffset()) }

    var globalAzanMode by remember { mutableStateOf(settingsRepo.getGlobalAzanSoundMode()) }
    var isPrayerCustomSoundEnabled by remember { mutableStateOf(settingsRepo.isPrayerCustomSoundEnabled()) }

    var isMorningReminder by remember { mutableStateOf(settingsRepo.isMorningAzkarReminderEnabled()) }
    var isEveningReminder by remember { mutableStateOf(settingsRepo.isEveningAzkarReminderEnabled()) }
    var isSleepReminder by remember { mutableStateOf(settingsRepo.isSleepAzkarReminderEnabled()) }

    var isTasbeehVibrate by remember { mutableStateOf(settingsRepo.isTasbeehVibrationEnabled()) }
    var isAutoSilent by remember { mutableStateOf(settingsRepo.isAutoSilentEnabled()) }

    var showCalcDialog by remember { mutableStateOf(false) }
    var showJuristicDialog by remember { mutableStateOf(false) }
    var showMoazenDialog by remember { mutableStateOf(false) }
    var showPreAzanDialog by remember { mutableStateOf(false) }
    var showGlobalAzanDialog by remember { mutableStateOf(false) }
    var showOffsetDialog by remember { mutableStateOf(false) }

    var downloadingAzanId by remember { mutableStateOf<String?>(null) }
    var downloadProgress by remember { mutableIntStateOf(0) }

    var selectedPrayerForCustomDialog by remember { mutableStateOf<PrayerType?>(null) }

    val customAzanPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val customAzan = customAzanRepo.copyUserPickedAudio(it, "أذان مخصص من الهاتف")
                if (customAzan != null) {
                    settingsRepo.setCustomAzanFile(customAzan.filePath, customAzan.titleArabic)
                    customAzanTitle = customAzan.titleArabic
                    Toast.makeText(context, "تم حفظ وتفعيل الأذان المخصص من جهازك", Toast.LENGTH_SHORT).show()
                    AzanAudioService.previewAzanSound(context, PrayerType.FAJR, globalAzanMode)
                    showMoazenDialog = false
                }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            } else {
                true
            }
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                    isIgnoringBatteryOptimizations = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "الإعدادات والضبط",
                style = MaterialTheme.typography.headlineMedium,
                color = textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "تخصيص طرق الحساب والمؤذن والتنبيهات المخصصة",
                style = MaterialTheme.typography.bodyMedium,
                color = GoldAccent,
                fontWeight = FontWeight.Medium
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            // Section 1: Location & GPS
            item {
                Text(
                    text = "الموقع الجغرافي والمدينة",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "المدينة الحالية: ${settingsRepo.getCityName()}",
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "إحداثيات GPS: ${String.format("%.2f", settingsRepo.getLatitude())}, ${String.format("%.2f", settingsRepo.getLongitude())}",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = {
                                LocationHelper.fetchCurrentLocation(
                                    context = context,
                                    onSuccess = { city, _, _ ->
                                        Toast.makeText(context, "تم تحديث موقعك: $city", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { errorMsg ->
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تحديث GPS", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Section 2: Theme Settings
            item {
                Text(
                    text = "المظهر وثيم التطبيق",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الوضع الفاتح (Bright Theme)",
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "تفعيل الثيم الفاتح النقي بدلاً من الثيم الداكن",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Switch(
                            checked = isLightTheme,
                            onCheckedChange = {
                                isLightTheme = it
                                AppThemeState.isLightTheme = it
                                settingsRepo.setLightTheme(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldAccent,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }
                }
            }

            // Section 3: Azan Sound Settings & Moazen Voice Selection
            item {
                Text(
                    text = "إعدادات أصوات وصوت المؤذن للأذان",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Moazen Voice Picker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMoazenDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "صوت المؤذن المفضل / الأذان المخصص",
                                    color = textPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = customAzanTitle ?: selectedMoazenVoice.titleArabic,
                                    color = EmeraldPrimary,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }

                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                        // Pre-Azan Notification Offset
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPreAzanDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "التنبيه المسبق قبل الأذان",
                                    color = textPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = selectedPreAzanOffset.titleArabic,
                                    color = EmeraldPrimary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }

                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                        // Global Azan Mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showGlobalAzanDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "نوع صوت الأذان العام",
                                    color = textPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = globalAzanMode.titleArabic,
                                    color = EmeraldPrimary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }
                    }
                }
            }

            // Section 4: Custom Sound Mode per Prayer
            item {
                Text(
                    text = "تخصيص صوت الأذان لكل صلاة بشكل منفصل",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تخصيص وضع أذان لكل صلاة",
                                    color = textPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "تحديد أذان كامل أو تكبيرات فقط أو إشعار لكل صلاة على حدة",
                                    color = textSecondary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Switch(
                                checked = isPrayerCustomSoundEnabled,
                                onCheckedChange = {
                                    isPrayerCustomSoundEnabled = it
                                    settingsRepo.setPrayerCustomSoundEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldAccent,
                                    checkedTrackColor = EmeraldPrimary
                                )
                            )
                        }

                        AnimatedVisibility(visible = isPrayerCustomSoundEnabled) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 6.dp))

                                PrayerType.entries.forEach { prayer ->
                                    val currentMode = settingsRepo.getPrayerAzanSoundMode(prayer)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPrayerForCustomDialog = prayer }
                                            .padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "أذان ${prayer.arabicName}",
                                            color = textPrimary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentMode.titleArabic,
                                                color = GoldAccent,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = textSecondary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 5: Reminders & Notifications
            item {
                Text(
                    text = "تنبيهات الأذكار التلقائية والوضع الصامت",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Morning Azkar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تنبيه أذكار الصباح", color = textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("تنبيه يومي في الساعة 07:00 صباحاً", color = textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = isMorningReminder,
                                onCheckedChange = {
                                    isMorningReminder = it
                                    settingsRepo.setMorningAzkarReminderEnabled(it)
                                    AzkarReminderScheduler.scheduleReminders(context)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = EmeraldPrimary)
                            )
                        }

                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                        // Evening Azkar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تنبيه أذكار المساء", color = textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("تنبيه يومي في الساعة 04:30 عصراً", color = textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = isEveningReminder,
                                onCheckedChange = {
                                    isEveningReminder = it
                                    settingsRepo.setEveningAzkarReminderEnabled(it)
                                    AzkarReminderScheduler.scheduleReminders(context)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = EmeraldPrimary)
                            )
                        }

                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                        // Sleep Azkar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تنبيه أذكار النوم", color = textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("تنبيه يومي في الساعة 10:30 مساءً", color = textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = isSleepReminder,
                                onCheckedChange = {
                                    isSleepReminder = it
                                    settingsRepo.setSleepAzkarReminderEnabled(it)
                                    AzkarReminderScheduler.scheduleReminders(context)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = EmeraldPrimary)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "الاهتزاز أثناء التسبيح",
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "نبضة اهتزاز لطيفة مع كل تسبيحة وعند إتمام الدورة",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Switch(
                            checked = isTasbeehVibrate,
                            onCheckedChange = {
                                isTasbeehVibrate = it
                                settingsRepo.setTasbeehVibrationEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldAccent,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "وضع عدم الإزعاج الصامت أثناء الصلاة",
                                color = textPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "تحويل الهاتف تلقائياً للوضع الصامت لمدة 20 دقيقة بعد كل أذان لمنع الإزعاج بالمسجد",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Switch(
                            checked = isAutoSilent,
                            onCheckedChange = {
                                isAutoSilent = it
                                settingsRepo.setAutoSilentEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoldAccent,
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }
                }
            }

            // Section 6: Calculation Methods & Fine-Tuning Offsets
            item {
                Text(
                    text = "طرق حساب المواقيت وتعديل الدقائق",
                    style = MaterialTheme.typography.titleMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCalcDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("طريقة حساب مواقيت الصلاة", color = textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(selectedCalcMethod.titleArabic, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }

                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showJuristicDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("المذهب الفقهي لصلاة العصر", color = textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(selectedJuristicMethod.titleArabic, color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }

                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showOffsetDialog = true }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("تعديل مواقيت الصلاة بالدقائق (+/-)", color = textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("ضبط وتأخير أو تقديم أوقات الصلوات لتمسكت بالمسجد المحلي", color = EmeraldPrimary, style = MaterialTheme.typography.bodySmall)
                            }
                            Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = textSecondary)
                        }
                    }
                }
            }

            // Section 7: Battery Optimization Guide Banner (يظهر فقط إذا لم يُلغى تقييد البطارية بعد)
            if (!isIgnoringBatteryOptimizations) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(EmeraldPrimary, GoldAccent)))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.BatterySaver, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ضمان انطلاق الأذان في موعده الدقيق", color = textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "لضمان رفع الأذان في موعده بدقة حتى عند إغلاق الشاشة لفترات طويلة، يُنصح بإلغاء قيود توفير البطارية للتطبيق.",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.parse("package:${context.packageName}")
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("ضبط إعدادات البطارية", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 8: About
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الأذان والأذكار والقرآن",
                            color = GoldAccent,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "الإصدار 1.3.0 • تطبيق إسلامي شامل أوفلاين بدون إعلانات",
                            color = textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "«مَنْ دَلَّ عَلَى خَيْرٍ فَلَهُ مِثْلُ أَجْرِ فَاعِلِهِ»\nنسألكم صالح الدعاء لنا ولوالدينا ولجميع المسلمين.\nوقراءة الفاتحة لوالدتي رحمها الله.",
                            color = EmeraldPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = cardBorder.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "برمجة وتطوير: محمد ماجد",
                            color = textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Contact Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // WhatsApp Button
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/201286450343"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("واتساب 💬", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Facebook Button
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/mohamedmagedme"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("فيسبوك 🌐", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // Email Button
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mohamedmaged3g@gmail.com"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إيميل ✉️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                    }
                }
            }
        }
    }

    // Dialog: Fine-Tune Minute Offsets
    if (showOffsetDialog) {
        var dummyTrigger by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = { showOffsetDialog = false },
            title = { Text("تعديل مواقيت الصلاة بالدقائق", color = GoldAccent, fontWeight = FontWeight.Bold) },
            containerColor = cardBg,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrayerType.entries.forEach { prayer ->
                        val currentOffset = settingsRepo.getPrayerMinuteOffset(prayer)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("صلاة ${prayer.arabicName}", color = textPrimary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedIconButton(
                                    onClick = {
                                        settingsRepo.setPrayerMinuteOffset(prayer, currentOffset - 1)
                                        dummyTrigger++
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = if (currentOffset > 0) "+$currentOffset د" else if (currentOffset < 0) "$currentOffset د" else "0 د",
                                    color = GoldAccent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                                OutlinedIconButton(
                                    onClick = {
                                        settingsRepo.setPrayerMinuteOffset(prayer, currentOffset + 1)
                                        dummyTrigger++
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)
                        showOffsetDialog = false
                    }
                ) {
                    Text("حفظ وتحديث", color = EmeraldPrimary)
                }
            }
        )
    }

    // Dialog: Moazen Voice Selection
    if (showMoazenDialog) {
        AlertDialog(
            onDismissRequest = { showMoazenDialog = false },
            title = {
                Text(
                    text = "اختر صوت المؤذن أو حمّل أذان مخصص",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            containerColor = cardBg,
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Pick Audio File from Phone Button
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { customAzanPickerLauncher.launch("audio/*") }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Folder, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("اختيار ملف أذان من الجهاز 📁", fontWeight = FontWeight.Bold, color = textPrimary)
                                    Text("تحديد أي ملف MP3 مخزن على هاتفك كصوت للأذان", style = MaterialTheme.typography.labelSmall, color = textSecondary)
                                }
                            }
                        }
                    }

                    // Active Custom Audio File (if selected)
                    if (customAzanTitle != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, EmeraldPrimary)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("الأذان المخصص الحالي:", style = MaterialTheme.typography.labelSmall, color = EmeraldPrimary)
                                        Text(customAzanTitle ?: "", fontWeight = FontWeight.Bold, color = textPrimary)
                                    }
                                    IconButton(
                                        onClick = {
                                            settingsRepo.clearCustomAzanFile()
                                            customAzanTitle = null
                                            Toast.makeText(context, "تم العودة للأذان المضمن", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "إلغاء", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    // Section: Online Downloadable Azan Voices
                    item {
                        Text(
                            text = "تنزيل أصوات أذان من النت 🌐",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(customAzanRepo.onlineAzanList) { onlineAzan ->
                        val isDownloaded = customAzanRepo.isAzanDownloaded(onlineAzan.id)
                        val isCurrentlySelected = customAzanTitle == onlineAzan.titleArabic
                        val isDownloading = downloadingAzanId == onlineAzan.id

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCurrentlySelected) EmeraldPrimary.copy(alpha = 0.18f) else cardBg,
                            border = BorderStroke(1.dp, if (isCurrentlySelected) GoldAccent else cardBorder.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(onlineAzan.titleArabic, fontWeight = FontWeight.Bold, color = textPrimary, style = MaterialTheme.typography.bodyMedium)
                                    Text(onlineAzan.descriptionArabic, style = MaterialTheme.typography.labelSmall, color = textSecondary)
                                }

                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = GoldAccent,
                                        strokeWidth = 2.dp
                                    )
                                } else if (isDownloaded) {
                                    Button(
                                        onClick = {
                                            val filePath = customAzanRepo.getDownloadedFilePath(onlineAzan.id)
                                            settingsRepo.setCustomAzanFile(filePath, onlineAzan.titleArabic)
                                            customAzanTitle = onlineAzan.titleArabic
                                            Toast.makeText(context, "تم تفعيل ${onlineAzan.titleArabic}", Toast.LENGTH_SHORT).show()
                                            AzanAudioService.previewAzanSound(context, PrayerType.FAJR, globalAzanMode)
                                            showMoazenDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isCurrentlySelected) EmeraldPrimary else GoldAccent),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (isCurrentlySelected) "مُفعل ✓" else "تفعيل", fontSize = 11.sp)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            downloadingAzanId = onlineAzan.id
                                            coroutineScope.launch {
                                                val path = customAzanRepo.downloadAzan(onlineAzan) { prog ->
                                                    downloadProgress = prog
                                                }
                                                downloadingAzanId = null
                                                if (path != null) {
                                                    settingsRepo.setCustomAzanFile(path, onlineAzan.titleArabic)
                                                    customAzanTitle = onlineAzan.titleArabic
                                                    Toast.makeText(context, "تم تنزيل وتفعيل ${onlineAzan.titleArabic}", Toast.LENGTH_SHORT).show()
                                                    AzanAudioService.previewAzanSound(context, PrayerType.FAJR, globalAzanMode)
                                                    showMoazenDialog = false
                                                } else {
                                                    Toast.makeText(context, "فشل تنزيل الأذان، تأكد من اتصال الإنترنت", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تنزيل", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Section: Standard Built-in Moazen Voices
                    item {
                        Text(
                            text = "أصوات الأذان الرئيسية المضمنة",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    items(MoazenVoice.entries) { voice ->
                        val isSelected = voice == selectedMoazenVoice && customAzanTitle == null
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsRepo.clearCustomAzanFile()
                                    customAzanTitle = null
                                    selectedMoazenVoice = voice
                                    settingsRepo.setMoazenVoice(voice)
                                    AzanAudioService.previewAzanSound(context, PrayerType.FAJR, globalAzanMode)
                                    showMoazenDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(voice.titleArabic, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                Text(voice.descriptionArabic, color = textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoazenDialog = false }) {
                    Text("إغلاق", color = EmeraldPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Pre-Azan Offset Selection
    if (showPreAzanDialog) {
        AlertDialog(
            onDismissRequest = { showPreAzanDialog = false },
            title = { Text("اختر وقت التنبيه المسبق قبل الأذان", color = GoldAccent, fontWeight = FontWeight.Bold) },
            containerColor = cardBg,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreAzanOffset.entries.forEach { offset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPreAzanOffset = offset
                                    settingsRepo.setPreAzanOffset(offset)
                                    AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)
                                    showPreAzanDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = offset == selectedPreAzanOffset,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(offset.titleArabic, color = textPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPreAzanDialog = false }) {
                    Text("إغلاق", color = EmeraldPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Global Azan Sound Mode
    if (showGlobalAzanDialog) {
        AlertDialog(
            onDismissRequest = { showGlobalAzanDialog = false },
            title = { Text("اختر وضع صوت الأذان العام", color = GoldAccent, fontWeight = FontWeight.Bold) },
            containerColor = cardBg,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AzanSoundMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    globalAzanMode = mode
                                    settingsRepo.setGlobalAzanSoundMode(mode)
                                    showGlobalAzanDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == globalAzanMode,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(mode.titleArabic, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                Text(mode.descriptionArabic, color = textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGlobalAzanDialog = false }) {
                    Text("إغلاق", color = EmeraldPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Custom Sound Mode per Prayer
    selectedPrayerForCustomDialog?.let { prayer ->
        val currentMode = settingsRepo.getPrayerAzanSoundMode(prayer)
        AlertDialog(
            onDismissRequest = { selectedPrayerForCustomDialog = null },
            title = { Text("تخصيص وضع أذان صلاة ${prayer.arabicName}", color = GoldAccent, fontWeight = FontWeight.Bold) },
            containerColor = cardBg,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AzanSoundMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsRepo.setPrayerAzanSoundMode(prayer, mode)
                                    selectedPrayerForCustomDialog = null
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == currentMode,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(mode.titleArabic, color = textPrimary, fontWeight = FontWeight.SemiBold)
                                Text(mode.descriptionArabic, color = textSecondary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPrayerForCustomDialog = null }) {
                    Text("إغلاق", color = EmeraldPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Calculation Method
    if (showCalcDialog) {
        AlertDialog(
            onDismissRequest = { showCalcDialog = false },
            title = { Text("اختر طريقة حساب المواقيت", color = GoldAccent, fontWeight = FontWeight.Bold) },
            containerColor = cardBg,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalculationMethod.entries.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCalcMethod = method
                                    settingsRepo.setCalculationMethod(method)
                                    AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)
                                    showCalcDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = method == selectedCalcMethod,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(method.titleArabic, color = textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCalcDialog = false }) {
                    Text("إغلاق", color = EmeraldPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Dialog: Juristic Method (Asr)
    if (showJuristicDialog) {
        AlertDialog(
            onDismissRequest = { showJuristicDialog = false },
            title = { Text("اختر المذهب الفقهي لصلاة العصر", color = GoldAccent, fontWeight = FontWeight.Bold) },
            containerColor = cardBg,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    JuristicMethod.entries.forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedJuristicMethod = method
                                    settingsRepo.setJuristicMethod(method)
                                    AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)
                                    showJuristicDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = method == selectedJuristicMethod,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(method.titleArabic, color = textPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showJuristicDialog = false }) {
                    Text("إغلاق", color = EmeraldPrimary)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
