package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fdcare.MediaPlayerSingleton
import com.example.fdcare.R
import com.example.fdcare.databinding.ActivityHomeCaretakerBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeCaretakerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeCaretakerBinding
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeCaretakerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ProgressDialogue to show while loading patient details
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Firebase Authorization
        firebaseAuth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)

        // Load Data
        loadMyData()

        // Stop Ringing
        binding.btnStopRinging.setOnClickListener {
            stopRinging()
        }

        // Logout
        binding.btnLogOut.setOnClickListener {
            stopRinging()
            setCaretakerOnlineStatus(false)

            firebaseAuth.signOut()
            startActivity(Intent(this, LoginOptionsActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadMyData() {
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        FirebaseDatabase.getInstance().getReference("Caretakers")
            .child(firebaseAuth.uid!!).child("patientUid").addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val patientUid = snapshot.value.toString()
                    FirebaseDatabase.getInstance().getReference("Patients")
                        .child(patientUid).addValueEventListener(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // get data
                                val name = "${snapshot.child("name").value}"
                                val email = "${snapshot.child("email").value}"
                                val phoneCode = "${snapshot.child("phoneCode").value}"
                                val phoneNumber = "${snapshot.child("phoneNumber").value}"
                                val phone = phoneCode + phoneNumber
                                val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                                val onlineStatus = "${snapshot.child("onlineStatus").value}"

                                // set data
                                try {
                                    Glide.with(this@HomeCaretakerActivity)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.ic_person_white)
                                        .into(binding.ivHomeCaretakerProfilePic)
                                } catch (e : Exception) {
                                    Log.e("homeCaretakerActivity", "onDataChange: ", e)
                                }
                                binding.tvHomeCaretakerName.text = "Name:\t $name"
                                binding.tvHomeCaretakerEmail.text = "Email:\t $email"
                                binding.tvHomeCaretakerPhone.text = "Phone:\t $phone"

                                progressDialog.dismiss()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun stopRinging() {
        MediaPlayerSingleton.mediaPlayer?.let {
            if (it.isPlaying) {
                it.isLooping = false
                it.stop()
            }
            it.release()
        }
        MediaPlayerSingleton.mediaPlayer = null
    }

    private fun setCaretakerOnlineStatus(value : Boolean) {
        FirebaseDatabase.getInstance().getReference("Caretakers")
            .child(firebaseAuth.uid!!).child("onlineStatus").setValue(value.toString())
    }
}