package com.threeal.smartvisitor

import android.location.Location

class ArPoint constructor(name: String, latitude: Double, longitude: Double, altitude: Double) {
    var name: String = name

    var location: Location = Location("ArPoint").apply {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
    }
}