package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.fdcare.R
import com.example.fdcare.databinding.ActivityProfilePatientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception

class ProfilePatientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfilePatientBinding
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // Setup ProgressDialogue to show while loading user profile
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back
        binding.ivProfilePatientBack.setOnClickListener {
            startActivity(Intent(this, HomePatientActivity::class.java))
            finish()
        }

        // Load Data
        loadMyData()
    }

    private fun loadMyData() {
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        val firebaseAuth = FirebaseAuth.getInstance()
        FirebaseDatabase.getInstance().getReference("Patients")
            .child(firebaseAuth.uid!!).addValueEventListener(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    // get data
                    val name = "${snapshot.child("name").value}"
                    val email = "${snapshot.child("email").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val phone = phoneCode + phoneNumber
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                    val onlineStatus = "${snapshot.child("onlineStatus").value}"
                    val caretakerName = "${snapshot.child("caretakerName").value}"
                    val caretakerEmail = "${snapshot.child("caretakerEmail").value}"
                    val caretakerPhoneCode = "${snapshot.child("caretakerPhoneCode").value}"
                    val caretakerPhoneNumber = "${snapshot.child("caretakerPhoneNumber").value}"
                    val caretakerPhone = caretakerPhoneCode + caretakerPhoneNumber

                    // set data
                    try {
                        Glide.with(this@ProfilePatientActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.ivProfilePatientProfilePic)
                    } catch (e : Exception) {
                        Log.e("profilePatientActivity", "onDataChange: ", e)
                    }
                    binding.tvProfilePatientName.text = "Name:\t $name"
                    binding.tvProfilePatientEmail.text = "Email:\t $email"
                    binding.tvProfilePatientPhone.text = "Phone:\t $phone"
                    binding.tvProfilePatientCaretakerName.text = "Caretaker name:\t $caretakerName"
                    binding.tvProfilePatientCaretakerEmail.text = "Caretaker email:\t $caretakerEmail"
                    binding.tvProfilePatientCaretakerPhone.text = "Caretaker phone:\t$caretakerPhone"

                    progressDialog.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}