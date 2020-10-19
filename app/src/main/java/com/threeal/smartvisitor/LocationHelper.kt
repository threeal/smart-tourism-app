package com.threeal.smartvisitor

import android.location.Location
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationHelper {
    companion object {
        private const val WGS84_A: Double = 6378137.0
        private const val WGS84_E2: Double = 0.00669437999014

        fun WSG84toECF(location: Location): FloatArray {
            val radLat = Math.toRadians(location.latitude)
            val radLon = Math.toRadians(location.longitude)

            val cLat = cos(radLat)
            val sLat = sin(radLat)
            val cLon = cos(radLon)
            val sLon = sin(radLon)

            val n = WGS84_A / sqrt(1.0 - WGS84_E2 * sLat * sLat)

            val x = (n + location.altitude) * cLat * cLon
            val y = (n + location.altitude) * cLat * sLon
            val z = (n * (1.0 - WGS84_E2) + location.altitude) * sLat

            return floatArrayOf(x.toFloat(), y.toFloat(), z.toFloat())
        }

        fun ECEFtoENU(
            currentLocation: Location, ecefCurrentLocation: FloatArray, ecefPOI: FloatArray
        ): FloatArray {
            val radLat = Math.toRadians(currentLocation.latitude)
            val radLon = Math.toRadians(currentLocation.longitude)

            val cLat = cos(radLat)
            val sLat = sin(radLat)
            val cLon = cos(radLon)
            val sLon = sin(radLon)

            val dx = ecefCurrentLocation[0] - ecefPOI[0]
            val dy = ecefCurrentLocation[1] - ecefPOI[1]
            val dz = ecefCurrentLocation[2] - ecefPOI[2]

            val east = (-sLon * dx) + (cLon * dy)
            val north = (-sLat * cLon * dx) - (sLat * sLon * dy) + (cLat * dz)
            val up = (cLat * cLon * dx) + (cLat * sLon * dy) + (sLat * dz)

            return floatArrayOf(east.toFloat(), north.toFloat(), up.toFloat(), 1f)
        }
    }
}