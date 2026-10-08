package com.muslim.prayerapp.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muslim.prayerapp.data.model.AzkarCategory
import com.muslim.prayerapp.data.model.AzkarItem
import com.muslim.prayerapp.data.repository.AzkarRepository
import com.muslim.prayerapp.data.repository.DailySunanRepository
import com.muslim.prayerapp.data.repository.SettingsRepository
import com.muslim.prayerapp.ui.theme.*

@Composable
fun AzkarScreen(
    context: Context = LocalContext.current
) {
    val settingsRepo = remember { SettingsRepository(context) }
    val categories = remember { AzkarRepository.getAllCategories() }

    var selectedMode by remember { mutableStateOf("CATEGORY") } // "CATEGORY", "FAVORITE", "SUNAN"
    var selectedCategory by remember { mutableStateOf(AzkarCategory.MORNING) }
    var searchQuery by remember { mutableStateOf("") }
    var fontScale by remember { mutableStateOf(settingsRepo.getAzkarFontSizeScale()) }
    var favoriteTrigger by remember { mutableIntStateOf(0) }

    val fontSp = when (fontScale) {
        1 -> 16.sp
        3 -> 24.sp
        else -> 20.sp
    }

    val favoriteIds = remember(favoriteTrigger) { settingsRepo.getFavoriteAzkarIds() }

    val allAzkarList = remember { AzkarRepository.getAllCategories().flatMap { AzkarRepository.getAzkarByCategory(it) }.distinctBy { it.id } }

    val filteredAzkarList = remember(selectedMode, selectedCategory, searchQuery, favoriteIds) {
        val baseList = when (selectedMode) {
            "FAVORITE" -> allAzkarList.filter { favoriteIds.contains(it.id.toString()) }
            "SUNAN" -> emptyList()
            else -> AzkarRepository.getAzkarByCategory(selectedCategory)
        }

        if (searchQuery.isBlank()) {
            baseList
        } else {
            val query = searchQuery.trim()
            if (selectedMode == "FAVORITE" || selectedMode == "CATEGORY") {
                baseList.filter {
                    it.arabicText.contains(query) ||
                    it.benefit.contains(query) ||
                    it.reference.contains(query)
                }
            } else {
                allAzkarList.filter {
                    it.arabicText.contains(query) ||
                    it.benefit.contains(query)
                }
            }
        }
    }

    val counts = remember(selectedCategory, selectedMode) {
        mutableStateMapOf<Int, Int>()
    }

    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrateShort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator?.vibrate(40)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Title Header & Font Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "حصن المسلم والأذكار",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            // Font Resizing Buttons (A- / A+)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        TextButton(
                            onClick = {
                                if (fontScale > 1) {
                                    fontScale--
                                    settingsRepo.setAzkarFontSizeScale(fontScale)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("أ-", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }

                        Text(
                            text = when (fontScale) { 1 -> "صغير" 3 -> "كبير" else -> "وسط" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(
                            onClick = {
                                if (fontScale < 3) {
                                    fontScale++
                                    settingsRepo.setAzkarFontSizeScale(fontScale)
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text("أ+", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            placeholder = { Text("ابحث في الأذكار والأدعية...", style = MaterialTheme.typography.bodySmall) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "بحث", tint = GoldAccent) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "مسح", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Horizontal Category & Feature Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorites Chip
            item {
                val isSelected = selectedMode == "FAVORITE"
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedMode = "FAVORITE"
                        searchQuery = ""
                    },
                    label = { Text("المفضلة ❤️", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldAccent,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Daily Sunan Chip
            item {
                val isSelected = selectedMode == "SUNAN"
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedMode = "SUNAN"
                        searchQuery = ""
                    },
                    label = { Text("السنن والآداب 🌿", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Category Chips
            items(categories) { category ->
                val isSelected = selectedMode == "CATEGORY" && category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedMode = "CATEGORY"
                        selectedCategory = category
                        searchQuery = ""
                    },
                    label = {
                        Text(
                            text = category.titleArabic,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (selectedMode == "SUNAN" && searchQuery.isBlank()) {
            // Render Daily Sunan Categories
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                items(DailySunanRepository.categories) { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = cat.titleArabic,
                                style = MaterialTheme.typography.titleMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            cat.items.forEach { sunan ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("• ${sunan.titleArabic}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
                                    Text(sunan.descriptionArabic, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Render Azkar Cards List
            if (filteredAzkarList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedMode == "FAVORITE") "لم تقم بإضافة أي أذكار للمفضلة بعد.\nاضغط على علامة القلب ❤️ لإضافة أذكارك المفضلة." else "لم يتم العثور على أذكار تطابق البحث.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    items(filteredAzkarList, key = { it.id }) { item ->
                        val current = counts[item.id] ?: 0
                        val isDone = current >= item.targetCount
                        val isFav = favoriteIds.contains(item.id.toString())

                        AnimatedVisibility(
                            visible = !isDone,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            AzkarCardItem(
                                item = item,
                                currentCount = current,
                                isCompleted = isDone,
                                isFavorite = isFav,
                                fontSize = fontSp,
                                onTapCount = {
                                    if (!isDone) {
                                        counts[item.id] = current + 1
                                        vibrateShort()
                                    }
                                },
                                onReset = {
                                    counts[item.id] = 0
                                },
                                onToggleFavorite = {
                                    settingsRepo.toggleFavoriteAzkar(item.id.toString())
                                    favoriteTrigger++
                                },
                                onShare = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "${item.arabicText}\n\n[${item.benefit} - ${item.reference}]")
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة الذكر"))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AzkarCardItem(
    item: AzkarItem,
    currentCount: Int,
    isCompleted: Boolean,
    isFavorite: Boolean,
    fontSize: TextUnit,
    onTapCount: () -> Unit,
    onReset: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit
) {
    val borderColor = if (isCompleted) EmeraldPrimary else MaterialTheme.colorScheme.outline
    val containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(borderColor, Color.Transparent)))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Top Bar: Badge and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isCompleted) EmeraldPrimary.copy(alpha = 0.2f) else GoldAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isCompleted) "✓ تم بحمد الله" else "التكرار المطلوب: ${item.targetCount}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = if (isCompleted) EmeraldPrimary else GoldAccent,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (currentCount > 0) {
                        IconButton(onClick = onReset, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = "إعادة ضبط",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Arabic Zikr Text with scalable font size
            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = (fontSize.value * 1.6).sp,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            if (item.benefit.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "• ${item.benefit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (item.reference.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "المصدر: ${item.reference}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Interactive Counter Button
            Button(
                onClick = onTapCount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) EmeraldMedium else EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCompleted) "اكتمل الذكر" else "اضغط للتسبيح ($currentCount / ${item.targetCount})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
