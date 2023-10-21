package com.example.citylink.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citylink.R
import com.example.citylink.adapters.TransacsAdapter
import com.example.citylink.adapters.TravelHistoryAdapter
import com.example.citylink.dataClasses.TransactionsBody
import com.example.citylink.dataClasses.Trip
import com.example.citylink.databinding.FragmentTripHistoryBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TripHistory : Fragment() {

    private var fragTripHisBinding : FragmentTripHistoryBinding? = null
    @Inject
    lateinit var mAuth: FirebaseAuth
    @Inject
    lateinit var userDB: CollectionReference
    private lateinit var userRef: DocumentReference
    private lateinit var tripsDb : CollectionReference
    private var tripsAdapter : TravelHistoryAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragTripHisBinding = FragmentTripHistoryBinding.inflate(inflater, container, false)

        userRef = userDB.document(mAuth.currentUser!!.uid)
        tripsDb = userRef.collection("trips")
        setupTripsRecyclerView()

        return fragTripHisBinding!!.root
    }

    private fun setupTripsRecyclerView() {
        val query : Query = tripsDb.orderBy("millis", Query.Direction.DESCENDING)
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<Trip> = FirestoreRecyclerOptions.Builder<Trip>()
            .setQuery(query, Trip::class.java)
            .build()
        tripsAdapter = TravelHistoryAdapter(firestoreRecyclerOptions)
        fragTripHisBinding?.tripsHistory?.layoutManager = LinearLayoutManager(requireContext())
        fragTripHisBinding?.tripsHistory?.adapter = tripsAdapter
    }

    override fun onStart() {
        super.onStart()
        tripsAdapter?.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        tripsAdapter?.stopListening()
    }

}