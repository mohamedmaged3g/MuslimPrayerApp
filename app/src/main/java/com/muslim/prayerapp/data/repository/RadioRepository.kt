package com.muslim.prayerapp.data.repository

import com.muslim.prayerapp.data.model.RadioStation

object RadioRepository {

    val stations = listOf(
        RadioStation(
            id = "cairo_quran",
            nameArabic = "إذاعة القرآن الكريم من القاهرة",
            streamUrl = "https://stream.radiojar.com/8s5u5tpdtwzuv",
            description = "بث مباشر رسمي - تلاوات وبرامج إذاعة القرآن الكريم المصرية",
            country = "مصر"
        ),

        RadioStation(
            id = "makkah_quran",
            nameArabic = "إذاعة القرآن الكريم من مكة المكرمة",
            streamUrl = "https://stream.radiojar.com/0tpy1h0kxtzuv",
            description = "تلاوات عطرة مباشرة من أروقة المسجد الحرام",
            country = "المملكة العربية السعودية"
        ),
        RadioStation(
            id = "recitations_khushoo",
            nameArabic = "إذاعة تلاوات خاشعة ومؤثرة",
            streamUrl = "https://qurango.net/radio/tarateel",
            description = "تلاوات مختارة لكبار القراء بأعذب الأصوات",
            country = "العالم الإسلامي"
        ),
        RadioStation(
            id = "azkar_radio",
            nameArabic = "إذاعة الأذكار والرقية الشرعية",
            streamUrl = "https://qurango.net/radio/athkar_sabah",
            description = "أذكار الصباح والمساء والرقية الشرعية مكررة على مدار الساعة",
            country = "العالم الإسلامي"
        ),
        RadioStation(
            id = "abdulbasit",
            nameArabic = "إذاعة الشيخ عبد الباسط عبد الصمد",
            streamUrl = "https://backup.qurango.net/radio/abdulbasit_abdulsamad_mojawwad",
            description = "تلاوات مجودة نادرة لصوت مكة الشيخ عبد الباسط",
            country = "مصر"
        ),
        RadioStation(
            id = "alafasy",
            nameArabic = "إذاعة الشيخ مشاري راشد العفاسي",
            streamUrl = "https://backup.qurango.net/radio/mishary_alafasi",
            description = "المصحف المرتل برواية حفص عن عاصم",
            country = "الكويت"
        )
    )
}
