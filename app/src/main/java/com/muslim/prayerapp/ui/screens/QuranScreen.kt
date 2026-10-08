package com.muslim.prayerapp.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muslim.prayerapp.data.model.Ayah
import com.muslim.prayerapp.data.model.Surah
import com.muslim.prayerapp.data.repository.QuranReciter
import com.muslim.prayerapp.data.repository.QuranRepository
import com.muslim.prayerapp.service.AzanAudioService
import com.muslim.prayerapp.ui.theme.*

@Composable
fun QuranScreen(
    context: Context = LocalContext.current
) {
    val quranRepo = remember { QuranRepository(context) }

    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var fontScale by remember { mutableIntStateOf(2) }
    var isMushafMode by remember { mutableStateOf(true) }

    var bookmarkSurahNum by remember { mutableIntStateOf(quranRepo.getBookmarkSurahNumber()) }
    var bookmarkAyahNum by remember { mutableIntStateOf(quranRepo.getBookmarkAyahNumber()) }
    var bookmarkSurahName by remember { mutableStateOf(quranRepo.getBookmarkSurahName()) }

    var showReciterDialog by remember { mutableStateOf(false) }
    var activeAudioSurah by remember { mutableStateOf<Surah?>(null) }
    var selectedReciter by remember { mutableStateOf(quranRepo.getSelectedReciter()) }

    val fontSp = when (fontScale) {
        1 -> 20.sp
        3 -> 30.sp
        else -> 24.sp
    }

    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            QuranRepository.surahs
        } else {
            QuranRepository.surahs.filter {
                it.nameArabic.contains(searchQuery.trim()) ||
                it.number.toString() == searchQuery.trim() ||
                it.nameEnglish.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (selectedSurah == null) {
            // === SURAH INDEX VIEW ===
            Column(modifier = Modifier.padding(16.dp)) {
                // Title Header
                Text(
                    text = "القرآن الكريم",
                    style = MaterialTheme.typography.headlineMedium,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "﴿إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ﴾",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldAccent,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Last Read Bookmark Card
                if (bookmarkSurahNum > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val surah = QuranRepository.surahs.find { it.number == bookmarkSurahNum }
                                if (surah != null) {
                                    selectedSurah = surah
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldAccent, EmeraldPrimary)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "فاصل القراءة الحالي",
                                    color = EmeraldPrimary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "سورة $bookmarkSurahName • آية $bookmarkAyahNum",
                                    color = textPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    val surah = QuranRepository.surahs.find { it.number == bookmarkSurahNum }
                                    if (surah != null) {
                                        selectedSurah = surah
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("متابعة القراءة", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GoldAccent) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Surahs List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSurahs) { surah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSurah = surah },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(cardBorder, Color.Transparent)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = EmeraldPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${surah.number}",
                                            color = EmeraldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "سورة ${surah.nameArabic}",
                                        color = textPrimary,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${surah.revelationType} • ${surah.numberOfAyahs} آيات • صـ ${surah.startPage}",
                                        color = textSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        activeAudioSurah = surah
                                        showReciterDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VolumeUp,
                                        contentDescription = "استماع للسورة",
                                        tint = GoldAccent
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Filled.ChevronLeft,
                                    contentDescription = null,
                                    tint = textSecondary
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }

        } else {
            // === SURAH READER VIEW ===
            val currentSurah = selectedSurah!!
            var ayahs by remember(currentSurah) { mutableStateOf<List<Ayah>>(emptyList()) }
            var isLoading by remember(currentSurah) { mutableStateOf(true) }

            LaunchedEffect(currentSurah) {
                isLoading = true
                ayahs = quranRepo.fetchSurahAyahs(currentSurah.number)
                isLoading = false
            }

            // Reader Top Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedSurah = null }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "رجوع", tint = EmeraldPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "سورة ${currentSurah.nameArabic}",
                                color = GoldAccent,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${currentSurah.revelationType} • ${currentSurah.numberOfAyahs} آيات • صـ ${currentSurah.startPage}",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Audio Stream Button with Reciter Selector
                            IconButton(
                                onClick = {
                                    activeAudioSurah = currentSurah
                                    showReciterDialog = true
                                }
                            ) {
                                Icon(Icons.Filled.VolumeUp, contentDescription = "استماع للسورة", tint = GoldAccent)
                            }

                            // Font Resizing Controls
                            IconButton(onClick = { if (fontScale > 1) fontScale-- }) {
                                Text("أ-", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            IconButton(onClick = { if (fontScale < 3) fontScale++ }) {
                                Text("أ+", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode Switcher: Mushaf View vs Separated Verses
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isMushafMode = true },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMushafMode) EmeraldPrimary else Color.Transparent
                        ) {
                            Text(
                                text = "عرض المصحف",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                color = if (isMushafMode) Color.White else textSecondary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isMushafMode = false },
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isMushafMode) EmeraldPrimary else Color.Transparent
                        ) {
                            Text(
                                text = "عرض الآيات منفصلة",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                color = if (!isMushafMode) Color.White else textSecondary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            } else {
                if (isMushafMode) {
                    // === CONTINUOUS MUSHAF PAGE VIEW ===
                    val annotatedMushafText = remember(ayahs) {
                        buildAnnotatedString {
                            ayahs.forEach { ayah ->
                                append(ayah.arabicText)
                                append(" ")
                                withStyle(style = SpanStyle(color = GoldAccent, fontWeight = FontWeight.Bold)) {
                                    append("۝${ayah.numberInSurah.toArabicDigits()} ")
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(GoldAccent, EmeraldPrimary, GoldAccent)))
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Bismillah Header (except Tawbah & Fatiha)
                                    if (currentSurah.number != 9 && currentSurah.number != 1) {
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                            fontFamily = QuranFont,
                                            color = EmeraldPrimary,
                                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(bottom = 16.dp))
                                    }

                                    // Continuous Mushaf Verse Flow
                                    Text(
                                        text = annotatedMushafText,
                                        fontFamily = QuranFont,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            lineHeight = (fontSp.value * 2.0).sp,
                                            fontSize = fontSp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = textPrimary,
                                        textAlign = TextAlign.Justify,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Quick Bookmark Button in Mushaf Mode
                                    OutlinedButton(
                                        onClick = {
                                            quranRepo.saveBookmark(currentSurah.number, 1, currentSurah.nameArabic)
                                            bookmarkSurahNum = currentSurah.number
                                            bookmarkAyahNum = 1
                                            bookmarkSurahName = currentSurah.nameArabic
                                            Toast.makeText(context, "تم حفظ فاصل القراءة عند سورة ${currentSurah.nameArabic}", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("حفظ فاصل القراءة عند هذه السورة")
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // === SEPARATED VERSES MODE ===
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(ayahs) { ayah ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = EmeraldPrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "الآية ${ayah.numberInSurah}",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                color = EmeraldPrimary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                quranRepo.saveBookmark(currentSurah.number, ayah.numberInSurah, currentSurah.nameArabic)
                                                bookmarkSurahNum = currentSurah.number
                                                bookmarkAyahNum = ayah.numberInSurah
                                                bookmarkSurahName = currentSurah.nameArabic
                                                Toast.makeText(context, "تم حفظ فاصل القراءة عند آية ${ayah.numberInSurah}", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (bookmarkSurahNum == currentSurah.number && bookmarkAyahNum == ayah.numberInSurah) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                                contentDescription = "حفظ الفاصل",
                                                tint = GoldAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "${ayah.arabicText} ۝${ayah.numberInSurah.toArabicDigits()}",
                                        fontFamily = QuranFont,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            lineHeight = (fontSp.value * 1.8).sp,
                                            fontSize = fontSp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = textPrimary,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Select Quran Reciter
    if (showReciterDialog && activeAudioSurah != null) {
        val surahToPlay = activeAudioSurah!!
        AlertDialog(
            onDismissRequest = { showReciterDialog = false },
            icon = { Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = GoldAccent) },
            title = {
                Text(
                    text = "اختر المقرئ للاستماع لسورة ${surahToPlay.nameArabic}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    QuranReciter.entries.forEach { reciter ->
                        val isSelected = reciter == selectedReciter
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedReciter = reciter
                                    quranRepo.setSelectedReciter(reciter)
                                    val audioUrl = QuranRepository.getSurahAudioUrl(surahToPlay.number, reciter)
                                    AzanAudioService.playRadioStation(
                                        context,
                                        "سورة ${surahToPlay.nameArabic}",
                                        audioUrl,
                                        "بصوت الشيخ ${reciter.nameArabic}"
                                    )
                                    showReciterDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الشيخ ${reciter.nameArabic}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedReciter = reciter
                                        quranRepo.setSelectedReciter(reciter)
                                        val audioUrl = QuranRepository.getSurahAudioUrl(surahToPlay.number, reciter)
                                        AzanAudioService.playRadioStation(
                                            context,
                                            "سورة ${surahToPlay.nameArabic}",
                                            audioUrl,
                                            "بصوت الشيخ ${reciter.nameArabic}"
                                        )
                                        showReciterDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReciterDialog = false }) {
                    Text("إلغاء", color = textSecondary)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = cardBg
        )
    }
}

// Convert English numbers to Arabic digits (١, ٢, ٣...)
fun Int.toArabicDigits(): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = this.toString()
    val builder = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            builder.append(arabicDigits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}
