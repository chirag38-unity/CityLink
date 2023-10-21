package com.example.citylink.ui

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.citylink.R
import com.example.citylink.adapters.ConnectionState
import com.example.citylink.databinding.ActivityHomePageBinding
import com.example.citylink.databinding.NavHeaderBinding
import com.example.citylink.others.Constants
import com.example.citylink.others.Constants.ACTION_SHOW_ALERT_IN_ALERT_FRAGMENT
import com.example.citylink.others.Constants.ACTION_SHOW_ALERT_IN_TRACKING_FRAGMENT
import com.example.citylink.others.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.citylink.others.TrackingUtility
import com.example.citylink.services.FireBaseMessagingService
import com.example.citylink.services.NetworkConnectivityObserver
import com.example.citylink.services.TrackingService
import com.example.citylink.ui.fragments.Transactions
import com.example.citylink.ui.viewmodels.BluetoothServiceViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.ncorti.slidetoact.SlideToActView
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private lateinit var headerBinding: NavHeaderBinding
    private lateinit var navController: NavController

    //Bus Proximity Listeners-----------------------------------------------------------------------
    private val btServiceViewModel : BluetoothServiceViewModel by viewModels()
    @Inject lateinit var baseNotification : NotificationCompat.Builder
    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var bluetoothAdapter: BluetoothAdapter
    private var isBusProximityAlertAlreadyShown = MutableLiveData(false)
    private var isBusProximityDialogAlreadyShown = MutableLiveData(false)
    private var isDialogCurrentlyShowing = MutableLiveData(false)
    private val mainScope = MainScope()

    //FireBase & Google Auth Declarations-----------------------------------------------------------
    @Inject lateinit var mAuth: FirebaseAuth
    @Inject lateinit var mGoogleSignInClient : GoogleSignInClient

    //User dependencies-----------------------------------------------------------------------------
    @Inject lateinit var userDB: CollectionReference
    private lateinit var userRef: DocumentReference
    private var onlineWallet = MutableLiveData<Long>()
    private lateinit var tripsDB: CollectionReference
    private lateinit var transactionsDB : CollectionReference
    @Inject lateinit var geocoder: Geocoder

     //Network Observer & Bluetooth Adapter--------------
    @Inject lateinit var connectivityObserver : NetworkConnectivityObserver
    private lateinit var loadingDialog: LoadingDialog

    //Socket------------------
    @Inject lateinit var socket : Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        headerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0))
        setContentView(binding.root)
        Timber.plant(Timber.DebugTree())

        //Network Connectivity Observer-------------------------------------------------------------
        loadingDialog = LoadingDialog(this)

        //View Activities---------------------------------------------------------------------------
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.findNavController()
        setSupportActionBar(binding.navToolBar)

        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener{ _, destination, _ ->
            when(destination.id){
                R.id.homeFragment, R.id.busStopsNearMe, R.id.alerts, R.id.tripHistory, R.id.userSettings, R.id.transactions, R.id.trackingFragment -> {

                    if(destination.id == R.id.homeFragment || destination.id == R.id.busStopsNearMe || destination.id == R.id.alerts || destination.id == R.id.tripHistory || destination.id == R.id.userSettings || destination.id == R.id.transactions ){
                        binding.navView.visibility = View.VISIBLE
                        showActionBar()
                    }else{
                        hideActionBar()
                    }

                    if(mAuth.currentUser != null){
                        headerBinding.navEmail.text = mAuth.currentUser!!.email
                        headerBinding.navName.text = mAuth.currentUser!!.displayName
                        Glide.with(this).load(mAuth.currentUser!!.photoUrl).into(headerBinding.navImage)

                        userRef = userDB.document(mAuth.currentUser!!.uid)
                        tripsDB = userRef.collection("trips")
                        transactionsDB = userRef.collection("transactions")

                        userRef.addSnapshotListener{ snapshot, e ->
                            if (e != null) {
                                Timber.tag(ContentValues.TAG).w(e, "Listen failed.")
                                return@addSnapshotListener
                            }
                            if ((snapshot != null) && snapshot.exists()) {
                                onlineWallet.postValue(snapshot.get("wallet") as Long)
                            }
                            else {
                                logoutUser()
                                Toast.makeText(this,"User account must be deleted.",Toast.LENGTH_SHORT).show()
                            }
                        }

                        setupNetworkConnectivityListener()
                        if(TrackingUtility.hasAllPermissions(this)) {
                            if(bluetoothAdapter.isEnabled){
                                setUpBusProximityListener()
                            }
                        }
                        else{
                            Toast.makeText(this, "Some permissions are missing. Please allow them to use all features of the app", Toast.LENGTH_LONG).show()
                        }

                    }
                }
                else ->{
                    binding.navView.visibility = View.GONE
                    supportActionBar?.hide()
                    btServiceViewModel.connectionState.removeObservers(this)
                    hideActionBar()
                }
            }
        }

        handleNotificationClickEvent(intent)    //Handling Notification Click Events----------------

        //Firebase Messaging Services---------------------------------------------------------------
        FireBaseMessagingService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().subscribeToTopic("alerts")
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            FireBaseMessagingService.messagingToken = it
        }

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerlayout,
            binding.navToolBar,
            R.string.open_nav,
            R.string.close_nav
        )
        binding.drawerlayout.addDrawerListener(toggle)
        toggle.drawerArrowDrawable.color = resources.getColor(R.color.white)
        toggle.syncState()

        //Handle app launch during service----------------------------------------------------------
        if(TrackingService.isTracking.value!!){
            navController.navigate(R.id.action_global_trackingFragment)
        }

    }

    private fun logoutUser() {
        mGoogleSignInClient.signOut().addOnSuccessListener {
            mAuth.signOut()
            navController.navigate(R.id.action_global_googleLogin)
        }
    }


    //Bus Proximity Listener------------------------------------------------------------------------
    private fun setUpBusProximityListener() {

        btServiceViewModel.initializeConnection()
        createNotificationChannel(notificationManager)
        btServiceViewModel.connectionState.observe(this) {
            Timber.tag("ConnectionState").d(it.toString())
            if((it == ConnectionState.IntermConnect) && !isBusProximityAlertAlreadyShown.value!! && !TrackingService.isTracking.value!!) {
                Timber.tag("ConnectionState").d("Alert Shown")
                isBusProximityAlertAlreadyShown.postValue(true)
                resetBusAlertTimer()

                baseNotification.setContentTitle("Bus Nearby")
                baseNotification.setContentText("Please Get Ready")
                baseNotification.setGroup("CityLink Alerts")
                baseNotification.setContentIntent(null)
                baseNotification.setChannelId(Constants.NOTIFICATION_ALERT_CHANNEL_ID)
                val notificationID = Constants.FCM_NOTIFICATION_ID

                notificationManager.notify(notificationID, baseNotification.build())

            }
            if(it == ConnectionState.Connected && !isBusProximityDialogAlreadyShown.value!! && !isDialogCurrentlyShowing.value!! && !TrackingService.isTracking.value!! && btServiceViewModel.busId.isInitialized ){
                isBusProximityDialogAlreadyShown.value = true
                isDialogCurrentlyShowing.value = true
                resetBusDialogTimer()
                if(checkWalletvalue()){
                    showBusBoardDialogue(btServiceViewModel.busId.value)
                } else {
                    Toast.makeText(this, "You dont have enough money to commute. Please add more money", Toast.LENGTH_SHORT).show()
                }
            }
            if(it == ConnectionState.Connected && !isBusProximityDialogAlreadyShown.value!! && !isDialogCurrentlyShowing.value!! && TrackingService.isTracking.value!! && btServiceViewModel.busId.isInitialized) {
                isBusProximityDialogAlreadyShown.value = true
                isDialogCurrentlyShowing.value = true
                resetBusDialogTimer()
                showBusDeboardDialogue(btServiceViewModel.busId.value)
            }
        }

    }

    private fun checkWalletvalue(): Boolean {
        return onlineWallet.value!! > 100
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){

        val channel = NotificationChannel(
            Constants.NOTIFICATION_ALERT_CHANNEL_ID,
            Constants.NOTIFICATION_ALERT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "City Alerts"
            enableLights(true)
            lightColor = Color.GREEN
            setShowBadge(true)
        }
        notificationManager.createNotificationChannel(channel)

    }

    private fun resetBusDialogTimer() {
        mainScope.launch {
            delay(30000)
            isBusProximityDialogAlreadyShown.value = false
        }
    }

    private fun resetBusAlertTimer() {
        mainScope.launch {
            delay(300000)
            isBusProximityAlertAlreadyShown.value = false
        }
    }

    //Bluetooth Connectivity Observer---------------------------------------------------------------
    private fun setupBluetoothListener() {

//        btObserver = LifecycleEventObserver{_, event ->
//            if(event == Lifecycle.Event.ON_START){
//                if(TrackingUtility.hasBluetoothPermissions(applicationContext) && (bluetoothAdapter.isEnabled) && (bleConnectionState.value == ConnectionState.Disconnected)){
//                    btServiceViewModel.reconnect()
//                }
//            }
//            if(event == Lifecycle.Event.ON_STOP){
//                if(bleConnectionState.value == ConnectionState.Connected){
//                    btServiceViewModel.disconnect()
//                }
//            }
//        }
        
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //Network Connectivity Observer functions-------------------------------------------------------
    private fun setupNetworkConnectivityListener() {
        connectivityObserver.observe(this) {isConnected ->  //Connectivity Observing
            if(isConnected){
                hideLoadingDialog()
            }else{
                showLoadingDialog()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
        if(binding.drawerlayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerlayout.closeDrawer(GravityCompat.START)
        }
        else {
            finish()
        }
    }

    //Notification Click Events---------------------------------------------------------------------
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNotificationClickEvent(intent)
    }

    private fun handleNotificationClickEvent(intent: Intent?){

        when(intent?.action){
            ACTION_SHOW_TRACKING_FRAGMENT->{
                Timber.tag("Handling Intents").d(ACTION_SHOW_TRACKING_FRAGMENT)
                navController.navigate(R.id.action_global_trackingFragment)
            }
            ACTION_SHOW_ALERT_IN_TRACKING_FRAGMENT->{
                Timber.tag("Handling Intents").d(ACTION_SHOW_ALERT_IN_TRACKING_FRAGMENT)
                navController.navigate(R.id.action_global_trackingFragment)
            }
            ACTION_SHOW_ALERT_IN_ALERT_FRAGMENT->{
                Timber.tag("Handling Intents").d(ACTION_SHOW_ALERT_IN_ALERT_FRAGMENT)
                navController.navigate(R.id.action_global_alerts)
            }
        }
    }

    //Loading dialog functions----------------------------------------------------------------------
    private fun showLoadingDialog() {
        loadingDialog.setMessage("No internet connection. Please wait...")
        loadingDialog.show()
        socket.disconnect()
    }

    private fun hideLoadingDialog() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
            socket.connect()
        }
    }

    //ActionBar Functions---------------------------------------------------------------------------
    private fun hideActionBar() {
        val actionBar = (this as AppCompatActivity).supportActionBar
        actionBar?.setDisplayShowCustomEnabled(false)
        actionBar?.hide()
    }

    private fun showActionBar() {
        val actionBar = (this as AppCompatActivity).supportActionBar
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.show()
    }

    //Bus Dialogues---------------------------------------------------------------------------------
    private fun showBusBoardDialogue(value: String?) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.bus_board_dialog, null)

        val busDialogID = dialogView.findViewById<TextView>(R.id.boarding_dialog_id)
        val boardingSlider = dialogView.findViewById<SlideToActView>(R.id.bus_board)

        builder.setView(dialogView)
        val alertDialog = builder.create()

        busDialogID.text = "Bus - ${value}"
        boardingSlider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                boardingSlider.setCompleted(true, true)
                startTrackingService(value)
                alertDialog.dismiss()
            }
        }

        alertDialog.setOnDismissListener{
            isDialogCurrentlyShowing.value = false
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setGravity(Gravity.BOTTOM)
        alertDialog.show()
    }



    private fun showBusDeboardDialogue(value: String?) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.bus_deboard_dialog, null)

        val busDialogID = dialogView.findViewById<TextView>(R.id.deboarding_dialog_id)
        val boardingSlider = dialogView.findViewById<SlideToActView>(R.id.bus_deboard)

        builder.setView(dialogView)
        val alertDialog = builder.create()

        busDialogID.text = "Bus - ${value}"
        boardingSlider.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                boardingSlider.setCompleted(true, true)
                calculateFare(value)
