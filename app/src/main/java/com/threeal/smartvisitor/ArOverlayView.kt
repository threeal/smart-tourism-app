package com.threeal.smartvisitor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import android.opengl.Matrix
import android.view.View
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ArOverlayView constructor(context: Context) : View(context) {
    private lateinit var rotatedProjectionMatrix: FloatArray
    private lateinit var currentLocation: Location

    private val arPoints = listOf<ArPoint>(
        ArPoint("Musholla Al Ikhlas", -6.871587, 112.347071, 48.0),
        ArPoint("Masjid Al Muttaqin", -6.872197, 112.349059, 48.0),
        ArPoint("Omahku", -6.871759, 112.347909, 48.0),
        ArPoint("Bengkel", -6.872025, 112.348004, 48.0),
        ArPoint("Omah Sebelah", -6.871644, 112.347705, 48.0),
        ArPoint("Omah Liyane", -6.871793, 112.348261, 48.0)
    )

    fun updateRotatedProjectionMatrix(rotatedProjectionMatrix: FloatArray) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix
        invalidate()
    }

    fun updateCurrentLocation(currentLocation: Location) {
        this.currentLocation = currentLocation
        invalidate()
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = 60f
    }

    private val cameraCoordinateVector = FloatArray(4)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        arPoints.forEach {
            val currentLocationInECEF = LocationHelper.WSG84toECF(currentLocation)
            val pointInECEF = LocationHelper.WSG84toECF(it.location)
            val pointInENU = LocationHelper.ECEFtoENU(
                currentLocation, currentLocationInECEF, pointInECEF
            )

            Matrix.multiplyMV(
                cameraCoordinateVector, 0, rotatedProjectionMatrix, 0,
                pointInENU, 0
            )

            if (cameraCoordinateVector[2] < 0) {
                val x =
                    (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * width
                val y =
                    (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * height

                var sum = 0.0;
                for (i in 0..2) {
                    sum += (pointInECEF[i] - currentLocationInECEF[i]).toDouble().pow(2.0);
                }

                val distance = sqrt(sum).roundToInt()
                val distanceText = when {
                    distance > 1000 -> {
                        "${distance / 1000} KM"
                    }
                    else -> {
                        "$distance M"
                    }
                }

                val pointText = "${it.name} ($distanceText)"

                canvas?.drawCircle(x, y, 20f, paint)
                canvas?.drawText(pointText, x - (30 * pointText.length / 2), y - 80, paint)
            }
        }
    }
}