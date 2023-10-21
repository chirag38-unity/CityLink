package com.example.citylink

import android.app.Application
import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.citylink.ui.viewmodels.BluetoothServiceViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import io.socket.client.Socket
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BaseApplication(): Application(), DefaultLifecycleObserver {

    @Inject lateinit var socket : Socket
    @Inject lateinit var mAuth: FirebaseAuth

    companion object {
        var isAppInForeground = false
    }

    override fun onCreate() {
        super<Application>.onCreate()
        Timber.plant(Timber.DebugTree())
        socket.connect()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onTerminate() {
        socket.disconnect()
        super.onTerminate()
    }

    @OnLifecycleEvent(Event.ON_STOP)
    fun onBackground() {
        isAppInForeground = false
    }

    @OnLifecycleEvent(Event.ON_START)
    fun onForeground() {
        isAppInForeground = true
    }

}