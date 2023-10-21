package com.example.citylink.services

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.citylink.R
import com.example.citylink.others.Constants
import com.example.citylink.others.Constants.FCM_NOTIFICATION_ID
import com.example.citylink.others.Constants.NOTIFICATION_ALERT_CHANNEL_ID
import com.example.citylink.others.Constants.NOTIFICATION_ALERT_CHANNEL_NAME

import com.example.citylink.ui.HomePage
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FireBaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var notificationManager : NotificationManager

    companion object {
        var sharedPref : SharedPreferences? = null
        var messagingToken : String?
            get(){
                return sharedPref?.getString("messagingToken", "")
            }set(value){
                sharedPref?.edit()?.putString("messagingToken", value)?.apply()
            }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        messagingToken = token
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

//            val notificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = FCM_NOTIFICATION_ID

            val messageData = Bundle()
            for ((key, value) in message.data) {
                messageData.putString(key, value)
            }

            createNotificationChannel(notificationManager)

            val notification = NotificationCompat.Builder(this, NOTIFICATION_ALERT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_bus_logo_foreground)
                .setContentTitle(message.notification?.title)
                .setContentText("at " + message.notification?.body)
                .setColor(ContextCompat.getColor(applicationContext, R.color.accent_green))
                .setLights(Color.argb(1, 34, 139, 34), 3000, 3000)
                .setAutoCancel(false)
                .setGroup("CityLink Alerts")
                .extend(
                    NotificationCompat.WearableExtender()
                        .setContentIcon(R.mipmap.ic_bus_logo_foreground)
                )
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setContentIntent(getMainActivityPendingIntent(messageData))
                .build()

            message.data["title"]?.let { Timber.tag(it) }
            notificationManager.notify(notificationID, notification)

    }

    private fun getMainActivityPendingIntent(data: Bundle): PendingIntent {
        val intent = Intent(this, HomePage::class.java)
        if(isServiceRunning(this, TrackingService::class.java)){
            intent.also{
                it.putExtra("alert_data",data)
                it.action = Constants.ACTION_SHOW_ALERT_IN_TRACKING_FRAGMENT
            }
        }else{
            intent.also{
                it.action = Constants.ACTION_SHOW_ALERT_IN_ALERT_FRAGMENT
            }
        }
        return PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){

        val channel = NotificationChannel(NOTIFICATION_ALERT_CHANNEL_ID,
            NOTIFICATION_ALERT_CHANNEL_NAME, IMPORTANCE_HIGH).apply {
            description = "City Alerts"
            enableLights(true)
            lightColor = Color.GREEN
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)

    }

    // Function to check if a service is running
    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // Get a list of running services
        val runningServices = activityManager.getRunningServices(Int.MAX_VALUE)
        // Check if the service class is in the list of running services
        for (serviceInfo in runningServices) {
            if (serviceClass.name == serviceInfo.service.className) {
                return true
            }
        }
        return false
    }

}