package com.threeal.smarttourism

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.opengl.Matrix
import androidx.core.content.res.ResourcesCompat
import kotlin.math.roundToInt

class PlacePoint constructor(
    val place: Place,
    val enu: ENUCoordinate,
    private val x: Float,
    private val y: Float,
    private val z: Float
) {
    companion object {

        const val CIRCLE_SIZE = 48

        private val defaultPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.HSVToColor(64, floatArrayOf(0f, 0f, 0f))
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 60f
        }

        fun fromProjectionOfPlaceEnu(
            projectionMatrix: ProjectionMatrix,
            placeENU: PlaceENU
        ): PlacePoint {
            val projectedPoint = floatArrayOf(
                placeENU.enu.x.toFloat(),
                placeENU.enu.y.toFloat(),
                placeENU.enu.z.toFloat(),
                1f
            )

            Matrix.multiplyMV(
                projectedPoint, 0,
                projectionMatrix.toArray(), 0,
                projectedPoint, 0
            )

            return PlacePoint(
                placeENU.place,
                placeENU.enu,
                (0.5f + projectedPoint[0] / projectedPoint[3]) * projectionMatrix.width,
                (0.5f - projectedPoint[1] / projectedPoint[3]) * projectionMatrix.height,
                -projectedPoint[2]
            )
        }
    }

    fun touching(x: Float, y: Float, selected: Boolean = false): Boolean {
        val circleSize = if (selected) CIRCLE_SIZE * 4 / 3 else CIRCLE_SIZE
        return (x >= this.x - circleSize && x <= this.x + circleSize
                && y >= this.y - circleSize && y <= this.y + circleSize)
    }

    fun draw(context: Context, canvas: Canvas, selected: Boolean = false) {
        if (z > 0) {
            val circleSize = if (selected) CIRCLE_SIZE * 4 / 3 else CIRCLE_SIZE
            canvas.drawCircle(x, y, circleSize.toFloat(), defaultPaint)

            val icon = ResourcesCompat.getDrawable(
                context.resources,
                when (place.type) {
                    Place.Type.Information -> R.drawable.icon_information
                    Place.Type.Gallery -> R.drawable.icon_information
                    Place.Type.Garden -> R.drawable.icon_garden
                    Place.Type.Rides -> R.drawable.icon_rides
                    Place.Type.ParkingArea -> R.drawable.icon_parking_area
                    Place.Type.Restroom -> R.drawable.icon_restroom
                    Place.Type.GiftShop -> R.drawable.icon_gift_shop
                    Place.Type.FoodCourt -> R.drawable.icon_food_court
                    Place.Type.Unknown -> R.drawable.icon_information
                },
                null
            )

            icon?.let {
                val iconSize = if (selected) CIRCLE_SIZE else CIRCLE_SIZE * 3 / 4
                it.setBounds(
                    x.roundToInt() - iconSize,
                    y.roundToInt() - iconSize,
                    x.roundToInt() + iconSize,
                    y.roundToInt() + iconSize
                )

                it.draw(canvas)
            }
        }
    }
}