package com.threeal.smarttourism

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class ENUCoordinate private constructor(val ecef: ECEFCoordinate, private val data: DoubleArray) {
    companion object {

        fun fromCurrentToTargetECEF(
            currentECEF: ECEFCoordinate, targetECEF: ECEFCoordinate
        ): ENUCoordinate {
            val radLat = Math.toRadians(currentECEF.location.latitude)
            val radLon = Math.toRadians(currentECEF.location.longitude)

            val cLat = cos(radLat)
            val sLat = sin(radLat)
            val cLon = cos(radLon)
            val sLon = sin(radLon)

            val dx = currentECEF.x - targetECEF.x
            val dy = currentECEF.y - targetECEF.y
            val dz = currentECEF.z - targetECEF.z

            val east = (-sLon * dx) + (cLon * dy)
            val north = (-sLat * cLon * dx) - (sLat * sLon * dy) + (cLat * dz)
            val up = (cLat * cLon * dx) + (cLat * sLon * dy) + (sLat * dz)

            return ENUCoordinate(
                targetECEF,
                doubleArrayOf(east, north, up)
            )
        }
    }

    val x: Double get() = data[0]
    val y: Double get() = data[1]
    val z: Double get() = data[2]
}