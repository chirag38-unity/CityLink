package com.example.citylink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.citylink.R
import com.example.citylink.dataClasses.Trip
import com.example.citylink.others.TrackingUtility
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class TravelHistoryAdapter(options: FirestoreRecyclerOptions<Trip>) : FirestoreRecyclerAdapter<Trip, TravelHistoryAdapter.TravelHistoryViewHolder>(options) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TravelHistoryAdapter.TravelHistoryViewHolder {
        return TravelHistoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.trips_card, parent, false))
    }

    override fun onBindViewHolder(
        holder: TravelHistoryAdapter.TravelHistoryViewHolder,
        position: Int,
        model: Trip
    ) {
        holder.date.text = TrackingUtility.getFormattedTransactionTimeStamp(model.startTime!!)
        holder.fare.text = "-${model.fare}₹"
        holder.busID.text = "Bus No. ${model.busNo}"
        holder.fromLocation.text = model.fromLocation
        holder.toLocation.text = model.toLocation
    }
    class TravelHistoryViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        var date = itemView.findViewById<TextView>(R.id.date_card)
        var fare = itemView.findViewById<TextView>(R.id.fare_card)
        var toLocation = itemView.findViewById<TextView>(R.id.to_location_card)
        var fromLocation = itemView.findViewById<TextView>(R.id.from_location_card)
        var busID = itemView.findViewById<TextView>(R.id.busId_card)
    }
}