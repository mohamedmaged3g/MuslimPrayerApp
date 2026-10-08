package com.muslim.prayerapp.data.calculator

import kotlin.math.*

object QiblaCalculator {

    // Sacred Kaaba coordinates in Makkah
    private const val KAABA_LATITUDE = 21.422487
    private const val KAABA_LONGITUDE = 39.826206

    /**
     * Calculates the bearing (in degrees from True North, 0..360) towards the Kaaba.
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Float {
        val lat1 = Math.toRadians(userLat)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLng = Math.toRadians(KAABA_LONGITUDE - userLng)

        val y = sin(deltaLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLng)

        val bearingRad = atan2(y, x)
        var bearingDeg = Math.toDegrees(bearingRad).toFloat()

        if (bearingDeg < 0) {
            bearingDeg += 360f
        }
        return bearingDeg
    }

    /**
     * Calculates relative needle angle given the compass heading and Qibla bearing.
     */
    fun calculateNeedleAngle(deviceHeading: Float, qiblaBearing: Float): Float {
        var angle = qiblaBearing - deviceHeading
        while (angle < -180f) angle += 360f
        while (angle > 180f) angle -= 360f
        return angle
    }

    /**
     * Determines whether user device is facing directly towards Kaaba within tolerance (e.g. +-4 degrees)
     */
    fun isFacingQibla(needleAngle: Float, tolerance: Float = 4f): Boolean {
        return abs(needleAngle) <= tolerance
    }
}