//                stopTrackingService(value)
                alertDialog.dismiss()
            }
        }

        alertDialog.setOnDismissListener{
            isDialogCurrentlyShowing.value = false
        }

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setGravity(Gravity.BOTTOM)
        alertDialog.show()
    }

    private fun pauseTrackingService(value: String?) {
        sendCommandToService(Constants.ACTION_PAUSE_SERVICE, value!!)
    }

    private fun stopTrackingService(value: String?) {
        sendCommandToService(Constants.ACTION_STOP_SERVICE, value!!)
        navController.navigate(R.id.action_trackingFragment_to_homeFragment)
    }

    private fun startTrackingService(value: String?) {
//        btServiceViewModel.disconnect()
//        reconnetAfterSomeTime()
        sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE, value!!)
        navController.navigate(R.id.action_homeFragment_to_trackingFragment)
    }

    private fun reconnetAfterSomeTime() {
        mainScope.launch {
            delay(120000)
            btServiceViewModel.initializeConnection()
        }
    }

    fun sendCommandToService(action:String, busId:String)=
        Intent(this, TrackingService ::class.java).also{
            it.action=action
            it.putExtra("busID", busId)
            this.startService(it)
        }

    private fun calculateFare(value: String?) {
        pauseTrackingService(value)
        var distanceTravelledinMts = 0
        for (path in TrackingService.pathPoints.value!!){
            distanceTravelledinMts += TrackingUtility.calculatePolylineLength(path).toInt()
        }
        val fare = (distanceTravelledinMts/1000f*10 + 10).toLong()
        val fromLocation = getStringLocation(TrackingService.pathPoints.value!!.first().first())
        val toLocation = getStringLocation(TrackingService.pathPoints.value!!.last().last())
        val timeTravelled = TrackingUtility.getFormattedStopWatchTime(TrackingService.timeSpentInSeconds.value!!)

        lifecycleScope.launch(Dispatchers.Main) {

            val prev_amount = onlineWallet.value!!
            Firebase.firestore.runTransaction { transaction->
                val snapshot = transaction.get(userRef)
                val kmsTravelled = snapshot.get("kmsTravelled") as Double
                transaction.update(userRef,"wallet",  onlineWallet.value!! - fare)
                transaction.update(userRef, "kmsTravelled", kmsTravelled + distanceTravelledinMts/1000F )
            }.addOnSuccessListener {

                Firebase.firestore.runBatch {batch->
                    val map2 = hashMapOf(
                    "amount" to fare,
                    "date" to TrackingUtility.getCurrentTimeStampString(),
                    "prev_amount" to prev_amount,
                    "curr_amount" to prev_amount - fare,
                    "type" to "debit",
                    "millis" to System.currentTimeMillis())

                    val map1 =  hashMapOf(
                        "fare" to fare,
                        "fromLocation" to fromLocation,
                        "distanceTravelled" to distanceTravelledinMts,
                        "toLocation" to toLocation,
                        "busNo" to value,
                        "timeTravelled" to timeTravelled,
                        "millis" to System.currentTimeMillis(),
                        "startTime" to TrackingService.startingTimeString.value!!)

                    val tripRef = tripsDB.document()
                    val transRef = transactionsDB.document()

                    batch.set(tripRef, map1)
                    batch.set(transRef, map2)

                }.addOnSuccessListener {
                    showTripConclusionDialog(fare, fromLocation, toLocation, distanceTravelledinMts )
                    stopTrackingService(value)
                }.addOnFailureListener {
                    Timber.tag("TransactionFailed").d(it)
                    Toast.makeText(this@HomePage,"Unable to deboard, try connecting to the internet please.", Toast.LENGTH_SHORT).show()
                    sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE, btServiceViewModel.busId.value!!)
                }

            }.addOnFailureListener {
                Timber.tag("TransactionFailed").d(it)
                Toast.makeText(this@HomePage,"Unable to deboard, try connecting to the internet please.", Toast.LENGTH_SHORT).show()
                sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE, btServiceViewModel.busId.value!!)
            }

        }

    }

    private fun showTripConclusionDialog(
        fare: Long,
        fromLocation: String,
        toLocation: String,
        distanceTravelledinMts: Int
    ) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.ride_conclude_card, null)

        val fLocation = dialogView.findViewById<TextView>(R.id.from_location_card)
        val tLocation = dialogView.findViewById<TextView>(R.id.to_Location_Card)
        val fareCard = dialogView.findViewById<TextView>(R.id.fare_card)
        val distanceCard = dialogView.findViewById<TextView>(R.id.distance_card)


        builder.setView(dialogView)
        val alertDialog = builder.create()

        fLocation.text = "From ${fromLocation}"
        tLocation.text = "To ${toLocation}"
        fareCard.text = "Fare Charged : ${fare}₹"
        distanceCard.text = "Distance Travelled : ${distanceTravelledinMts/1000f} mts"

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setGravity(Gravity.CENTER)
        alertDialog.show()

    }

    private fun getStringLocation(location : LatLng) : String {
        val addresses: MutableList<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val address: Address? = addresses?.firstOrNull()
        var addressLine = address?.getAddressLine(0)
        val locality = addressLine?.substringAfterLast(',', addressLine.substringBeforeLast(','))
        addressLine = addressLine?.substringBefore(",")
        return "${addressLine}, ${locality}"
    }



}
