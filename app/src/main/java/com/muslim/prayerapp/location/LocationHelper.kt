package com.muslim.prayerapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import com.muslim.prayerapp.data.repository.SettingsRepository
import com.muslim.prayerapp.service.AzanAlarmScheduler
import com.muslim.prayerapp.widget.PrayerTimesWidget
import java.util.Locale

object LocationHelper {

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onSuccess: (String, Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onError("خدمة الموقع غير متوفرة في الجهاز")
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            onError("يرجى تفعيل خدمة GPS وتحديد الموقع في الهاتف")
            return
        }

        var lastLocation: Location? = null

        try {
            if (isGpsEnabled) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (lastLocation == null && isNetworkEnabled) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (lastLocation != null) {
            processLocation(context, lastLocation, onSuccess)
        } else {
            // Fallback to request location update or error message
            onError("تعذر جلب إحداثيات الموقع حالياً. يرجى التأكد من فتح الـ GPS في مكان مفتوح.")
        }
    }

    private fun processLocation(
        context: Context,
        location: Location,
        onSuccess: (String, Double, Double) -> Unit
    ) {
        val lat = location.latitude
        val lng = location.longitude
        var cityName = "موقعي الحالي"

        try {
            val geocoder = Geocoder(context, Locale("ar"))
            @Suppress("DEPRECATION")
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locality = address.locality ?: address.subAdminArea ?: address.adminArea ?: ""
                val country = address.countryName ?: ""
                cityName = if (locality.isNotEmpty() && country.isNotEmpty()) {
                    "$locality، $country"
                } else if (locality.isNotEmpty()) {
                    locality
                } else if (country.isNotEmpty()) {
                    country
                } else {
                    "موقعي الحالي"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Save location to preferences
        val settingsRepo = SettingsRepository(context)
        settingsRepo.setLocation(lat, lng, cityName)

        // Reschedule alarms with new location
        AzanAlarmScheduler.scheduleAllUpcomingPrayers(context)

        // Update home screen widget
        PrayerTimesWidget.updateWidget(context)

        onSuccess(cityName, lat, lng)
    }
}
