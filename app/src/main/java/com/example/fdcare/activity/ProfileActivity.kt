package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.fdcare.R
import com.example.fdcare.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var progressDialog: ProgressDialog

    private companion object {
        private const val TAG = "ACCOUNT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // Setup ProgressDialogue to show while loading user profile
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back
        binding.ivProfileBack.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Load Data
        loadMyData()
    }

    private fun loadMyData() {
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        val firebaseAuth = FirebaseAuth.getInstance()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // get data
                val name = "${snapshot.child("name").value}"
                val email = "${snapshot.child("email").value}"
                val phoneCode = "${snapshot.child("phoneCode").value}"
                val phoneNumber = "${snapshot.child("phoneNumber").value}"
                val phone = phoneCode + phoneNumber
                val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                val onlineStatus = "${snapshot.child("onlineStatus").value}"
                val emergencyName = "${snapshot.child("emergencyName").value}"
                val emergencyEmail = "${snapshot.child("emergencyEmail").value}"
                val emergencyPhoneCode = "${snapshot.child("emergencyPhoneCode").value}"
                val emergencyPhoneNumber = "${snapshot.child("emergencyPhoneNumber").value}"
                val emergencyPhone = emergencyPhoneCode + emergencyPhoneNumber

                // set data
                try {
                    Glide.with(this@ProfileActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.ivProfileProfilePic)
                } catch (e : Exception) {
                    Log.e(ProfileActivity.TAG, "onDataChange: ", e)
                }
                binding.tvProfileName.text = "Name:\t $name"
                binding.tvProfileEmail.text = "Email:\t $email"
                binding.tvProfilePhone.text = "Phone:\t $phone"
                binding.tvProfileEmergencyName.text = "Emergency name:\t $emergencyName"
                binding.tvProfileEmergencyEmail.text = "Emergency email:\t $emergencyEmail"
                binding.tvProfileEmergencyPhone.text = "Emergency phone:\t$emergencyPhone"

                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}