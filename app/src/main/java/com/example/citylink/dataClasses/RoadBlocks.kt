package com.example.citylink.dataClasses

import androidx.annotation.Keep

@Keep
data class RoadBlocks(
    val latitude : Double,
    val longitude : Double,
    val title : String,
    val reason : String)
