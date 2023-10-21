package com.example.citylink.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.citylink.R
import com.example.citylink.databinding.FragmentUserSettingsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class UserSettings : Fragment(R.layout.fragment_user_settings) {

    private var fragmentUserSettingsBinding : FragmentUserSettingsBinding? = null

    @Inject lateinit var mGoogleSignInClient : GoogleSignInClient
    @Inject lateinit var mAuth: FirebaseAuth
    @Inject lateinit var userDB: CollectionReference
    private lateinit var tripsDB: CollectionReference
    private lateinit var userRef: DocumentReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentUserSettingsBinding = FragmentUserSettingsBinding.inflate(inflater, container, false)

        userRef = userDB.document(mAuth.currentUser!!.uid)
        tripsDB = userRef.collection("trips")
        Glide.with(this).load(mAuth.currentUser!!.photoUrl).into(fragmentUserSettingsBinding!!.userSettingsImage)
        fragmentUserSettingsBinding!!.userSettingsName.text = mAuth.currentUser!!.displayName
        fragmentUserSettingsBinding!!.userSettingsEmail.text = mAuth.currentUser!!.email

        tripsDB.get().addOnSuccessListener { documentsSnapshot ->
            val count = documentsSnapshot.size()
            fragmentUserSettingsBinding!!.userSettingsTrips.text = "Total user Trips : ${count}"
        }

        userRef.addSnapshotListener{ snapshot, e ->
            if (e != null) {
                Timber.tag(ContentValues.TAG).w(e, "Listen failed.")
                return@addSnapshotListener
            }
            if ((snapshot != null) && snapshot.exists()) {
                fragmentUserSettingsBinding!!.userSettingsKmsTravelled.text = "Total kms travelled :   ${DecimalFormat("#.00").format(snapshot.get("kmsTravelled") as Double)} kms"
                fragmentUserSettingsBinding!!.userSettingsWallet.text = "Total amount added : ${snapshot.get("totalAmount") as Long}₹"

            }

        }

        fragmentUserSettingsBinding!!.userSettingsDelete.setOnClickListener {
            fragmentUserSettingsBinding!!.userSettingsDelete.visibility = View.GONE
            fragmentUserSettingsBinding!!.deleteAnimation.visibility = View.VISIBLE

            val confirmDeletion = AlertDialog.Builder(requireContext())
            confirmDeletion.setTitle("Are you sure?")
            confirmDeletion.setMessage("Once confirmed, you will loose access to your account and all data within will be lost.")
            confirmDeletion.setCancelable(false)
            confirmDeletion.setPositiveButton("I need to leave"){ _,_ ->

                val signInClient = mGoogleSignInClient.signInIntent
                launcher.launch(signInClient)

            }
            confirmDeletion.setNegativeButton("Cancel deletion"){ _,_ ->
                Toast.makeText(requireContext(),"We are happy you chose to stay...", Toast.LENGTH_SHORT).show()
                fragmentUserSettingsBinding!!.userSettingsDelete.visibility = View.VISIBLE
                fragmentUserSettingsBinding!!.deleteAnimation.visibility = View.GONE
            }

            confirmDeletion.create().show()

        }

        return fragmentUserSettingsBinding!!.root
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful){
                val account: GoogleSignInAccount?= task.result
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

                mAuth.currentUser?.reauthenticate(credential)?.addOnSuccessListener {

                    mAuth.currentUser!!.delete().addOnSuccessListener {
                    Toast.makeText(requireContext(),"We are sorry to see you go...", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Timber.tag("DeletionFailed").d(it)
                    Toast.makeText(requireContext(),"Account deletion failed. It's not meant to be :)", Toast.LENGTH_SHORT).show()
                    fragmentUserSettingsBinding!!.userSettingsDelete.visibility = View.VISIBLE
                    fragmentUserSettingsBinding!!.deleteAnimation.visibility = View.GONE
                }

                }?.addOnFailureListener {
                    Toast.makeText(requireContext(),"Invalid credentials. Select only your account!", Toast.LENGTH_SHORT).show()
                    fragmentUserSettingsBinding!!.userSettingsDelete.visibility = View.VISIBLE
                    fragmentUserSettingsBinding!!.deleteAnimation.visibility = View.GONE
                }

            } else {
                Toast.makeText(requireContext(),"We are happy you chose to stay...", Toast.LENGTH_SHORT).show()
                fragmentUserSettingsBinding!!.userSettingsDelete.visibility = View.VISIBLE
                fragmentUserSettingsBinding!!.deleteAnimation.visibility = View.GONE
            }
        } else {
            Toast.makeText(requireContext(),"Select the google account you want to delete", Toast.LENGTH_SHORT).show()
            fragmentUserSettingsBinding!!.userSettingsDelete.visibility = View.VISIBLE
            fragmentUserSettingsBinding!!.deleteAnimation.visibility = View.GONE
        }
    }

}