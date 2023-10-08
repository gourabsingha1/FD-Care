package com.example.fdcare.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.fdcare.R
import com.example.fdcare.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

// doesnt work

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private companion object {
        private const val TAG = "ACCOUNT_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_profile)

        binding.button.setOnClickListener {
            Toast.makeText(this, "Camera or Storage or both permissions denied", Toast.LENGTH_LONG).show()
        }

        // Load Data
        loadMyData()
    }

    private fun loadMyData() {
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

                // set data
                binding.tvProfileName.text = name
                binding.tvProfileEmail.text = email
                binding.tvProfilePhone.text = phone
                try {
                    Glide.with(this@ProfileActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.ivProfileProfilePic)
                } catch (e : Exception) {
                    Log.e(ProfileActivity.TAG, "onDataChange: ", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}