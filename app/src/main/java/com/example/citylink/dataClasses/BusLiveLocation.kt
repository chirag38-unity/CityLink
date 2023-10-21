package com.example.citylink.dataClasses

import androidx.annotation.Keep

@Keep
data class BusLiveLocation(
    val id : String,
    val passengers : Number,
    val latitude : Double,
    val longitude : Double,
    val lastLocation : String
)
