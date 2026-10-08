package com.muslim.prayerapp.data.repository

import android.content.Context
import com.muslim.prayerapp.data.model.Ayah
import com.muslim.prayerapp.data.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader

enum class QuranReciter(
    val id: String,
    val nameArabic: String,
    val serverIdentifier: String,
    val mp3quranUrl: String
) {
    ALAFASY("alafasy", "مشاري راشد العفاسي", "ar.alafasy", "https://server8.mp3quran.net/afs"),
    ABDELBASET("abdelbaset", "عبد الباسط عبد الصمد (مرتل)", "ar.abdulbasitmurattal", "https://server7.mp3quran.net/basit"),
    MENSHAWY("menshawy", "محمد صديق المنشاوي", "ar.minshawi", "https://server10.mp3quran.net/minsh"),
    HUSSARY("hussary", "محمود خليل الحصري", "ar.husary", "https://server13.mp3quran.net/hussary"),
    MAHER("almuaiqly", "ماهر المعيقلي", "ar.mahermuaiqly", "https://server12.mp3quran.net/maher"),
    SHURAIM("shuraim", "سعود الشريم", "ar.saoodshuraym", "https://server7.mp3quran.net/shur"),
    SUDAIS("sudais", "عبد الرحمن السديس", "ar.abdurrahmanghamidi", "https://server11.mp3quran.net/sds")
}

class QuranRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)

    fun saveBookmark(surahNumber: Int, ayahNumber: Int, surahName: String) {
        prefs.edit()
            .putInt("bookmark_surah_num", surahNumber)
            .putInt("bookmark_ayah_num", ayahNumber)
            .putString("bookmark_surah_name", surahName)
            .apply()
    }

    fun getBookmarkSurahNumber(): Int = prefs.getInt("bookmark_surah_num", 1)
    fun getBookmarkAyahNumber(): Int = prefs.getInt("bookmark_ayah_num", 1)
    fun getBookmarkSurahName(): String = prefs.getString("bookmark_surah_name", "الفاتحة") ?: "الفاتحة"

    fun getSelectedReciter(): QuranReciter {
        val id = prefs.getString("selected_reciter_id", QuranReciter.ALAFASY.id)
        return QuranReciter.entries.firstOrNull { it.id == id } ?: QuranReciter.ALAFASY
    }

    fun setSelectedReciter(reciter: QuranReciter) {
        prefs.edit().putString("selected_reciter_id", reciter.id).apply()
    }

    // Fetch Ayahs from local assets/quran-uthmani.json (100% Offline)
    suspend fun fetchSurahAyahs(surahNumber: Int): List<Ayah> = withContext(Dispatchers.IO) {
        // Check in-memory cached map first
        surahAyahsCache[surahNumber]?.let { return@withContext it }

        // Parse from local assets/quran-uthmani.json
        val ayahs = loadAyahsFromAssets(surahNumber)
        if (ayahs.isNotEmpty()) {
            surahAyahsCache[surahNumber] = ayahs
            return@withContext ayahs
        }

        return@withContext getOfflineFallbackAyahs(surahNumber)
    }

    private fun loadAyahsFromAssets(surahNumber: Int): List<Ayah> {
        val list = mutableListOf<Ayah>()
        try {
            if (quranDataJsonObject == null) {
                context.assets.open("quran-uthmani.json").use { inputStream ->
                    val jsonText = InputStreamReader(inputStream, Charsets.UTF_8).readText()
                    quranDataJsonObject = JSONObject(jsonText)
                }
            }

            val dataObj = quranDataJsonObject?.optJSONObject("data") ?: return emptyList()
            val surahsArray = dataObj.optJSONArray("surahs") ?: return emptyList()

            for (i in 0 until surahsArray.length()) {
                val surahObj = surahsArray.getJSONObject(i)
                val num = surahObj.optInt("number")
                if (num == surahNumber) {
                    val ayahsArray = surahObj.optJSONArray("ayahs") ?: break
                    for (j in 0 until ayahsArray.length()) {
                        val ayahObj = ayahsArray.getJSONObject(j)
                        val numberInSurah = ayahObj.optInt("numberInSurah", j + 1)
                        var text = ayahObj.optString("text", "").trim()
                        // Clean BOM or invisible zero-width space characters if any
                        text = text.replace("\uFEFF", "").replace("\u200B", "")
                        list.add(Ayah(numberInSurah, text))
                    }
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    companion object {
        private var quranDataJsonObject: JSONObject? = null
        private val surahAyahsCache = mutableMapOf<Int, List<Ayah>>()

        fun getSurahAudioUrl(surahNumber: Int, reciter: QuranReciter = QuranReciter.ALAFASY): String {
            val paddedNumber = String.format(java.util.Locale.US, "%03d", surahNumber)
            return "${reciter.mp3quranUrl}/$paddedNumber.mp3"
        }

        val surahs = listOf(
            Surah(1, "الفاتحة", "Al-Fatiha", 7, "مكية", 1),
            Surah(2, "البقرة", "Al-Baqarah", 286, "مدنية", 2),
            Surah(3, "آل عمران", "Ali 'Imran", 200, "مدنية", 50),
            Surah(4, "النساء", "An-Nisa", 176, "مدنية", 77),
            Surah(5, "المائدة", "Al-Ma'idah", 120, "مدنية", 106),
            Surah(6, "الأنعام", "Al-An'am", 165, "مكية", 128),
            Surah(7, "الأعراف", "Al-A'raf", 206, "مكية", 151),
            Surah(8, "الأنفال", "Al-Anfal", 75, "مدنية", 177),
            Surah(9, "التوبة", "At-Tawbah", 129, "مدنية", 187),
            Surah(10, "يونس", "Yunus", 109, "مكية", 208),
            Surah(11, "هود", "Hud", 123, "مكية", 221),
            Surah(12, "يوسف", "Yusuf", 111, "مكية", 235),
            Surah(13, "الرعد", "Ar-Ra'd", 43, "مدنية", 249),
            Surah(14, "إبراهيم", "Ibrahim", 52, "مكية", 255),
            Surah(15, "الحجر", "Al-Hijr", 99, "مكية", 262),
            Surah(16, "النحل", "An-Nahl", 128, "مكية", 267),
            Surah(17, "الإسراء", "Al-Isra", 111, "مكية", 282),
            Surah(18, "الكهف", "Al-Kahf", 110, "مكية", 293),
            Surah(19, "مريم", "Maryam", 98, "مكية", 305),
            Surah(20, "طه", "Taha", 135, "مكية", 312),
            Surah(21, "الأنبياء", "Al-Anbiya", 112, "مكية", 322),
            Surah(22, "الحج", "Al-Hajj", 78, "مدنية", 332),
            Surah(23, "المؤمنون", "Al-Mu'minun", 118, "مكية", 342),
            Surah(24, "النور", "An-Nur", 64, "مدنية", 350),
            Surah(25, "الفرقان", "Al-Furqan", 77, "مكية", 359),
            Surah(26, "الشعراء", "Ash-Shu'ara", 227, "مكية", 367),
            Surah(27, "النمل", "An-Naml", 93, "مكية", 377),
            Surah(28, "القصص", "Al-Qasas", 88, "مكية", 385),
            Surah(29, "العنكبوت", "Al-'Ankabut", 69, "مكية", 396),
            Surah(30, "الروم", "Ar-Rum", 60, "مكية", 404),
            Surah(31, "لقمان", "Luqman", 34, "مكية", 411),
            Surah(32, "السجدة", "As-Sajdah", 30, "مكية", 415),
            Surah(33, "الأحزاب", "Al-Ahzab", 73, "مدنية", 418),
            Surah(34, "سبأ", "Saba", 54, "مكية", 428),
            Surah(35, "فاطر", "Fatir", 45, "مكية", 434),
            Surah(36, "يس", "Ya-Sin", 83, "مكية", 440),
            Surah(37, "الصافات", "As-Saffat", 182, "مكية", 446),
            Surah(38, "ص", "Sad", 88, "مكية", 453),
            Surah(39, "الزمر", "Az-Zumar", 75, "مكية", 458),
            Surah(40, "غافر", "Ghafir", 85, "مكية", 467),
            Surah(41, "فصلت", "Fussilat", 54, "مكية", 477),
            Surah(42, "الشورى", "Ash-Shura", 53, "مكية", 483),
            Surah(43, "الزخرف", "Az-Zukhruf", 89, "مكية", 489),
            Surah(44, "الدخان", "Ad-Dukhan", 59, "مكية", 496),
            Surah(45, "الجاثية", "Al-Jathiyah", 37, "مكية", 499),
            Surah(46, "الأحقاف", "Al-Ahqaf", 35, "مكية", 502),
            Surah(47, "محمد", "Muhammad", 38, "مدنية", 507),
            Surah(48, "الفتح", "Al-Fath", 29, "مدنية", 511),
            Surah(49, "الحجرات", "Al-Hujurat", 18, "مدنية", 515),
            Surah(50, "ق", "Qaf", 45, "مكية", 518),
            Surah(51, "الذاريات", "Adh-Dhariyat", 60, "مكية", 520),
            Surah(52, "الطور", "At-Tur", 49, "مكية", 523),
            Surah(53, "النجم", "An-Najm", 62, "مكية", 526),
            Surah(54, "القمر", "Al-Qamar", 55, "مكية", 528),
            Surah(55, "الرحمن", "Ar-Rahman", 78, "مدنية", 531),
            Surah(56, "الواقعة", "Al-Waqi'ah", 96, "مكية", 534),
            Surah(57, "الحديد", "Al-Hadid", 29, "مدنية", 537),
            Surah(58, "المجادلة", "Al-Mujadila", 22, "مدنية", 542),
            Surah(59, "الحشر", "Al-Hashr", 24, "مدنية", 545),
            Surah(60, "الممتحنة", "Al-Mumtahanah", 13, "مدنية", 549),
            Surah(61, "الصف", "As-Saff", 14, "مدنية", 551),
            Surah(62, "الجمعة", "Al-Jumu'ah", 11, "مدنية", 553),
            Surah(63, "المنافقون", "Al-Munafiqun", 11, "مدنية", 554),
            Surah(64, "التغابن", "At-Taghabun", 18, "مدنية", 556),
            Surah(65, "الطلاق", "At-Talaq", 12, "مدنية", 558),
            Surah(66, "التحريم", "At-Tahrim", 12, "مدنية", 560),
            Surah(67, "الملك", "Al-Mulk", 30, "مكية", 562),
            Surah(68, "القلم", "Al-Qalam", 52, "مكية", 564),
            Surah(69, "الحاقة", "Al-Haqqah", 52, "مكية", 566),
            Surah(70, "المعارج", "Al-Ma'arij", 44, "مكية", 568),
            Surah(71, "نوح", "Nuh", 28, "مكية", 570),
            Surah(72, "الجن", "Al-Jinn", 28, "مكية", 572),
            Surah(73, "المزمل", "Al-Muzzammil", 20, "مكية", 574),
            Surah(74, "المدثر", "Al-Muddaththir", 56, "مكية", 575),
            Surah(75, "القيامة", "Al-Qiyamah", 40, "مكية", 577),
            Surah(76, "الإنسان", "Al-Insan", 31, "مدنية", 578),
            Surah(77, "المرسلات", "Al-Mursalat", 50, "مكية", 580),
            Surah(78, "النبأ", "An-Naba", 40, "مكية", 582),
            Surah(79, "النازعات", "An-Nazi'at", 46, "مكية", 583),
            Surah(80, "عبس", "Abasa", 42, "مكية", 585),
            Surah(81, "التكوير", "At-Takwir", 29, "مكية", 586),
            Surah(82, "الانفطار", "Al-Infitar", 19, "مكية", 587),
            Surah(83, "المطففين", "Al-Mutaffifin", 36, "مكية", 587),
            Surah(84, "الانشقاق", "Al-Inshiqaq", 25, "مكية", 589),
            Surah(85, "البروج", "Al-Buruj", 22, "مكية", 590),
            Surah(86, "الطارق", "At-Tariq", 17, "مكية", 591),
            Surah(87, "الأعلى", "Al-A'la", 19, "مكية", 591),
            Surah(88, "الغاشية", "Al-Ghashiyah", 26, "مكية", 592),
            Surah(89, "الفجر", "Al-Fajr", 30, "مكية", 593),
            Surah(90, "البلد", "Al-Balad", 20, "مكية", 594),
            Surah(91, "الشمس", "Ash-Shams", 15, "مكية", 595),
            Surah(92, "الليل", "Al-Layl", 21, "مكية", 595),
            Surah(93, "الضحى", "Ad-Duha", 11, "مكية", 596),
            Surah(94, "الشرح", "Ash-Sharh", 8, "مكية", 596),
            Surah(95, "التين", "At-Tin", 8, "مكية", 597),
            Surah(96, "العلق", "Al-'Alaq", 19, "مكية", 597),
            Surah(97, "القدر", "Al-Qadr", 5, "مكية", 598),
            Surah(98, "البينة", "Al-Bayyinah", 8, "مدنية", 598),
            Surah(99, "الزلزلة", "Az-Zalzalah", 8, "مدنية", 599),
            Surah(100, "العاديات", "Al-'Adiyat", 11, "مكية", 599),
            Surah(101, "القارعة", "Al-Qari'ah", 11, "مكية", 600),
            Surah(102, "التكاثر", "At-Takathur", 8, "مكية", 600),
            Surah(103, "العصر", "Al-'Asr", 3, "مكية", 601),
            Surah(104, "الهمزة", "Al-Humazah", 9, "مكية", 601),
            Surah(105, "الفيل", "Al-Fil", 5, "مكية", 601),
            Surah(106, "قريش", "Quraysh", 4, "مكية", 602),
            Surah(107, "الماعون", "Al-Ma'un", 7, "مكية", 602),
            Surah(108, "الكوثر", "Al-Kawthar", 3, "مكية", 602),
            Surah(109, "الكافرون", "Al-Kafirun", 6, "مكية", 603),
            Surah(110, "النصر", "An-Nasr", 3, "مدنية", 603),
            Surah(111, "المسد", "Al-Masad", 5, "مكية", 603),
            Surah(112, "الإخلاص", "Al-Ikhlas", 4, "مكية", 604),
            Surah(113, "الفلق", "Al-Falaq", 5, "مكية", 604),
            Surah(114, "الناس", "An-Nas", 6, "مكية", 604)
        )

        private fun getOfflineFallbackAyahs(surahNumber: Int): List<Ayah> {
            return when (surahNumber) {
                1 -> listOf(
                    Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
                    Ayah(2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ"),
                    Ayah(3, "الرَّحْمَٰنِ الرَّحِيمِ"),
                    Ayah(4, "مَالِكِ يَوْمِ الدِّينِ"),
                    Ayah(5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"),
                    Ayah(6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ"),
                    Ayah(7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ")
                )
                112 -> listOf(
                    Ayah(1, "قُلْ هُوَ اللَّهُ أَحَدٌ"),
                    Ayah(2, "اللَّهُ الصَّمَدُ"),
                    Ayah(3, "لَمْ يَلِدْ وَلَمْ يُولَدْ"),
                    Ayah(4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ")
                )
                113 -> listOf(
                    Ayah(1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ"),
                    Ayah(2, "مِن شَرِّ مَا خَلَقَ"),
                    Ayah(3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ"),
                    Ayah(4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ"),
                    Ayah(5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ")
                )
                114 -> listOf(
                    Ayah(1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ"),
                    Ayah(2, "مَلِكِ النَّاسِ"),
                    Ayah(3, "إِلَٰهِ النَّاسِ"),
                    Ayah(4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ"),
                    Ayah(5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ"),
                    Ayah(6, "مِنَ الْجِنَّةِ وَالنَّاسِ")
                )
                else -> listOf(
                    Ayah(1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"),
                    Ayah(2, "اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ"),
                    Ayah(3, "خَلَقَ الْإِنسَانَ مِنْ عَلَقٍ"),
                    Ayah(4, "اقْرَأْ وَرَبُّكَ الْأَكْرَمُ"),
                    Ayah(5, "الَّذِي عَلَّمَ بِالْقَلَمِ")
                )
            }
        }
    }
}
