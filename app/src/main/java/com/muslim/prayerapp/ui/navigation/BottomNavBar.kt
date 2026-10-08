package com.muslim.prayerapp.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.muslim.prayerapp.ui.theme.GoldAccent

@Composable
fun MuslimBottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val haptic = LocalHapticFeedback.current

    val listState = rememberLazyListState()

    val selectedIndex = remember(currentRoute) {
        Screen.navItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    }

    LaunchedEffect(selectedIndex) {
        if ((selectedIndex >= 0) && (listState.layoutInfo.totalItemsCount > selectedIndex)) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = surfaceColor,
        tonalElevation = 6.dp,
        shadowElevation = 10.dp,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    primaryColor.copy(alpha = 0.25f),
                    GoldAccent.copy(alpha = 0.35f),
                    primaryColor.copy(alpha = 0.25f),
                ),
            ),
        ),
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(Screen.navItems) { screen ->
                val isSelected = currentRoute == screen.route

                val animatedBgColor by animateColorAsState(
                    targetValue = if (isSelected) primaryColor.copy(alpha = 0.16f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220),
                    label = "bgColor",
                )

                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) primaryColor else onSurfaceVariant.copy(alpha = 0.60f),
                    animationSpec = tween(durationMillis = 220),
                    label = "contentColor",
                )

                val animatedBorderColor by animateColorAsState(
                    targetValue = if (isSelected) GoldAccent.copy(alpha = 0.45f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 220),
                    label = "borderColor",
                )

                val animatedScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                    label = "scale",
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(animatedBgColor)
                        .border(
                            width = 1.dp,
                            color = animatedBorderColor,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (!isSelected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.PrayerTimes.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.titleArabic,
                            tint = animatedContentColor,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(animatedScale),
                        )

                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(tween(180)) + expandHorizontally(tween(180)),
                            exit = fadeOut(tween(140)) + shrinkHorizontally(tween(140)),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = screen.titleArabic,
                                    color = animatedContentColor,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
