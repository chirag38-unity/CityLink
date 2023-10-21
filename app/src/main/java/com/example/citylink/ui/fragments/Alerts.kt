package com.example.citylink.ui.fragments

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citylink.R
import com.example.citylink.adapters.RetrofitInterface
import com.example.citylink.adapters.AlertsAdapter
import com.example.citylink.dataClasses.AlertNotificationBody
import com.example.citylink.dataClasses.PingResult
import com.example.citylink.databinding.FragmentAlertsBinding
import com.example.citylink.others.TrackingUtility
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class Alerts : Fragment() {

    private var alertsPageBinding : FragmentAlertsBinding? = null
    private val alertsDb : CollectionReference = FirebaseFirestore.getInstance().collection("Alerts")
    private var alertsAdapter : AlertsAdapter? = null
    private val alertOptions = arrayOf("Vehicle Breakdown", "Heavy Traffic", "Road Maintenance")
    private var itemSelected : Int = -1
    private var alertOptionSelected : String? = null
    @Inject lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    @Inject lateinit var geocoder: Geocoder
    @Inject lateinit var retrofitInterface: RetrofitInterface

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        alertsPageBinding = FragmentAlertsBinding.inflate(inflater, container, false)

        setupAlertsRecycler()

        val alertDialog = AlertDialog.Builder(requireContext()) //Alert Dialogue to create new alerts
            .setTitle("Select the reason for creating an alert:-")
            .setIcon(R.drawable.alert_icon)
            .setCancelable(true)
            .setItems(alertOptions){ dialogInterface, i ->
                alertOptionSelected = alertOptions[i]
                if(alertOptionSelected != null){
                    createAlert()
                    dialogInterface.dismiss()
                }else{
                    Toast.makeText(requireContext(),"Please select a reason to place an alert", Toast.LENGTH_SHORT).show()
                    alertsPageBinding!!.addAlert.visibility = View.VISIBLE
                    dialogInterface.dismiss()
                }
            }
            .setNegativeButton("Cancel"){ dialogInterface, _ ->
                alertsPageBinding!!.addAlert.visibility = View.VISIBLE
                dialogInterface.cancel()
            }.create()

        if(!TrackingUtility.hasPermissions(requireContext())){
            alertsPageBinding!!.addAlert.visibility = View.GONE
        }

        alertsPageBinding!!.addAlert.setOnClickListener{
            alertsPageBinding!!.addAlert.visibility = View.GONE
            alertDialog.show()
        }

        return alertsPageBinding!!.root
    }

    @SuppressLint("MissingPermission")
    private fun createAlert() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val addresses: MutableList<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address: Address? = addresses?.firstOrNull()

                val map = HashMap<String, Any>()
                map["latitude"] = location.latitude
                map["longitude"] = location.longitude
                map["reason"] = alertOptionSelected!!


                if (address != null) {
                    var addressLine = address.getAddressLine(0)
                    Timber.tag("Address").d("AddressComplete %s", address)
                    Timber.tag("Address").d("AddressLine %s", addressLine)

                    // Split the input string by commas
                    val parts = addressLine.split(",")

                    val locality = parts[4] + "," + parts[5]

                    Timber.tag("Address").d("${parts[0]}, ${locality}")
                    map["address"] = "${parts[0]}, ${locality}"
                }

                retrofitInterface.sendAlert(map).enqueue(object : Callback<PingResult>{
                    override fun onResponse(
                        call: Call<PingResult>,
                        response: Response<PingResult>
                    ) {
                        if (response.code() == 200) {
                            Toast.makeText(activity, "Alert created successfully.", Toast.LENGTH_SHORT).show()
                            alertsPageBinding!!.addAlert.visibility = View.VISIBLE
                        }
                        else{
                            Toast.makeText(activity, "Error creating alert" + response.code().toString(), Toast.LENGTH_SHORT).show()
                            alertsPageBinding!!.addAlert.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<PingResult>, t: Throwable) {
                        Timber.d(t)
                        Toast.makeText(activity, "Error creating alert: $t", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

    }

    private fun setupAlertsRecycler() {
        val query : Query = alertsDb.orderBy("timeinmillis", Query.Direction.DESCENDING)
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<AlertNotificationBody> = FirestoreRecyclerOptions.Builder<AlertNotificationBody>()
            .setQuery(query, AlertNotificationBody::class.java)
            .build()
        alertsAdapter = AlertsAdapter(firestoreRecyclerOptions)
        alertsPageBinding?.alertViewer?.layoutManager = LinearLayoutManager(requireContext())
        alertsPageBinding?.alertViewer?.adapter = alertsAdapter
    }

    override fun onStart() {
        super.onStart()
        alertsAdapter?.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        alertsAdapter?.stopListening()
    }
}