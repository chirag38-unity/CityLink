package com.example.citylink.dataClasses

import androidx.annotation.Keep

@Keep
data class AlertNotificationBody(
    val address : String? = null,
    val reason : String? = null
)
