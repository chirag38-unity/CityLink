package com.example.citylink.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.example.citylink.R
import com.example.citylink.adapters.RetrofitInterface
import com.example.citylink.databinding.FragmentHomeBinding
import com.example.citylink.others.Constants.PERMISSIONS
import com.example.citylink.others.TrackingUtility
import com.example.citylink.others.TrackingUtility.hasAllPermissions
import com.example.citylink.others.TrackingUtility.hasBluetoothPermissions
import com.example.citylink.others.TrackingUtility.warningPermissionDialog
import com.example.citylink.repositories.MapServiceRepo
import com.example.citylink.ui.viewmodels.MapsServiceViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private var fragHome_binding : FragmentHomeBinding? = null
    private val mapsServiceViewModel : MapsServiceViewModel by viewModels()
    @Inject lateinit var inputMethodManager: InputMethodManager

    //FireBase Declarations-------------------------------------------------------------------------
    @Inject lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser
    @Inject lateinit var userDB: CollectionReference
    private lateinit var transactionsDB : CollectionReference
    private lateinit var userRef: DocumentReference
    @Inject lateinit var mGoogleSignInClient : GoogleSignInClient

    //Retrofit Declarations-------------------------------------------------------------------------
    @Inject lateinit var retrofitInterface: RetrofitInterface
    //Bluetooth Adapter-----------------------------------------------------------------------------
    @Inject lateinit var bluetoothAdapter: BluetoothAdapter
    @Inject lateinit var mapServiceRepo : MapServiceRepo
    private var isBluetoothDialogAlreadyShown = false
