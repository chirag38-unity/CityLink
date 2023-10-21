package com.example.citylink.adapters

import com.example.citylink.dataClasses.BluetoothResources
import com.example.citylink.dataClasses.BusBtEmit
import kotlinx.coroutines.flow.MutableSharedFlow

interface BusBtEmitReceiverManager {
    val data : MutableSharedFlow<BluetoothResources<BusBtEmit>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun closeConnection()
}