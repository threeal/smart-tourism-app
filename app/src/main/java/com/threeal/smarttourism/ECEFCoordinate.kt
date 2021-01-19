package com.threeal.smarttourism

import android.location.Location
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ECEFCoordinate private constructor(val location: Location, private val data: DoubleArray) {
    companion object {
        private const val WGS84_A: Double = 6378137.0
        private const val WGS84_E2: Double = 0.00669437999014

        fun fromLocation(location: Location): ECEFCoordinate {
            val radLat = Math.toRadians(location.latitude)
            val radLon = Math.toRadians(location.longitude)

            val cLat = cos(radLat)
            val sLat = sin(radLat)
            val cLon = cos(radLon)
            val sLon = sin(radLon)

            val n = WGS84_A / sqrt(1.0 - WGS84_E2 * sLat * sLat)

            // surpass altitude
            // val x = (n + location.altitude) * cLat * cLon
            // val y = (n + location.altitude) * cLat * sLon
            // val z = (n * (1.0 - WGS84_E2) + location.altitude) * sLat

            val x = n * cLat * cLon
            val y = n * cLat * sLon
            val z = n * (1.0 - WGS84_E2) * sLat

            return ECEFCoordinate(location, doubleArrayOf(x, y, z))
        }
    }

    fun distanceTo(other: ECEFCoordinate): Double {
        val x2 = (other.x - x).pow(2)
        val y2 = (other.y - y).pow(2)
        val z2 = (other.z - z).pow(2)

        return sqrt(x2 + y2 + z2)
    }

    val x: Double get() = data[0]
    val y: Double get() = data[1]
    val z: Double get() = data[2]
}