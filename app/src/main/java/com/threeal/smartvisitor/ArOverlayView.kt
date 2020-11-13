package com.threeal.smartvisitor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import android.opengl.Matrix
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ArOverlayView constructor(context: Context) : View(context) {
    private var rotatedProjectionMatrix: FloatArray? = null
    private var currentLocation: Location? = null

    private val places = listOf<Place>(
        Place("Taman", Place.Type.Garden, "", -6.869847, 112.347112, 0.0),
        Place("Masjid", Place.Type.Restroom, "", -6.869712, 112.347698, 0.0),
        Place("Parkiran", Place.Type.ParkingArea, "", -6.869839, 112.347405, 0.0),
        Place("Toko", Place.Type.GiftShop, "", -6.870136, 112.347188, 0.0),
        Place("Toko", Place.Type.GiftShop, "", -6.870163, 112.346959, 0.0)
    )

    private var placePoints = mutableListOf<PlacePoint>()

    private fun updatePlacePoints(currentLocation: Location, rotatedProjectionMatrix: FloatArray) {
        placePoints.clear()

        val currentLocationInECEF = LocationHelper.WSG84toECF(currentLocation)

        places.forEach { place ->
            val pointInECEF = LocationHelper.WSG84toECF(place.location)
            val pointInENU = LocationHelper.ECEFtoENU(
                currentLocation, currentLocationInECEF, pointInECEF
            )

            Matrix.multiplyMV(
                cameraCoordinateVector, 0, rotatedProjectionMatrix, 0,
                pointInENU, 0
            )

            if (cameraCoordinateVector[2] < 0) {
                placePoints.add(
                    PlacePoint(
                        place,
                        (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * width,
                        (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * height,
                        sqrt(listOf(0, 1, 2).sumByDouble {
                            (pointInECEF[it] - currentLocationInECEF[it]).toDouble().pow(2.0)
                        })
                    )
                )
            }
        }
    }

    fun updateRotatedProjectionMatrix(rotatedProjectionMatrix: FloatArray) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix
        if (this.currentLocation != null) {
            updatePlacePoints(this.currentLocation!!, this.rotatedProjectionMatrix!!)
        }
        invalidate()
    }

    fun updateCurrentLocation(currentLocation: Location) {
        this.currentLocation = currentLocation
        if (this.rotatedProjectionMatrix != null) {
            updatePlacePoints(this.currentLocation!!, this.rotatedProjectionMatrix!!)
        }
        invalidate()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.HSVToColor(64, floatArrayOf(0f, 0f, 0f))
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = 60f
    }

    private val cameraCoordinateVector = FloatArray(4)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        placePoints.forEach { placePoint ->
            canvas?.drawCircle(placePoint.x, placePoint.y, 48f, paint)

            val icon = ResourcesCompat.getDrawable(
                resources,
                when (placePoint.place.type) {
                    Place.Type.Information -> R.drawable.icon_information
                    Place.Type.Garden -> R.drawable.icon_garden
                    Place.Type.Rides -> R.drawable.icon_rides
                    Place.Type.ParkingArea -> R.drawable.icon_parking_area
                    Place.Type.Restroom -> R.drawable.icon_restroom
                    Place.Type.GiftShop -> R.drawable.icon_gift_shop
                    Place.Type.FoodCourt -> R.drawable.icon_food_court
                },
                null
            )

            icon?.setBounds(
                placePoint.x.roundToInt() - 32,
                placePoint.y.roundToInt() - 32,
                placePoint.x.roundToInt() + 32,
                placePoint.y.roundToInt() + 32
            )

            icon?.draw(canvas!!)

//            val distance = placePoint.distance.roundToInt()
//            val distanceText = when {
//                distance > 1000 -> {
//                    "${distance / 1000} KM"
//                }
//                else -> {
//                    "$distance M"
//                }
//            }
//            val pointText = "${placePoint.place.name} ($distanceText)"
//
//            canvas?.drawText(
//                pointText,
//                placePoint.x - (30 * pointText.length / 2),
//                placePoint.y - 80,
//                paint
//            )
        }
    }
}