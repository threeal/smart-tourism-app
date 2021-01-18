package com.threeal.smarttourism

class PlaceENU private constructor(val place: Place, val enu: ENUCoordinate) {
    companion object {
        fun fromCurrentECEFToPlace(currentECEF: ECEFCoordinate, place: Place): PlaceENU {
            val placeECEF = ECEFCoordinate.fromLocation(place.location)
            val placeENU = ENUCoordinate.fromCurrentToTargetECEF(currentECEF, placeECEF)

            return PlaceENU(place, placeENU)
        }
    }
}