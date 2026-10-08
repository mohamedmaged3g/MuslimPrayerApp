package com.muslim.prayerapp.data.repository

import java.util.Calendar

data class WisdomItem(
    val titleArabic: String,
    val textArabic: String,
    val sourceArabic: String,
    val type: WisdomType
)

enum class WisdomType { AYAH, HADITH }

object DailyWisdomRepository {

    val wisdoms = listOf(
        WisdomItem(
            titleArabic = "آية اليوم",
            textArabic = "﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾",
            sourceArabic = "سورة الرعد - الآية 28",
            type = WisdomType.AYAH
        ),
        WisdomItem(
            titleArabic = "حديث اليوم",
            textArabic = "«مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا، سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ»",
            sourceArabic = "رواه مسلم",
            type = WisdomType.HADITH
        ),
        WisdomItem(
            titleArabic = "آية اليوم",
            textArabic = "﴿فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ﴾",
            sourceArabic = "سورة البقرة - الآية 152",
            type = WisdomType.AYAH
        ),
        WisdomItem(
            titleArabic = "حديث اليوم",
            textArabic = "«كَلِمَتَانِ خَفِيفَتَانِ عَلَى اللِّسَانِ، ثَقِيلَتَانِ فِي الْمِيزَانِ، حَبِيبَتَانِ إِلَى الرَّحْمَنِ: سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ»",
            sourceArabic = "صحيح البخاري ومسلم",
            type = WisdomType.HADITH
        ),
        WisdomItem(
            titleArabic = "آية اليوم",
            textArabic = "﴿وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ﴾",
            sourceArabic = "سورة البقرة - الآية 186",
            type = WisdomType.AYAH
        ),
        WisdomItem(
            titleArabic = "حديث اليوم",
            textArabic = "«إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى»",
            sourceArabic = "صحيح البخاري",
            type = WisdomType.HADITH
        ),
        WisdomItem(
            titleArabic = "آية اليوم",
            textArabic = "﴿إِنَّ مَعَ الْعُسْرِ يُسْرًا﴾",
            sourceArabic = "سورة الشرح - الآية 6",
            type = WisdomType.AYAH
        ),
        WisdomItem(
            titleArabic = "حديث اليوم",
            textArabic = "«خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ»",
            sourceArabic = "صحيح البخاري",
            type = WisdomType.HADITH
        )
    )

    fun getTodayWisdom(): WisdomItem {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1) % wisdoms.size
        return wisdoms[index]
    }

    fun formatShareText(item: WisdomItem): String {
        return """
            🌸 ${item.titleArabic} 🌸
            
            ${item.textArabic}
            
            📌 المصدر: ${item.sourceArabic}
            
            📱 تم المشاركة عبر تطبيق (أمل - الأذان والأذكار)
        """.trimIndent()
    }
}
