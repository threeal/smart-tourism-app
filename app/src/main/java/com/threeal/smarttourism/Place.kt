package com.threeal.smarttourism

import android.content.Context
import android.location.Location
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface PlaceListener {
    companion object {
        private val placeListeners = mutableListOf<PlaceListener>()

        fun register(placeListener: PlaceListener) {
            if (placeListeners.all { it != placeListener }) {
                placeListeners.add(placeListener)
            }
        }

        fun trigger(places: List<Place>?) {
            placeListeners.forEach {
                it.onPlacesChanged(places)
            }
        }
    }

    fun onPlacesChanged(places: List<Place>?)
}

class Place private constructor(
    val id: String,
    val name: String,
    val type: Type,
    val description: String,
    val timestamp: LocalDateTime?,
    val location: Location
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

    companion object {
        fun fetchPlaces(context: Context) {
            val queue = Volley.newRequestQueue(context)

            Toast.makeText(context, R.string.text_fetching_data, Toast.LENGTH_SHORT).show()
            val placesRequest =
                JsonArrayRequest(
                    Request.Method.GET, context.getString(R.string.server_address), null,
                    { jsonArray ->
                        Toast.makeText(
                            context,
                            R.string.text_fetching_data_success,
                            Toast.LENGTH_SHORT
                        ).show()

                        val places = mutableListOf<Place>()
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray[i] as JSONObject

                            places.add(fromJSON(jsonObject))
                        }

                        PlaceListener.trigger(places)
                    },
                    {
                        Toast.makeText(
                            context,
                            R.string.text_fetching_data_failed,
                            Toast.LENGTH_SHORT
                        ).show()

                        PlaceListener.trigger(null)
                    })

            queue.add(placesRequest)
        }

        private fun fromJSON(jsonObject: JSONObject): Place {
            return Place(jsonObject.getString("id"),
                jsonObject.getString("name"),
                when (jsonObject.getString("type")) {
                    "information" -> Type.Information
                    "gallery" -> Type.Gallery
                    "garden" -> Type.Garden
                    "parking_area" -> Type.ParkingArea
                    "restroom" -> Type.Restroom
                    "gift_shop" -> Type.GiftShop
                    "food_court" -> Type.FoodCourt
                    else -> Type.Unknown
                },
                jsonObject.getString("description"),
                try {
                    LocalDateTime.parse(
                        jsonObject.getString("timestamp"),
                        DateTimeFormatter.ISO_DATE_TIME
                    )
                } catch (e: Exception) {
                    null
                },
                Location("Place").apply {
                    longitude = jsonObject.getDouble("longitude");
                    latitude = jsonObject.getDouble("latitude");
                    altitude = 0.0;
                })
        }
    }
}