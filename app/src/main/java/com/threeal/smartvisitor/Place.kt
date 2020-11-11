package com.threeal.smartvisitor

import android.location.Location

class Place constructor(
    name: String,
    type: Type,
    description: String,
    latitude: Double,
    longitude: Double,
    altitude: Double
) {
    enum class Type {
        Information,
        Garden,
        Rides,
        ParkingArea,
        Restroom,
        GiftShop,
        FoodCourt
    }

    var name: String = name
    var type: Type = type
    var description: String = description

    var location: Location = Location("SmartVisitor").apply {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
    }
}