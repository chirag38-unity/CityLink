package com.example.citylink.dataClasses

import androidx.annotation.Keep

@Keep
data class TransactionsBody(
    val curr_amount : Int? = null,
    val date : String? = null,
    val type : String? = null,
    val amount : Int? = null,
    val prev_amount : Int? = null
)
