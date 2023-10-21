package com.example.citylink.adapters


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.citylink.R
import com.example.citylink.dataClasses.TransactionsBody
import com.example.citylink.others.TrackingUtility
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class TransacsAdapter (options: FirestoreRecyclerOptions<TransactionsBody>) :
    FirestoreRecyclerAdapter<TransactionsBody, TransacsAdapter.TransacsViewHolder>(options){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransacsViewHolder {
        return TransacsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.transactions_card, parent, false))
    }

    override fun onBindViewHolder(
        holder: TransacsViewHolder,
        position: Int,
        model: TransactionsBody
    ) {
        if(model.type == "credit"){
            holder.image.setImageResource(R.drawable.trans_card_wallet)
            holder.stats.text = "Credited ${TrackingUtility.getFormattedTransactionTimeStamp(model.date!!)}"
            holder.transAmount.text = "+${model.amount}₹"
            holder.transAmount.setTextColor(Color.GREEN)
        }else{
            holder.image.setImageResource(R.drawable.bus_boarding_ticket_icon)
            holder.stats.text = "Debited ${TrackingUtility.getFormattedTransactionTimeStamp(model.date!!)}"
            holder.transAmount.text = "-${model.amount}₹"
            holder.transAmount.setTextColor(Color.RED)
        }
        holder.currentAmount.text = "${model.curr_amount} ₹"
    }

    class TransacsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image = itemView.findViewById<ImageView>(R.id.transCardImage)
        var currentAmount = itemView.findViewById<TextView>(R.id.current_amount)
        var stats = itemView.findViewById<TextView>(R.id.stats)
        var transAmount = itemView.findViewById<TextView>(R.id.trans_amount)
    }

}