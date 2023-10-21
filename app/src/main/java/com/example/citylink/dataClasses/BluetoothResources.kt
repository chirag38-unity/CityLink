package com.example.citylink.dataClasses

import androidx.annotation.Keep

@Keep
sealed class BluetoothResources<out T:Any>{
    data class Success<out T:Any> (val data:T):BluetoothResources<T>()
    data class IntermConnect <out T : Any> (val message: String?): BluetoothResources<T>()
    data class Error(val errorMessage:String):BluetoothResources<Nothing>()
    data class Loading<out T:Any>(val data:T? = null, val message:String? = null):BluetoothResources<T>()
}
