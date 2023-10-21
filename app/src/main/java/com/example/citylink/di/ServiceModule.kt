package com.example.citylink.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.citylink.R
import com.example.citylink.others.Constants
import com.example.citylink.ui.HomePage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

//    @ServiceScoped
//    @Provides
//    fun provideMainActivityPendingIntent(
//        @ApplicationContext app: Context
//    ) = PendingIntent.getActivity( app,0, Intent(app, HomePage::class.java).also {
//            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
//        },
//        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
//    )
//
//    @ServiceScoped
//    @Provides
//    fun provideBaseNotificationBuilder(@ApplicationContext app: Context, pendingIntent: PendingIntent ) =
//        NotificationCompat.Builder(app, Constants.NOTIFICATION_TRACKING_CHANNEL_ID)
//        .setSmallIcon(R.mipmap.ic_bus_logo)
//        .setAutoCancel(false)
//        .setOngoing(true)
//        .setContentTitle("You are using CityLink to Travel")
//        .setLights(Color.argb(1, 34, 139, 34), 3000, 3000)
//        .setGroup("CityLink Travel")
//        .extend(
//            NotificationCompat.WearableExtender()
//                .setContentIcon(R.mipmap.ic_bus_logo_foreground)
//        )
//        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
//        .setContentIntent(pendingIntent)

    //Notification Manager--------------------------------------------------------------------------
//    @Provides
//    @ServiceScoped
//    fun provideNotifManager(@ApplicationContext app: Context) : NotificationManager {
//        return app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    }

}