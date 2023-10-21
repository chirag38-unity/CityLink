package com.example.citylink.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import com.example.citylink.R
import com.example.citylink.adapters.BusBtEmitReceiveManager
import com.example.citylink.adapters.BusBtEmitReceiverManager
import com.example.citylink.adapters.RetrofitInterceptor
import com.example.citylink.adapters.RetrofitInterface
import com.example.citylink.others.Constants
import com.example.citylink.others.Constants.BASE_URL
import com.example.citylink.others.Constants.DEV_URL
import com.example.citylink.others.Constants.DEV_URL_2
import com.example.citylink.services.NetworkConnectivityObserver
import com.example.citylink.ui.HomePage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.Locale
import javax.inject.Singleton
import com.example.citylink.BuildConfig.serverApiKey

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @ActivityScoped
    @ActivityContext
    fun provideActivityContext(activity: AppCompatActivity): Context {
        return activity
    }

    //GoogleSignIN Instances------------------------------------------------------------------------
    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext applicationContext: Context) : GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(applicationContext.getString(R.string.default_web_client_id))
            .requestEmail().requestProfile().build()
        return GoogleSignIn.getClient(applicationContext, gso)
    }

    //Retrofit Instances----------------------------------------------------------------------------

    @Singleton
    @Provides
    fun provideOkHTTPInterceptor(retrofitInterceptor: RetrofitInterceptor) : OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(retrofitInterceptor).build()
    }

    @Singleton
    @Provides
    fun provideRetrofitBuilder() : Retrofit.Builder {
        val gson: Gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @Singleton
    @Provides
    fun provideRetrofitInterface(retrofitBuilder : Retrofit.Builder ,retrofitInterceptorClient : OkHttpClient) : RetrofitInterface {
        retrofitBuilder
            .client(retrofitInterceptorClient)
        return retrofitBuilder.build().create(RetrofitInterface::class.java)
    }

    //Socket.IO Instances---------------------------------------------------------------------------
    @Singleton
    @Provides
    fun provideSocketIOInstance(mAuth: FirebaseAuth) : Socket {
        val opts = IO.Options();
        opts.forceNew = true
        opts.reconnection = true
        opts.reconnectionDelay = 1000
        val userID = if(mAuth.currentUser != null) mAuth.currentUser!!.uid else "anonymous"
        opts.query = "apiKey=${serverApiKey}&userId=${userID}"
        return IO.socket(BASE_URL , opts)
    }

    //Firebase Auth & Firestore Instances-----------------------------------------------------------

    @Provides
    @Singleton
    fun provideFirestoreInstance() : FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseUserCollectionReference() : CollectionReference {
        return Firebase.firestore.collection("Users")
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth() : FirebaseAuth {
        return Firebase.auth
    }

    //FusedLocationClient & Geocoder -------------------------------------------------------------------------
    @Provides
    @Singleton
    fun provideFusedLocationClient(@ApplicationContext applicationContext: Context) : FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext applicationContext: Context) : Geocoder {
        return Geocoder(applicationContext, Locale.getDefault())
    }

    //Bluetooth Adapter-----------------------------------------------------------------------------
    @Provides
    @Singleton
    fun provideBluetoothAdapter(@ApplicationContext applicationContext: Context) : BluetoothAdapter {
        val manager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return manager.adapter
    }

    @Provides
    @Singleton
    fun provideBusBtEmitReceiverManager(
        @ApplicationContext applicationContext: Context,
        bluetoothAdapter: BluetoothAdapter) : BusBtEmitReceiverManager {
        return BusBtEmitReceiveManager(bluetoothAdapter, applicationContext)
    }

    //Network Observer------------------------------------------------------------------------------
    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(@ApplicationContext applicationContext: Context) : NetworkConnectivityObserver {
        return NetworkConnectivityObserver(applicationContext)
    }

    //Notifications Utility-------------------------------------------------------------------------
    @Provides
    @Singleton
    fun provideNotifManager(@ApplicationContext app: Context) : NotificationManager {
        return app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ) = PendingIntent.getActivity( app,0, Intent(app, HomePage::class.java).also {
        it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
    },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    @Provides
    fun provideBaseNotificationBuilder(@ApplicationContext app: Context, pendingIntent: PendingIntent ) =
        NotificationCompat.Builder(app, Constants.NOTIFICATION_TRACKING_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_bus_logo)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("You are using CityLink to Travel")
            .setLights(Color.argb(1, 34, 139, 34), 3000, 3000)
            .setGroup("CityLink Travel")
            .extend(
                NotificationCompat.WearableExtender()
                    .setContentIcon(R.mipmap.ic_bus_logo_foreground)
            )
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setContentIntent(pendingIntent)

    //Input Manager---------------------------------------------------------------------------------
    @Provides
    fun provideInputMethodManager(@ApplicationContext app: Context) : InputMethodManager {
        return app.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

}