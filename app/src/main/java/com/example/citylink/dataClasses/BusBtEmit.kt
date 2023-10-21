package com.example.citylink.dataClasses

import com.example.citylink.adapters.ConnectionState
import androidx.annotation.Keep

@Keep
data class BusBtEmit(val busCode : String,
    val connectionState: ConnectionState)
