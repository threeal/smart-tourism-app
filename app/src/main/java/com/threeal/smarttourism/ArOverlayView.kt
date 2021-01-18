package com.threeal.smarttourism

import android.app.Activity
import android.graphics.Canvas
import android.location.Location
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class ArOverlayView constructor(private val activity: Activity) :
    View(activity) {

    private val frameLayout: FrameLayout = activity.findViewById(R.id.arFrameLayout)

    private var projectionMatrix: ProjectionMatrix? = null
    private var currentLocation: Location? = null

    private var places = listOf<Place>()
    private var placeENUs = listOf<PlaceENU>()
    private var placePoints = listOf<PlacePoint>()

    private var selectedPlaceId: String? = null

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLocation = location
            processPlaceENUs()
        }
    }

    private val placeListener = object : PlaceListener {
        override fun onPlacesChanged(places: List<Place>) {
            this@ArOverlayView.places = places
            processPlaceENUs()
        }
    }

    private val rotationListener = object : RotationListener {
        override fun onRotationVectorChanged(rotationVector: FloatArray) {
            projectionMatrix = ProjectionMatrix.fromRotationVectorAndLayout(
                rotationVector, width.toFloat(), height.toFloat()
            )

            processPlacePoints()
        }
    }

    private fun processPlaceENUs() {
        currentLocation?.let { currentLocation ->
            val currentECEF = ECEFCoordinate.fromLocation(currentLocation)

            val newPlaceENUs = mutableListOf<PlaceENU>()
            places.forEach {
                newPlaceENUs.add(PlaceENU.fromCurrentECEFToPlace(currentECEF, it))
            }

            placeENUs = newPlaceENUs.toList()
            processPlacePoints()
        }
    }

    private fun processPlacePoints() {
        projectionMatrix?.let { projectionMatrix ->
            val newPlacePoints = mutableListOf<PlacePoint>()
            placeENUs.forEach {
                val placePoint = PlacePoint.fromProjectionOfPlaceEnu(projectionMatrix, it)
                newPlacePoints.add(placePoint)
            }

            placePoints = newPlacePoints
            invalidate()
        }
    }

    fun start() {
        LocationListener.register(locationListener)
        PlaceListener.register(placeListener)
        RotationListener.register(rotationListener)

        Place.fetchPlaces(activity)

        if (parent != null) {
            val viewGroup = parent as ViewGroup
            viewGroup.removeView(this)
        }
        frameLayout.addView(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                placePoints.forEach {
                    if (it.touching(event.x, event.y, it.place.id == selectedPlaceId)) {
                        selectedPlaceId = it.place.id
                    }
                }
            }
        }

        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        selectedPlaceId = null
        return super.performClick()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            placePoints.forEach { placePoint ->
                placePoint.draw(context, it, placePoint.place.id == selectedPlaceId)
            }
        }
    }
}