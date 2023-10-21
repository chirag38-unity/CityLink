package com.example.citylink.others

import android.Manifest
import android.graphics.Color
import android.os.Build

object Constants {

    const val BASE_URL="https://city-link.cyclic.app"
    const val DEV_URL="http://192.168.1.110:3000";
    const val DEV_URL_2 = "http://192.168.1.211:3000"

    const val ALL_PERMISSIONS_CODE = 110
    const val PERMISSION_NOTIFICATION_REQUEST_CODE = 112
    const val PERMISSION_LOCATION_REQUEST_CODE = 111

    val BLUETOOTHPERMISSIONS = if(Build.VERSION.SDK_INT > 30){
        arrayOf(Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT)
    }else{
        arrayOf(Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN)
    }

    val PERMISSIONS = if(Build.VERSION.SDK_INT > 32){
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT)
    }else if(Build.VERSION.SDK_INT > 30){
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    } else{
        arrayOf(Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val ALL_PERMISSIONS = if(Build.VERSION.SDK_INT > 32){
        arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT)
    }else if(Build.VERSION.SDK_INT > 30){
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    } else{
        arrayOf(Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    val LOCATION_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    val BACKGROUND_LOC_PERMISSION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION)


    const val LOCATION_UPDATE_INTERVAL = 4000L
    const val FASTEST_LOCATION_INTERVAL = 2000L
    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val ACTION_SHOW_ALERT_IN_TRACKING_FRAGMENT = "ACTION_SHOW_ALERT_IN_TRACKING_FRAGMENT"
    const val ACTION_SHOW_ALERT_IN_ALERT_FRAGMENT = "ACTION_SHOW_ALERT_IN_ALERT_FRAGMENT"

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

    const val NOTIFICATION_TRACKING_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_ALERT_CHANNEL_ID = "citylink_alerts"
    const val NOTIFICATION_ALERT_CHANNEL_NAME = "CityLink-alerts"
    const val NOTIFICATION_TRACKING_CHANNEL_NAME = "CityLink-travel"
    const val FCM_NOTIFICATION_ID = 1
    const val TRACKING_NOTIFICATION_ID = 2

}