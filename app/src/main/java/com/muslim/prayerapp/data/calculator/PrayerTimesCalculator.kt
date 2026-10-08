package com.muslim.prayerapp.data.calculator

import com.muslim.prayerapp.data.model.CalculationMethod
import com.muslim.prayerapp.data.model.DayPrayerSchedule
import com.muslim.prayerapp.data.model.JuristicMethod
import com.muslim.prayerapp.data.model.PrayerTime
import com.muslim.prayerapp.data.model.PrayerType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

object PrayerTimesCalculator {

    data class Coordinates(val latitude: Double, val longitude: Double)

    fun calculateDailySchedule(
        coordinates: Coordinates,
        calendar: Calendar = Calendar.getInstance(),
        method: CalculationMethod = CalculationMethod.EGYPTIAN,
        juristic: JuristicMethod = JuristicMethod.STANDARD,
        offsets: Map<PrayerType, Int> = emptyMap(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): DayPrayerSchedule {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val tzOffset = timeZone.getOffset(calendar.timeInMillis) / 3600000.0

        val julianDate = julianDate(year, month, day) - coordinates.longitude / (15.0 * 24.0)

        // Solar parameters
        val d = julianDate - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = fixHour(radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))) / 15.0)
        val eqt = q / 15.0 - ra
        val decl = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))

        // Dhuhr (Midday)
        val dhuhrHour = fixHour(12.0 + tzOffset - coordinates.longitude / 15.0 - eqt) + (1.0 / 60.0) // 1 min Zawal margin

        // Sunrise & Sunset (Standard 0.833° refraction)
        val sunriseHour = dhuhrHour - sunAngleTime(coordinates.latitude, decl, 0.833)
        val sunsetHour = dhuhrHour + sunAngleTime(coordinates.latitude, decl, 0.833)

        // Fajr
        val fajrHour = dhuhrHour - sunAngleTime(coordinates.latitude, decl, method.fajrAngle)

        // Asr (Shadow multiplier: Standard=1, Hanafi=2)
        val asrAltDeg = radToDeg(atan(1.0 / (juristic.shadowMultiplier + tan(degToRad(abs(coordinates.latitude - decl))))))
        val asrHour = dhuhrHour + asrTime(coordinates.latitude, decl, asrAltDeg)

        // Maghrib
        val maghribHour = sunsetHour

        // Isha
        val ishaHour = if (method.ishaIntervalMinutes > 0) {
            maghribHour + (method.ishaIntervalMinutes / 60.0)
        } else {
            dhuhrHour + sunAngleTime(coordinates.latitude, decl, method.ishaAngle)
        }

        // Build PrayerTime list with user minute offsets applied
        val prayers = listOf(
            createPrayerTime(PrayerType.FAJR, fajrHour, calendar, offsets[PrayerType.FAJR] ?: 0),
            createPrayerTime(PrayerType.SUNRISE, sunriseHour, calendar, offsets[PrayerType.SUNRISE] ?: 0),
            createPrayerTime(PrayerType.DHUHR, dhuhrHour, calendar, offsets[PrayerType.DHUHR] ?: 0),
            createPrayerTime(PrayerType.ASR, asrHour, calendar, offsets[PrayerType.ASR] ?: 0),
            createPrayerTime(PrayerType.MAGHRIB, maghribHour, calendar, offsets[PrayerType.MAGHRIB] ?: 0),
            createPrayerTime(PrayerType.ISHA, ishaHour, calendar, offsets[PrayerType.ISHA] ?: 0)
        )

        val now = System.currentTimeMillis()
        var nextPrayer: PrayerTime? = null
        var remainingMillis = 0L

        for (prayer in prayers) {
            if (prayer.timeMillis > now) {
                nextPrayer = prayer.copy(isNext = true)
                remainingMillis = prayer.timeMillis - now
                break
            }
        }

        // If all prayers passed today, next is Fajr tomorrow
        if (nextPrayer == null) {
            val tomorrowCal = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val tomorrowSchedule = calculateDailySchedule(coordinates, tomorrowCal, method, juristic, offsets, timeZone)
            nextPrayer = tomorrowSchedule.prayers.first().copy(isNext = true)
            remainingMillis = nextPrayer.timeMillis - now
        }

        val updatedPrayers = prayers.map { prayer ->
            prayer.copy(
                isNext = prayer.type == nextPrayer.type && prayer.timeMillis == nextPrayer.timeMillis,
                isCurrent = isCurrentPrayer(prayer, prayers, now)
            )
        }

        val gregorianFmt = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("ar"))
        val gregorianStr = gregorianFmt.format(calendar.time)

        return DayPrayerSchedule(
            date = calendar.time,
            hijriDateFormatted = approximateHijriDate(calendar),
            gregorianDateFormatted = gregorianStr,
            prayers = updatedPrayers,
            nextPrayer = nextPrayer,
            remainingMillisUntilNext = max(0L, remainingMillis)
        )
    }

    private fun isCurrentPrayer(prayer: PrayerTime, all: List<PrayerTime>, now: Long): Boolean {
        val index = all.indexOfFirst { it.type == prayer.type }
        if (index == -1) return false
        val startTime = prayer.timeMillis
        val endTime = if (index < all.size - 1) all[index + 1].timeMillis else Long.MAX_VALUE
        return now in startTime until endTime
    }

    private fun createPrayerTime(
        type: PrayerType,
        hourFraction: Double,
        calendar: Calendar,
        minuteOffset: Int
    ): PrayerTime {
        val cal = calendar.clone() as Calendar
        val totalMinutes = (fixHour(hourFraction) * 60.0).roundToInt() + minuteOffset
        val hour = (totalMinutes / 60) % 24
        val minute = totalMinutes % 60

        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val timeFormatter = SimpleDateFormat("hh:mm a", Locale("ar"))
        val formatted = timeFormatter.format(cal.time)

        return PrayerTime(
            type = type,
            timeMillis = cal.timeInMillis,
            formattedTime = formatted
        )
    }

    // Hour angle for sun angle below horizon (e.g. Fajr, Isha, Sunrise)
    private fun sunAngleTime(lat: Double, decl: Double, angleBelowHorizon: Double): Double {
        val h = -angleBelowHorizon
        val numerator = sin(degToRad(h)) - sin(degToRad(lat)) * sin(degToRad(decl))
        val denominator = cos(degToRad(lat)) * cos(degToRad(decl))
        val value = (numerator / denominator).coerceIn(-1.0, 1.0)
        return radToDeg(acos(value)) / 15.0
    }

    // Hour angle for Asr when sun altitude above horizon is asrAltDeg
    private fun asrTime(lat: Double, decl: Double, asrAltDeg: Double): Double {
        val numerator = sin(degToRad(asrAltDeg)) - sin(degToRad(lat)) * sin(degToRad(decl))
        val denominator = cos(degToRad(lat)) * cos(degToRad(decl))
        val value = (numerator / denominator).coerceIn(-1.0, 1.0)
        return radToDeg(acos(value)) / 15.0
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        var angle = a - 360.0 * floor(a / 360.0)
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun fixHour(h: Double): Double {
        var hour = h - 24.0 * floor(h / 24.0)
        if (hour < 0) hour += 24.0
        return hour
    }

    private fun degToRad(d: Double): Double = d * (PI / 180.0)
    private fun radToDeg(r: Double): Double = r * (180.0 / PI)

    fun approximateHijriDate(calendar: Calendar): String {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH) + 1
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        val jd = julianDate(y, m, d)
        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0)
        val lPrime = l - 10631 * n + 354
        val j = (floor((10985 - lPrime) / 5316.0)) * (floor((50 * lPrime) / 17719.0)) +
                (floor(lPrime / 5670.0)) * (floor((43 * lPrime) / 15238.0))
        val lDoublePrime = lPrime - (floor((30 - j) / 15.0)) * (floor((17719 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238 * j) / 43.0)) + 29
        val hMonth = floor((24 * lDoublePrime) / 709.0).toInt()
        val hDay = (lDoublePrime - floor((709 * hMonth) / 24.0)).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val hijriMonths = arrayOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
            "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
            "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
        )
        val monthName = hijriMonths.getOrElse(hMonth - 1) { "رمضان" }
        return "$hDay $monthName $hYear هـ"
    }
}
