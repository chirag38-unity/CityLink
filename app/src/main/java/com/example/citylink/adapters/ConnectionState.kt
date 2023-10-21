package com.example.citylink.adapters

import androidx.annotation.Keep

@Keep sealed interface ConnectionState{
    object Connected: ConnectionState
    object Disconnected: ConnectionState
    object Uninitialized: ConnectionState
    object CurrentlyInitializing: ConnectionState
    object IntermConnect : ConnectionState
}