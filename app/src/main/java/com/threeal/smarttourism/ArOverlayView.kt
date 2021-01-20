package com.threeal.smarttourism

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.graphics.Canvas
import android.location.Location
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import java.time.format.DateTimeFormatter

class ArOverlayView constructor(private val activity: Activity) :
    View(activity) {

    private val arFrameLayout: FrameLayout = activity.findViewById(R.id.arFrameLayout)
    private val locationView: CardView = activity.findViewById(R.id.locationView)

    private val locationTitle: TextView = activity.findViewById(R.id.locationTitle)
    private val locationInfo: TextView = activity.findViewById(R.id.locationInfo)
    private val locationDescription: TextView = activity.findViewById(R.id.locationDescription)

    private var projectionMatrix: ProjectionMatrix? = null
    private var currentECEF: ECEFCoordinate? = null

    private var places = listOf<Place>()
    private var placeENUs = listOf<PlaceENU>()
    private var placePoints = listOf<PlacePoint>()

    private var selectedPlaceId: String? = null

    private val locationListener = LocationListener { location ->
        currentECEF = ECEFCoordinate.fromLocation(location)
        processPlaceENUs()
    }

    private val placeListener = PlaceListener { places ->
        places?.let { safePlaces ->
            this.places = safePlaces
            processPlaceENUs()
        }
    }

    private val rotationListener = RotationListener { rotationVector ->
        projectionMatrix = ProjectionMatrix.fromRotationVectorAndLayout(
            rotationVector, width.toFloat(), height.toFloat()
        )

        processPlacePoints()
    }

    private val onTouchListener = OnTouchListener { _, event ->
        event?.let { safeEvent ->
            when (safeEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    performClick()

                    var placeId: String? = null
                    placePoints.forEach {
                        val selected = it.place.id == selectedPlaceId
                        if (it.touching(safeEvent.x, safeEvent.y, selected)) {
                            placeId = it.place.id
                        }
                    }

                    selectPlace(placeId)
                }
            }
        }

        false
    }

    private fun processPlaceENUs() {
        currentECEF?.let { safeCurrentEcef ->
            val newPlaceENUs = mutableListOf<PlaceENU>()
            places.forEach {
                newPlaceENUs.add(PlaceENU.fromCurrentECEFToPlace(safeCurrentEcef, it))
            }

            placeENUs = newPlaceENUs.toList()
            processPlacePoints()
        }
    }

    private fun processPlacePoints() {
        projectionMatrix?.let { safeProjectionMatrix ->
            val newPlacePoints = mutableListOf<PlacePoint>()
            placeENUs.forEach {
                val placePoint = PlacePoint.fromProjectionOfPlaceEnu(safeProjectionMatrix, it)
                newPlacePoints.add(placePoint)
            }

            placePoints = newPlacePoints
            invalidate()
        }
    }

    private fun selectPlace(placeId: String?) {
        selectedPlaceId = if (placeId != selectedPlaceId) placeId else null

        if (selectedPlaceId == null) {
            if (locationView.visibility != View.GONE) {
                locationView.animate()
                    .alpha(0f)
                    .setDuration(100)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            locationView.visibility = View.GONE
                        }
                    })
            }

            return
        }

        selectedPlaceId?.let { safeSelectedPlaceId ->
            val placePoint = placePoints.find { it.place.id == safeSelectedPlaceId }

            placePoint?.let { safePlacePoint ->
                locationTitle.text = safePlacePoint.place.name

                var distanceText = activity.getString(R.string.distance_info_not_found)
                currentECEF?.let {
                    val distance = it.distanceTo(safePlacePoint.enu.ecef)
                    distanceText = if (distance < 1000) {
                        activity.getString(R.string.distance_info_m).format(distance.toInt())
                    } else {
                        activity.getString(R.string.distance_info_km)
                            .format(distance.toInt() / 1000)
                    }
                }

                var visitedText = activity.getString(R.string.visitation_info_not_visited)
                safePlacePoint.place.timestamp?.let {
                    visitedText = activity.getString(R.string.visitation_info)
                        .format(it.format(DateTimeFormatter.ISO_LOCAL_TIME))
                }

                locationInfo.text = activity.getString(R.string.location_info)
                    .format(distanceText, visitedText)

                locationDescription.text = safePlacePoint.place.description

                locationView.apply {
                    if (visibility != View.VISIBLE) {
                        visibility = View.VISIBLE
                        alpha = 0f

                        animate().alpha(1f).setDuration(100).setListener(null)
                    }
                }
            }
        }
    }

    fun start() {
        LocationListener.register(locationListener)
        PlaceListener.register(placeListener)
        RotationListener.register(rotationListener)

        val tagId = activity.intent.getStringExtra(activity.getString(R.string.intent_tag_id))

        tagId?.let {
            Place.fetchPlaces(activity, it)
        }

        if (parent != null) {
            val viewGroup = parent as ViewGroup
            viewGroup.removeView(this)
        }
        arFrameLayout.addView(this)

        setOnTouchListener(onTouchListener)
    }

    fun stop() {
        LocationListener.unregister(locationListener)
        PlaceListener.unregister(placeListener)
        RotationListener.unregister(rotationListener)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let { safeCanvas ->
            placePoints.forEach {
                it.draw(context, safeCanvas, it.place.id == selectedPlaceId)
            }
        }
    }
}