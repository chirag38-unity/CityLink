package com.example.citylink.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.citylink.adapters.TransacsAdapter
import com.example.citylink.dataClasses.TransactionsBody
import com.example.citylink.databinding.FragmentTransactionsBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Transactions : Fragment() {

    private var frag_transacs_binding : FragmentTransactionsBinding? = null
    @Inject lateinit var mAuth: FirebaseAuth
    @Inject lateinit var userDB: CollectionReference
    private lateinit var userRef: DocumentReference
    private lateinit var transacsDb : CollectionReference
    private var transacsAdapter : TransacsAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        frag_transacs_binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        userRef = userDB.document(mAuth.currentUser!!.uid)
        transacsDb = userRef.collection("transactions")

        setupTransacsRecyclerView()

        return frag_transacs_binding!!.root
    }

    private fun setupTransacsRecyclerView() {
        val query : Query = transacsDb.orderBy("millis", Query.Direction.DESCENDING)
        val firestoreRecyclerOptions : FirestoreRecyclerOptions<TransactionsBody> = FirestoreRecyclerOptions.Builder<TransactionsBody>()
            .setQuery(query, TransactionsBody::class.java)
            .build()
        transacsAdapter = TransacsAdapter(firestoreRecyclerOptions)
        frag_transacs_binding?.transactionViewer?.layoutManager = LinearLayoutManager(requireContext())
        frag_transacs_binding?.transactionViewer?.adapter = transacsAdapter
    }

    override fun onStart() {
        super.onStart()
        transacsAdapter?.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        transacsAdapter?.stopListening()
    }

}