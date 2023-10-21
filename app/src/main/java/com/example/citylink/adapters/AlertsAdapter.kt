package com.example.citylink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.citylink.R
import com.example.citylink.dataClasses.AlertNotificationBody
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AlertsAdapter(options: FirestoreRecyclerOptions<AlertNotificationBody>) :
    FirestoreRecyclerAdapter<AlertNotificationBody, AlertsAdapter.AlertViewHolder>(options) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        return AlertViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.alert_card, parent, false))
    }

    override fun onBindViewHolder(
        holder: AlertViewHolder,
        position: Int,
        model: AlertNotificationBody
    ) {
        holder.alertTitle.text = "at ${model.address}"
        holder.alertMessage.text = model.reason
        when(model.reason){
            "Vehicle Breakdown" -> {
                holder.alertReason.setImageResource(R.drawable.overheat)
            } "Heavy Traffic" -> {
                holder.alertReason.setImageResource(R.drawable.warning)
            } "Road Maintenance" -> {
                holder.alertReason.setImageResource(R.drawable.under_construction)
            } else -> {
                holder.alertReason.visibility = View.GONE
            }
        }
    }

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var alertTitle = itemView.findViewById<TextView>(R.id.alertCardTitle)
        var alertMessage = itemView.findViewById<TextView>(R.id.alertCardMessage)
        var alertReason = itemView.findViewById<ImageView>(R.id.alertCardImage)
    }

}