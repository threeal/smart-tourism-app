package com.threeal.smarttourism

import android.app.Activity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class RefreshHandler constructor(private val activity: Activity) {
    private val swipeRefresh: SwipeRefreshLayout = activity.findViewById(R.id.swipeRefresh)

    private val swipeRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        val tagId = activity.intent.getStringExtra("com.threeal.smarttourism.TAG_ID")

        tagId?.let {
            Place.fetchPlaces(activity, it)
            swipeRefresh.isRefreshing = true
        }
    }

    private val placeListener = PlaceListener { swipeRefresh.isRefreshing = false }

    fun start() {
        swipeRefresh.setOnRefreshListener(swipeRefreshListener)
        PlaceListener.register(placeListener)
    }

    fun stop() {
        PlaceListener.unregister(placeListener)
    }
}