//    private val bleConnectionState = btServiceViewModel.connectionState

    //User Resources--------------------------------------------------------------------------------
    private var onlineWallet = MutableLiveData<Long>()

    //BusLiveLocation Maps--------------------------------------------------------------------------
    private var busIcon : BitmapDescriptor? = null
    private var bus1Marker :  Marker? = null
    private var bus2Marker :  Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bus1MapFragment = childFragmentManager.findFragmentById(R.id.bus1Map) as SupportMapFragment?
        bus1MapFragment?.getMapAsync(bus1MapReadyCallback)

        val bus2MapFragment = childFragmentManager.findFragmentById(R.id.bus2Map) as SupportMapFragment?
        bus2MapFragment?.getMapAsync(bus2MapReadyCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment-----------------------------------------------------
        fragHome_binding = FragmentHomeBinding.inflate(inflater, container, false)

        //Firebase Initialisations------------------------------------------------------------------
        mUser = mAuth.currentUser!!
        userRef = userDB.document(mUser.uid)
        transactionsDB = userRef.collection("transactions")

        //Fragment Functionalities------------------------------------------------------------------

        fragHome_binding!!.logout.setOnClickListener{
            logoutUser()
        }

        fragHome_binding!!.addWallet.setOnClickListener{
            showAddToWalletDialog()
        }

        fragHome_binding!!.viewTransactions.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_transactions)
        }

        if (!hasAllPermissions(requireContext())) {
            Toast.makeText(requireContext(), "There are some permissions missing. App may not work as intended.", Toast.LENGTH_SHORT).show()
            requestPermissionLauncher.launch(PERMISSIONS)
        } else {
            fragHome_binding!!.permissionsButton.visibility = View.GONE
        }

        fragHome_binding!!.permissionsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        showBluetoothDialog()

        return fragHome_binding!!.root
    }

    private fun logoutUser() {
        mGoogleSignInClient.signOut().addOnSuccessListener {
            mAuth.signOut()
            findNavController().navigate(R.id.action_homeFragment_to_googleLogin)
        }
    }

    private fun showAddToWalletDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.add_to_wallet, null)

        val amountToAdd = dialogView.findViewById<EditText>(R.id.amount_to_add)
        val submit = dialogView.findViewById<Button>(R.id.wallet_submit)

        val paymentWindow = dialogView.findViewById<FrameLayout>(R.id.payment_window)
        val processWindow = dialogView.findViewById<FrameLayout>(R.id.processing_payment)
        val resultWindowSuccess = dialogView.findViewById<FrameLayout>(R.id.payment_result_success)
        val resultWindowFail = dialogView.findViewById<FrameLayout>(R.id.payment_result_fail)

        builder.setView(dialogView)
        val alertDialog = builder.create()

        submit.setOnClickListener {
            inputMethodManager.hideSoftInputFromWindow(amountToAdd.windowToken, 0)
            paymentWindow.visibility = View.GONE
            processWindow.visibility = View.VISIBLE
            if(TextUtils.isEmpty(amountToAdd.text.toString().trim())){
                Toast.makeText(requireContext(), "Please type a value to add.", Toast.LENGTH_SHORT).show()
                amountToAdd.text.clear()
                paymentWindow.visibility = View.VISIBLE
                processWindow.visibility = View.GONE
            }
            else{
                var amount = 0L
                try {
                    amount = amountToAdd.text.toString().toLong()
                }catch (e : Error){
                    Toast.makeText(requireContext(), "Please add a legit value.", Toast.LENGTH_SHORT).show()
                    amountToAdd.text.clear()
                    paymentWindow.visibility = View.VISIBLE
                    processWindow.visibility = View.GONE
                   return@setOnClickListener
                }
                val prev_amount = onlineWallet.value!!
                Firebase.firestore.runTransaction{ transaction->
                    val snapshot = transaction.get(userRef)
                    val totalAmount = snapshot.get("totalAmount") as Long
                    transaction.update(userRef, "wallet", amount + onlineWallet.value!!)
                    transaction.update(userRef, "totalAmount", totalAmount + amount)
                }.addOnSuccessListener {
                    Toast.makeText(requireContext(), "${amount} added to your account.", Toast.LENGTH_SHORT).show()
                    val data = hashMapOf(
                        "amount" to amount,
                        "date" to TrackingUtility.getCurrentTimeStampString(),
                        "prev_amount" to prev_amount,
                        "curr_amount" to amount + prev_amount,
                        "type" to "credit",
                        "millis" to System.currentTimeMillis()
                    )
                    transactionsDB.add(data).addOnSuccessListener {
                        processWindow.visibility = View.GONE
                        resultWindowSuccess.visibility = View.VISIBLE
                    }.addOnFailureListener {
                        processWindow.visibility = View.GONE
                        resultWindowFail.visibility = View.VISIBLE
                    }
                }.addOnFailureListener {
                    amountToAdd.text.clear()
                    processWindow.visibility = View.GONE
                    resultWindowFail.visibility = View.VISIBLE

                }
//                alertDialog.dismiss()
            }
        }
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    //Fragment Lifecycle Functions------------------------------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        fragHome_binding = null
        mapsServiceViewModel.removeBusLiveLocObserver()
        Timber.tag("HomeFrag").d("LeftHomeFrag")
    }



    override fun onStart() {
        super.onStart()

        mapsServiceViewModel.observeBusLiveLoc()
        mapsServiceViewModel.bus1LocationLiveData.observe(this){
            fragHome_binding?.bus1Passengers?.text = " : ${it.passengers}"

        }
        mapsServiceViewModel.bus2LocationLiveData.observe(this){
            fragHome_binding?.bus2Passengers?.text = " : ${it.passengers}"
        }

        userRef.addSnapshotListener{ snapshot, e ->
            if (e != null) {
                Timber.tag(ContentValues.TAG).w(e, "Listen failed.")
                return@addSnapshotListener
            }
            if ((snapshot != null) && snapshot.exists()) {
                onlineWallet.postValue(snapshot.get("wallet") as Long)
                fragHome_binding?.userWallet?.text = "Balance: ${snapshot.get("wallet")}₹"
            }

        }

    }


    //Bluetooth Adapter functions-------------------------------------------------------------------
    private fun showBluetoothDialog() {
        if(hasBluetoothPermissions(requireContext())){
            if(!bluetoothAdapter.isEnabled){
                if(!isBluetoothDialogAlreadyShown){
                    isBluetoothDialogAlreadyShown = true
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startBluetoothIntentForResult.launch(enableBluetoothIntent)
                }
            }
        }
    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            isBluetoothDialogAlreadyShown = false
            if(result.resultCode != Activity.RESULT_OK){
                Toast.makeText(requireContext(), "Bluetooth is required to use core functionality.", Toast.LENGTH_SHORT).show()
            }
        }


    //Request Permissions Launcher-------------------------------------------------------------------
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val isGranted = permissions.entries.all {it.value}
        if (!isGranted) {
            warningPermissionDialog(requireContext())
        }
    }

    //MapReadyCallBacks-----------------------------------------------------------------------------
    private val bus1MapReadyCallback = OnMapReadyCallback { map ->
//        bus1Map = map

        val scaler = BitmapFactory.decodeResource(resources,R.drawable.bus_placeholder)
        val scaledBitmap = Bitmap.createScaledBitmap(scaler, 70, 70, false)
        busIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)
        mapsServiceViewModel.bus1LocationLiveData.observe(this){
            bus1Marker?.remove()
            bus1Marker = map.addMarker(MarkerOptions()
                .icon(busIcon)
                .position(LatLng(it.latitude, it.longitude))
                .title(it.lastLocation)
            )
            bus1Marker!!.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 12F))
        }
        map.uiSettings.setAllGesturesEnabled(false)
        map.setOnMarkerClickListener {
            true
        }
    }

    private val bus2MapReadyCallback = OnMapReadyCallback { map ->
//        bus2Map = map

        map.uiSettings.setAllGesturesEnabled(false)
        mapsServiceViewModel.bus2LocationLiveData.observe(this){
            bus2Marker?.remove()
            bus2Marker = map.addMarker(MarkerOptions()
                .icon(busIcon)
                .position(LatLng(it.latitude, it.longitude))
                .title(it.lastLocation)
            )
            bus2Marker!!.showInfoWindow()
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 12F))
        }
        map.uiSettings.setAllGesturesEnabled(false)
        map.setOnMarkerClickListener{
            true
        }
    }

}