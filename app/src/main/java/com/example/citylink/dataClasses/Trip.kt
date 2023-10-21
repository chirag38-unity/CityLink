package com.example.citylink.dataClasses

import androidx.annotation.Keep

@Keep
data class Trip(
    var startTime : String? = null,
    var fare : Long? = null,
    var busNo : String? = null,
    var fromLocation : String? = null,
    var toLocation : String? = null){

}