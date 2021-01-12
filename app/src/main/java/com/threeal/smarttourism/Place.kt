package com.threeal.smarttourism

import android.location.Location

class Place constructor(
    var name: String,
    var type: Type,
    var description: String,
    latitude: Double,
    longitude: Double,
    altitude: Double
) {
    enum class Type {
        Information,
        Gallery,
        Garden,
        Rides,
        ParkingArea,
        Restroom,
        GiftShop,
        FoodCourt,
        Unknown,
    }

    var location: Location = Location("ArPoint").apply {
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
    }
}