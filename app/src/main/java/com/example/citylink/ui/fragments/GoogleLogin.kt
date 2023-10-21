package com.example.citylink.ui.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.citylink.R
import com.example.citylink.databinding.FragmentGoogleLoginBinding
import com.example.citylink.services.NetworkConnectivityObserver
import com.example.citylink.ui.HomePage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
@AndroidEntryPoint
class GoogleLogin : Fragment() {

    private var fragGoogleLogin : FragmentGoogleLoginBinding? = null
    //Firebase Declarations-------------------------------------------------------------------------
    @Inject
    lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser
    @Inject
    lateinit var userDB: CollectionReference
    private lateinit var userRef: DocumentReference
    private var account: GoogleSignInAccount? = null

    //GoogleSignIn Declaration----------------------------------------------------------------------
    @Inject
    lateinit var mGoogleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())
    }
    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (currentUser != null && account != null) {
            findNavController().navigate(R.id.action_googleLogin_to_homeFragment)
        }else{
            mGoogleSignInClient.signOut()
            mAuth.signOut()
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragGoogleLogin = FragmentGoogleLoginBinding.inflate(inflater, container, false)

        fragGoogleLogin!!.login.setOnClickListener{
            setupLogin()
        }

        return fragGoogleLogin!!.root
    }

    private fun setupLogin() {
        val signInClient = mGoogleSignInClient.signInIntent
        fragGoogleLogin!!.login.visibility = View.GONE
        fragGoogleLogin!!.loginAnimation.visibility = View.VISIBLE
        launcher.launch(signInClient)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful){
                val account: GoogleSignInAccount?= task.result
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
                mAuth.signInWithCredential(credential).addOnCompleteListener{

                    mUser = mAuth.currentUser!!
                    userRef = userDB.document(mUser.uid)
                    userRef.get().addOnCompleteListener{ task ->
                        if (task.isSuccessful){
                            val document = task.result
                            if(document.exists()){
                                Toast.makeText(requireContext(), "LoggedIn Successfully", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_googleLogin_to_homeFragment)
                            }else{
                                val user = hashMapOf(
                                    "id" to mUser.uid,
                                    "kmsTravelled" to 0.00001,
                                    "totalAmount" to 0,
                                    "wallet" to 0,
                                    "isTracking" to false
                                )
                                userDB.document(mUser.uid).set(user).addOnSuccessListener{
                                    Toast.makeText(requireContext(), "SignedIn Successfully", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_googleLogin_to_homeFragment)
                                }
                            }
                        }else{
                            Timber.tag("TAG").d(task.exception, "Error: ")
                        }
                    }
                }.addOnFailureListener{
                    Toast.makeText(requireContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    fragGoogleLogin!!.login.visibility = View.VISIBLE
                    fragGoogleLogin!!.loginAnimation.visibility = View.GONE
                }
            }else{
                Toast.makeText(requireContext(), "SignIn Failed", Toast.LENGTH_SHORT).show()
                fragGoogleLogin!!.login.visibility = View.VISIBLE
                fragGoogleLogin!!.loginAnimation.visibility = View.GONE
            }
        }else{
            Toast.makeText(requireContext(), "Please select a Google Account", Toast.LENGTH_SHORT).show()
            fragGoogleLogin!!.login.visibility = View.VISIBLE
            fragGoogleLogin!!.loginAnimation.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}