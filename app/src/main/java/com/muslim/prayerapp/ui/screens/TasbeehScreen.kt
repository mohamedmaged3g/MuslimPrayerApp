package com.muslim.prayerapp.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muslim.prayerapp.data.repository.SettingsRepository

@Composable
fun TasbeehScreen(
    context: Context = LocalContext.current
) {
    val settingsRepo = remember { SettingsRepository(context) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    val defaultPraises = remember {
        listOf(
            "سُبْحَانَ اللَّهِ",
            "الْحَمْدُ لِلَّهِ",
            "لاَ إِلَٰهَ إِلاَّ اللَّهُ",
            "اللَّهُ أَكْبَرُ",
            "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            "لاَ حَوْلَ وَلاَ قُوَّةَ إِلاَّ بِاللَّهِ",
            "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ"
        )
    }

    var customPraises by remember { mutableStateOf(settingsRepo.getCustomPraises()) }
    val allPraises = remember(customPraises) { defaultPraises + customPraises }

    var selectedPraise by remember { mutableStateOf(allPraises.first()) }
    var currentCount by remember { mutableIntStateOf(0) }
    var targetCycle by remember { mutableIntStateOf(33) }
    var totalCount by remember { mutableIntStateOf(settingsRepo.getTasbeehTotalCount()) }
    var isVibrateEnabled by remember { mutableStateOf(settingsRepo.isTasbeehVibrationEnabled()) }

    var showAddDialog by remember { mutableStateOf(false) }
    var newPraiseText by remember { mutableStateOf("") }

    val tasbeehInteractionSource = remember { MutableInteractionSource() }
    val isPressed by tasbeehInteractionSource.collectIsPressedAsState()

    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.40f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "glowAlpha"
    )

    val glowScale by animateFloatAsState(
        targetValue = if (isPressed) 1.08f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glowScale"
    )

    val countScale = remember { Animatable(1f) }
    LaunchedEffect(currentCount) {
        if (currentCount > 0) {
            countScale.animateTo(
                targetValue = 1.25f,
                animationSpec = tween(durationMillis = 60)
            )
            countScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    val progress = (currentCount % targetCycle).toFloat() / targetCycle.toFloat()
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    fun triggerVibration(isMilestone: Boolean = false) {
        if (!isVibrateEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isMilestone) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 150), -1))
            } else {
                vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } else {
            vibrator?.vibrate(if (isMilestone) 200 else 35)
        }
    }

    fun onIncrement() {
        val next = currentCount + 1
        currentCount = next
        totalCount += 1
        settingsRepo.saveTasbeehTotalCount(totalCount)

        val isCycleEnd = next % targetCycle == 0
        triggerVibration(isMilestone = isCycleEnd)
    }

    fun onReset() {
        currentCount = 0
    }

    // باليت ألوان راقية ومتجاوبة مع الثيم
    val cardBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
    val goldToned = Color(0xFFD4A346)
    val primaryEmerald = Color(0xFF1B6A45)
    val accentEmerald = Color(0xFF2E9462)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // شريط العنوان
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "السبحة الإلكترونية",
                    style = MaterialTheme.typography.headlineSmall,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "وَاذْكُر رَّبَّكَ كَثِيرًا وَسَبِّحْ بِالْعَشِيِّ وَالْإِبْكارِ",
                    style = MaterialTheme.typography.bodySmall,
                    color = goldToned,
                    fontWeight = FontWeight.Medium
                )
            }

            Row {
                IconButton(
                    onClick = {
                        isVibrateEnabled = !isVibrateEnabled
                        settingsRepo.setTasbeehVibrationEnabled(isVibrateEnabled)
                    }
                ) {
                    Icon(
                        imageVector = if (isVibrateEnabled) Icons.Filled.Vibration else Icons.Filled.Smartphone,
                        contentDescription = "الاهتزاز",
                        tint = if (isVibrateEnabled) accentEmerald else textSecondary
                    )
                }

                IconButton(onClick = { onReset() }) {
                    Icon(
                        imageVector = Icons.Filled.RestartAlt,
                        contentDescription = "إعادة ضبط",
                        tint = textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // بطاقة عرض الذكر والخيارات
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        goldToned.copy(alpha = 0.2f),
                        accentEmerald.copy(alpha = 0.35f),
                        goldToned.copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedPraise,
                    color = goldToned,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = { targetCycle = 33 },
                        label = { Text("دورة 33") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (targetCycle == 33) accentEmerald.copy(alpha = 0.22f) else Color.Transparent,
                            labelColor = if (targetCycle == 33) accentEmerald else textSecondary
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (targetCycle == 33) accentEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    )
                    AssistChip(
                        onClick = { targetCycle = 100 },
                        label = { Text("دورة 100") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (targetCycle == 100) accentEmerald.copy(alpha = 0.22f) else Color.Transparent,
                            labelColor = if (targetCycle == 100) accentEmerald else textSecondary
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (targetCycle == 100) accentEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // زر التسبيح الدائري الملكي (تدرج فاخر + حلقة مسار التقدم + انيميشن التفاعل)
        Box(
            contentAlignment = Alignment.Center
        ) {
            // توهج خلفي ناعم عند الضغط
            Box(
                modifier = Modifier
                    .size(255.dp)
                    .scale(glowScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                goldToned.copy(alpha = glowAlpha),
                                accentEmerald.copy(alpha = glowAlpha * 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // زر السبحة الرئيسي
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(scaleAnim)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                accentEmerald,
                                primaryEmerald,
                                Color(0xFF0F3D27)
                            )
                        )
                    )
                    .border(
                        width = 3.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                goldToned.copy(alpha = 0.9f),
                                Color(0xFFF1D18A),
                                goldToned.copy(alpha = 0.4f),
                                goldToned.copy(alpha = 0.9f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .clickable(
                        interactionSource = tasbeehInteractionSource,
                        indication = null,
                        onClick = { onIncrement() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // مؤشر التقدم الدائري البارز والكبير على طرف الدائرة
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(232.dp),
                    color = goldToned,
                    trackColor = Color.White.copy(alpha = 0.12f),
                    strokeWidth = 7.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$currentCount",
                        color = Color.White,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 68.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.scale(countScale.value)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "من $targetCycle",
                        color = Color(0xFFF2D594),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "اضغط للتسبيح",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // شرائح الأذكار أسفل السبحة مباشرةً (بناءً على طلب المستخدم)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // زر إضافة ذكر جديد
            item {
                AssistChip(
                    onClick = { showAddDialog = true },
                    label = {
                        Text(
                            text = "إضافة ذكر +",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = goldToned.copy(alpha = 0.18f),
                        labelColor = goldToned
                    ),
                    border = BorderStroke(1.dp, goldToned.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // قائمة الأذكار الرئيسية والمخصصة
            items(allPraises) { praise ->
                val isSelected = praise == selectedPraise
                val isCustom = customPraises.contains(praise)

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedPraise = praise
                        onReset()
                    },
                    label = {
                        Text(
                            text = praise,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    trailingIcon = if (isCustom) {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "حذف الذكر",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        settingsRepo.removeCustomPraise(praise)
                                        customPraises = settingsRepo.getCustomPraises()
                                        if (selectedPraise == praise) {
                                            selectedPraise = defaultPraises.first()
                                            onReset()
                                        }
                                    },
                                tint = if (isSelected) Color.White.copy(alpha = 0.8f) else textSecondary
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryEmerald,
                        selectedLabelColor = Color.White,
                        containerColor = cardBg,
                        labelColor = textSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) goldToned.copy(alpha = 0.5f) else Color.Transparent,
                        selectedBorderColor = goldToned,
                        borderWidth = 1.dp,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // شريط إجمالي التسابيح
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 45.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), goldToned.copy(alpha = 0.25f))
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إجمالي التسابيح المحفوظة",
                    color = textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$totalCount",
                    color = goldToned,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // نافذة إدخال ذكر مخصص جديد
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newPraiseText = ""
            },
            title = {
                Text(
                    text = "إضافة ذكر مخصص",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column {
                    Text(
                        text = "اكتب الذكر أو الدعاء الذي تريد تسبيحه:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPraiseText,
                        onValueChange = { newPraiseText = it },
                        placeholder = { Text("مثال: لا إله إلا أنت سبحانك إني كنت من الظالمين") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryEmerald,
                            cursorColor = primaryEmerald
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newPraiseText.trim()
                        if (trimmed.isNotBlank()) {
                            settingsRepo.addCustomPraise(trimmed)
                            customPraises = settingsRepo.getCustomPraises()
                            selectedPraise = trimmed
                            onReset()
                            showAddDialog = false
                            newPraiseText = ""
                        }
                    },
                    enabled = newPraiseText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryEmerald)
                ) {
                    Text("حفظ وإضافة")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        newPraiseText = ""
                    }
                ) {
                    Text("إلغاء", color = textSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
