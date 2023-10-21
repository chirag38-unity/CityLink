package com.example.citylink.dataClasses

import androidx.annotation.Keep

@Keep
data class BusStops(
    val latitude : Double,
    val longitude : Double,
    val title : String
)
