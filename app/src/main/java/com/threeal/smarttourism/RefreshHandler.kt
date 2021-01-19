package com.threeal.smarttourism

import android.app.Activity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class RefreshHandler constructor(private val activity: Activity) {
    private val swipeRefresh: SwipeRefreshLayout = activity.findViewById(R.id.swipeRefresh)

    private val swipeRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        Place.fetchPlaces(activity)
        swipeRefresh.isRefreshing = true
    }

    private val placeListener = object : PlaceListener {
        override fun onPlacesChanged(places: List<Place>?) {
            swipeRefresh.isRefreshing = false
        }
    }

    fun start() {
        swipeRefresh.setOnRefreshListener(swipeRefreshListener)
        PlaceListener.register(placeListener)
    }
}