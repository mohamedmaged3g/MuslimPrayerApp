package com.muslim.prayerapp.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muslim.prayerapp.data.model.AudioState
import com.muslim.prayerapp.service.AzanAudioService
import com.muslim.prayerapp.ui.theme.GoldAccent

@Composable
fun GlobalMiniPlayerBar(
    context: Context = LocalContext.current
) {
    AnimatedVisibility(
        visible = AudioState.isPlaying,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 10.dp,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        GoldAccent.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        GoldAccent.copy(alpha = 0.5f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Artwork Icon & Animated Equalizer Soundwaves
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(GoldAccent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (AudioState.isRadioMode) Icons.Filled.Radio else Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Equalizer Visualizer
                        EqualizerVisualizer(
                            isPlaying = AudioState.isPlaying && !AudioState.isPaused,
                            color = GoldAccent
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = AudioState.currentTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (AudioState.isRadioMode) {
                                    Text(
                                        text = "بث مباشر ●",
                                        color = GoldAccent,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = AudioState.currentSubtitle,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Right: Media Player Controls (Seek -10s, Play/Pause, Seek +10s, Close)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Rewind -10s (For surahs/audio)
                        if (!AudioState.isRadioMode) {
                            IconButton(
                                onClick = { AzanAudioService.seekRelative(context, -10_000) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                                    contentDescription = "ترجيع 10 ثوانٍ",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Play / Pause Toggle Button
                        IconButton(
                            onClick = {
                                if (AudioState.isPaused) {
                                    AzanAudioService.resumeAudio(context)
                                } else {
                                    AzanAudioService.pauseAudio(context)
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(GoldAccent)
                        ) {
                            Icon(
                                imageVector = if (AudioState.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                contentDescription = if (AudioState.isPaused) "تشغيل" else "إيقاف مؤقت",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Fast Forward +10s (For surahs/audio)
                        if (!AudioState.isRadioMode) {
                            IconButton(
                                onClick = { AzanAudioService.seekRelative(context, 10_000) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                    contentDescription = "تقديم 10 ثوانٍ",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Close / Dismiss Player
                        IconButton(
                            onClick = {
                                AzanAudioService.stopAudio(context)
                                AudioState.stop()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إغلاق المشغل",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EqualizerVisualizer(
    isPlaying: Boolean,
    color: Color = GoldAccent
) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val height1 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )

    val height2 by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )

    val height3 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.height(22.dp)
    ) {
        val h1 = if (isPlaying) height1.dp else 6.dp
        val h2 = if (isPlaying) height2.dp else 12.dp
        val h3 = if (isPlaying) height3.dp else 8.dp

        Box(modifier = Modifier.width(3.dp).height(h1).clip(RoundedCornerShape(2.dp)).background(color))
        Box(modifier = Modifier.width(3.dp).height(h2).clip(RoundedCornerShape(2.dp)).background(color))
        Box(modifier = Modifier.width(3.dp).height(h3).clip(RoundedCornerShape(2.dp)).background(color))
    }
}
