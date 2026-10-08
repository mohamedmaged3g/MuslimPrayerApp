package com.muslim.prayerapp.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muslim.prayerapp.data.calculator.QiblaCalculator
import com.muslim.prayerapp.data.repository.SettingsRepository
import com.muslim.prayerapp.sensor.CompassManager
import com.muslim.prayerapp.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun QiblaScreen(
    context: Context = LocalContext.current
) {
    val settingsRepo = remember { SettingsRepository(context) }
    val compassManager = remember { CompassManager(context) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    val azimuth by compassManager.azimuth.collectAsState()

    DisposableEffect(Unit) {
        compassManager.startListening()
        onDispose {
            compassManager.stopListening()
        }
    }

    val qiblaBearing = remember {
        QiblaCalculator.calculateQiblaBearing(
            settingsRepo.getLatitude(),
            settingsRepo.getLongitude()
        )
    }

    val needleAngle = remember(azimuth, qiblaBearing) {
        QiblaCalculator.calculateNeedleAngle(azimuth, qiblaBearing)
    }

    val isFacingQibla = remember(needleAngle) {
        QiblaCalculator.isFacingQibla(needleAngle, tolerance = 3.5f)
    }

    // Trigger subtle vibration once when aligned with Qibla
    var wasFacing by remember { mutableStateOf(false) }
    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla && !wasFacing) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator?.vibrate(100)
            }
        }
        wasFacing = isFacingQibla
    }

    val animatedDialRotation by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = spring(stiffness = 300f),
        label = "dial"
    )

    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Header
        Text(
            text = "بوصلة القبلة الشريفة",
            style = MaterialTheme.typography.headlineMedium,
            color = textPrimary,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${settingsRepo.getCityName()} (زاوية القبلة: ${qiblaBearing.roundToInt()}°)",
                style = MaterialTheme.typography.bodySmall,
                color = textSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Alignment Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFacingQibla) MaterialTheme.colorScheme.primaryContainer else cardBg
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    if (isFacingQibla) listOf(EmeraldPrimary, GoldAccent) else listOf(cardBorder, Color.Transparent)
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isFacingQibla) "🕋 أنت تواجه الكعبة المشرفة الآن مباشرة!" else "أدر الهاتف حتى يشير السهم الذهبي للأعلى تماماً",
                    color = if (isFacingQibla) GoldAccent else textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isFacingQibla) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Big Compass Dial
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(cardBg, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.background)
                    )
                )
                .border(
                    width = 4.dp,
                    brush = Brush.sweepGradient(
                        if (isFacingQibla) listOf(EmeraldPrimary, GoldAccent, EmeraldPrimary) else listOf(cardBorder, GoldAccent.copy(alpha = 0.3f), cardBorder)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Compass Markings Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedDialRotation)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 20f

                // Draw degree ticks
                for (angle in 0 until 360 step 15) {
                    val angleRad = Math.toRadians(angle.toDouble() - 90.0)
                    val isMajor = angle % 90 == 0
                    val tickLength = if (isMajor) 28f else 14f

                    val startX = center.x + (radius - tickLength) * cos(angleRad).toFloat()
                    val startY = center.y + (radius - tickLength) * sin(angleRad).toFloat()
                    val endX = center.x + radius * cos(angleRad).toFloat()
                    val endY = center.y + radius * sin(angleRad).toFloat()

                    drawLine(
                        color = if (isMajor) GoldAccent else TextMuted.copy(alpha = 0.4f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 3.5f else 1.5f
                    )
                }
            }

            // Qibla Indicator Needle (pointing towards Kaaba angle)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedDialRotation + qiblaBearing)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val needleLength = size.width * 0.36f
                val needleWidth = 22f

                // Golden pointer towards Kaaba
                val kaabaPath = Path().apply {
                    moveTo(center.x, center.y - needleLength)
                    lineTo(center.x + needleWidth, center.y)
                    lineTo(center.x, center.y - 15f)
                    lineTo(center.x - needleWidth, center.y)
                    close()
                }
                drawPath(kaabaPath, color = GoldAccent)

                // South pointer
                val southPath = Path().apply {
                    moveTo(center.x, center.y + needleLength)
                    lineTo(center.x + needleWidth, center.y)
                    lineTo(center.x, center.y + 15f)
                    lineTo(center.x - needleWidth, center.y)
                    close()
                }
                drawPath(southPath, color = cardBorder)

                // Center pivot ring
                drawCircle(color = EmeraldPrimary, radius = 14f, center = center)
                drawCircle(color = cardBg, radius = 6f, center = center)
            }

            // Kaaba Badge in Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-65).dp)
            ) {
                Text(
                    text = "🕋",
                    fontSize = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CompassCalibration,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "للحصول على أعلى دقة، حرّك الهاتف بحركة رقم (8) في الهواء بعيداً عن المعادن والمجالات المغناطيسية.",
                    style = MaterialTheme.typography.bodySmall,
                    color = textSecondary
                )
            }
        }
    }
}
