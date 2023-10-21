package com.example.citylink.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import androidx.lifecycle.LiveData
import timber.log.Timber


class NetworkConnectivityObserver(
    private val context: Context
): LiveData<Boolean>() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

   private lateinit var networkConnectionCallback : ConnectivityManager.NetworkCallback

    override fun onActive() {
        super.onActive()
        updateNetworkConnection()
        connectivityManager.registerDefaultNetworkCallback(connectionCallback())
    }

    private fun updateNetworkConnection() {
        val networkConnection : NetworkInfo? = connectivityManager?.activeNetworkInfo
        postValue(networkConnection?.isConnected == true)
    }

    override fun onInactive() {
        super.onInactive()
        try {
            connectivityManager.unregisterNetworkCallback(connectionCallback())
        }catch (e : Exception){
            Timber.d("unregister failed")
        }
    }

    private val networkReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            updateNetworkConnection()
        }
    }

    private fun connectionCallback() : ConnectivityManager.NetworkCallback {
       networkConnectionCallback = object : ConnectivityManager.NetworkCallback() {

           override fun onAvailable(network: Network) {
               super.onAvailable(network)
               postValue(true)
           }

           override fun onLost(network: Network) {
               super.onLost(network)
               postValue(false)
           }

           override fun onUnavailable() {
               super.onUnavailable()
               postValue(false)
           }
       }
       return  networkConnectionCallback
   }
    fun removeNetworkObserver() {
        connectivityManager.unregisterNetworkCallback(connectionCallback())
    }
}