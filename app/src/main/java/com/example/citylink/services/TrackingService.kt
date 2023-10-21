package com.example.citylink.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.citylink.R
import com.example.citylink.adapters.RetrofitInterface
import com.example.citylink.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.citylink.others.Constants.ACTION_PAUSE_SERVICE
import com.example.citylink.others.Constants.ACTION_STOP_SERVICE
import com.example.citylink.others.Constants.FASTEST_LOCATION_INTERVAL
import com.example.citylink.others.Constants.LOCATION_UPDATE_INTERVAL
import com.example.citylink.others.Constants.NOTIFICATION_TRACKING_CHANNEL_ID
import com.example.citylink.others.Constants.NOTIFICATION_TRACKING_CHANNEL_NAME
import com.example.citylink.others.Constants.TRACKING_NOTIFICATION_ID
import com.example.citylink.others.TrackingUtility.getFormattedStopWatchTime
import com.example.citylink.others.TrackingUtility.getCurrentTimeStampString
import com.example.citylink.others.TrackingUtility.hasPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Poly_line = MutableList<LatLng>
typealias Poly_lines = MutableList<Poly_line>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    private var isFirstRun = true

    private var serviceKilled = false

    @Inject lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject lateinit var mAuth: FirebaseAuth
    @Inject lateinit var baseNotification : NotificationCompat.Builder
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var socket: Socket
    private lateinit var currentNotification : NotificationCompat.Builder
    private val gson = Gson()

    companion object {
        val isTracking = MutableLiveData<Boolean>(false)
        val hasLastLocation = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Poly_lines>()
        val currentLocation = MutableLiveData<Location>()
        val timeSpentInSeconds = MutableLiveData<Long>()
        val startingTimeString = MutableLiveData<String>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        hasLastLocation.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeSpentInSeconds.postValue(0L)
        startingTimeString.postValue("")

        currentLocation.postValue(Location("First Loc").apply {
            this.longitude = 0.0
            this.latitude = 0.0
        })
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        currentNotification = baseNotification
        isTracking.observe(this) {
            updateLocationTracking(it)
//            updateNotificationTracking(it)
        }
    }

    private fun startLocUpdatesToServer(busID: String) {
        val map = HashMap<String, Any>()
        map["ID"] = mAuth.currentUser!!.uid
        map["busID"] = busID

        lifecycleScope.launch(Dispatchers.IO) {
            socket.emit("serviceStart", gson.toJson(map).toString())
        }
        Timber.tag("Starting Location Updates").d(" -> $map")
    }

    private fun notifyCurrentLocToServer() {
        val map = HashMap<String, Any>()
        map["ID"] = mAuth.currentUser!!.uid
        map["longitude"] = currentLocation.value!!.longitude
        map["latitude"] = currentLocation.value!!.latitude

        lifecycleScope.launch(Dispatchers.IO) {
            socket.emit("locationUpdate", gson.toJson(map).toString())
        }
        Timber.tag("User Location Updates").d("Current Location -> $map")

    }

    private fun stopLocUpdatesToServer(busID: String) {
        val map = HashMap<String, Any>()
        map["busID"] = busID
        map["ID"] = mAuth.currentUser!!.uid
        map["longitude"] = currentLocation.value!!.longitude
        map["latitude"] = currentLocation.value!!.latitude

        lifecycleScope.launch(Dispatchers.IO) {
            socket.emit("serviceStop", gson.toJson(map).toString())
        }
        Timber.tag("User Location Updates").d("Location update service killed -> $map")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun) {
                        startForegroundService(it.getStringExtra("busID")!!)
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startForegroundService(it.getStringExtra("busID")!!)
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService(it.getStringExtra("busID")!!)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService(){
        isTracking.postValue(false)
    }

    private fun killService(busID : String){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopLocUpdatesToServer(busID!!)
        stopForeground(true)
        stopSelf()
    }

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        startingTimeString.postValue(getCurrentTimeStampString())

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                timeSpentInSeconds.postValue(timeSpentInSeconds.value!! + 1)
                delay(1000L)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(hasPermissions(this)) {
                val serviceLocationRequest = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    serviceLocationRequest,
                    serviceLocationCallback,
                    Looper.getMainLooper()
                )

            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(serviceLocationCallback)
        }
    }

    private val serviceLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for(location in locations) {
                        addPathPoint(location)
                    }
                    currentLocation.postValue(locations.last())
                }
                hasLastLocation.postValue(true)
                notifyCurrentLocToServer()
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService(busID : String) {
        createNotificationChannel(notificationManager)
        startForeground(TRACKING_NOTIFICATION_ID, baseNotification.build())
        startTimer()
        startLocUpdatesToServer(busID!!)
        timeSpentInSeconds.observe(this) {
            if (!serviceKilled) {
                val notification =
                    currentNotification.setContentText("Time spent travelling is ${getFormattedStopWatchTime(it*1000)}")
                notificationManager.notify(TRACKING_NOTIFICATION_ID, notification.build())
            }
        }
    }

    private fun updateNotificationTracking(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT or FLAG_MUTABLE)
        }
        currentNotification.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotification, ArrayList<NotificationCompat.Action>())
        }
        if (!serviceKilled) {
            currentNotification = baseNotification.addAction(
                R.drawable.baseline_pause_circle_24,
                notificationActionText,
                pendingIntent
            )
            notificationManager.notify(TRACKING_NOTIFICATION_ID, currentNotification.build())
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_TRACKING_CHANNEL_ID,
            NOTIFICATION_TRACKING_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}