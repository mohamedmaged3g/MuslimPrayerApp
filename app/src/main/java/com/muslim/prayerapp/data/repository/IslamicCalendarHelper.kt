package com.muslim.prayerapp.data.repository

import com.muslim.prayerapp.data.model.HijriDate
import com.muslim.prayerapp.data.model.IslamicEvent
import java.util.Calendar

object IslamicCalendarHelper {

    val hijriMonthsArabic = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    // Major Islamic Events
    val majorEvents = listOf(
        IslamicEvent("رأس السنة الهجرية", "بداية العام الهجري الجديد 1 محرم", 1, 1, "محرم"),
        IslamicEvent("يوم عاشوراء", "صيام يوم عاشوراء ويكفر السنة الماضية", 10, 1, "محرم"),
        IslamicEvent("المولد النبوي الشريف", "ذكرى مولد النبي محمد ﷺ 12 ربيع الأول", 12, 3, "ربيع الأول"),
        IslamicEvent("ليلة الإسراء والمعراج", "ذكرى الإسراء والمعراج 27 رجب", 27, 7, "رجب"),
        IslamicEvent("ليلة النصف من شعبان", "ليلة مباركة يُستحب فيها الدعاء والقيام", 15, 8, "شعبان"),
        IslamicEvent("بداية شهر رمضان المبارك", "شهر الصيام والقرآن والرحمة", 1, 9, "رمضان"),
        IslamicEvent("ليلة القدر المباركة", "خير من ألف شهر (العشر الأواخر من رمضان)", 27, 9, "رمضان"),
        IslamicEvent("عيد الفطر المبارك", "جائزة الصائمين بانتهاء شهر رمضان (1 شوال)", 1, 10, "شوال"),
        IslamicEvent("بداية أشهر الحج", "بداية شهر ذي القعدة المبارك", 1, 11, "ذو القعدة"),
        IslamicEvent("يوم عرفة المبارك", "خير أيام السنة وصيامه يكفر سنتين", 9, 12, "ذو الحجة"),
        IslamicEvent("عيد الأضحى المبارك", "عيد النحر والتكبير والأضحية (10 ذو الحجة)", 10, 12, "ذو الحجة")
    )

    /**
     * Approximate Hijri Date conversion algorithm (Umm al-Qura approximation)
     */
    fun getTodayHijriDate(): HijriDate {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        // Julian Day calculation
        var a = (14 - month) / 12
        var y = year + 4800 - a
        var m = month + 12 * a - 3
        var jd = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045

        val l = jd - 1948440 + 10632
        val n = (l - 1) / 10631
        val l1 = l - 10631 * n + 354
        val j = ((10985 - l1) / 5316) * ((50 * l1) / 17719) + ((l1 / 5670) * ((43 * l1) / 15238))
        val l2 = l1 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 30) * ((15238 * j) / 43) + 29
        val hMonth = (24 * l2) / 709
        val hDay = l2 - (709 * hMonth) / 24
        val hYear = 30 * n + j - 30

        val monthIdx = (hMonth - 1).coerceIn(0, 11)
        val monthName = hijriMonthsArabic[monthIdx]

        return HijriDate(
            day = hDay.coerceIn(1, 30),
            month = hMonth.coerceIn(1, 12),
            monthNameArabic = monthName,
            year = hYear
        )
    }

    /**
     * Get upcoming Islamic events from current Hijri date
     */
    fun getUpcomingEvents(): List<Pair<IslamicEvent, Int>> {
        val currentHijri = getTodayHijriDate()
        val results = mutableListOf<Pair<IslamicEvent, Int>>()

        for (event in majorEvents) {
            var daysRemaining = (event.hijriMonth - currentHijri.month) * 30 + (event.hijriDay - currentHijri.day)
            if (daysRemaining < 0) {
                daysRemaining += 354 // Next Hijri year
            }
            results.add(Pair(event, daysRemaining))
        }

        return results.sortedBy { it.second }
    }

    /**
     * Check if today is one of the White Days (الأيام البيض 13, 14, 15)
     */
    fun isWhiteDay(): Boolean {
        val today = getTodayHijriDate()
        return today.day in 13..15
    }
}
