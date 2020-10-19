package com.threeal.smartvisitor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import android.opengl.Matrix
import android.view.View

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

                canvas?.drawCircle(x, y, 30f, paint)
                canvas?.drawText(it.name, x - (30 * it.name.length / 2), y - 80, paint)
            }
        }
    }
}