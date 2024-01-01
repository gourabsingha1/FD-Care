package com.example.fdcare.activity

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
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

        // Grant notification permission
        if(!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.add_contact_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val tvAddContactMessage = dialog.findViewById<TextView>(R.id.tvAddContactMessage)
            tvAddContactMessage.text = "Grant notification permission"

            dialog.findViewById<Button>(R.id.btnDismiss).text = "Open settings"
            val btnDismiss = dialog.findViewById<Button>(R.id.btnDismiss)
            btnDismiss.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
                dialog.dismiss()
            }

            dialog.show()
        }

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
            Intent(this, SplashActivity::class.java).also { intent ->
                startActivity(intent)
                finishAffinity()
            }
        }

        linkCaretakerAndPatient()

        // If patient does not exist, a dialog will pop up
        Handler().postDelayed({
            checkCaretakersPatient()
        }, 1000)
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
                    Log.d("dbError", error.message)
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
        if(firebaseAuth.currentUser != null) {
            FirebaseDatabase.getInstance().getReference("Caretakers")
                .child(firebaseAuth.uid!!).child("onlineStatus").setValue(value.toString())
        }
    }


    // Link caretaker and patient if patient exists
    private fun linkCaretakerAndPatient() {
        FirebaseDatabase.getInstance().getReference("Caretakers")
            .child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = snapshot.child("email").value.toString()
                    val token = snapshot.child("token").value.toString()
                    val patientUid = snapshot.child("patientUid").value.toString()

                    if(patientUid == "") {
                        // Look for matching caretaker email in patients
                        FirebaseDatabase.getInstance().getReference("Patients")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(ds in snapshot.children) {
                                        val uid = ds.child("uid").value.toString()
                                        val caretakerEmail = ds.child("caretakerEmail").value.toString()

                                        if(caretakerEmail == email) {
                                            // Update patient details in caretaker list
                                            FirebaseDatabase.getInstance().getReference("Caretakers")
                                                .child(firebaseAuth.uid!!).child("patientUid").setValue(uid)

                                            // Update caretaker details in patient list
                                            FirebaseDatabase.getInstance().getReference("Patients")
                                                .child(uid).child("caretakerUid").setValue(firebaseAuth.uid.toString())
                                            FirebaseDatabase.getInstance().getReference("Patients")
                                                .child(uid).child("caretakerToken").setValue(token)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("dbError", error.message)
                                }

                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("dbError", error.message)
                }

            })
    }


    // If caretaker has no patient then show dialog
    private fun checkCaretakersPatient() {
        // check patientUid is set or not in O(1) time
        FirebaseDatabase.getInstance().getReference("Caretakers")
            .child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val patientUid = "${snapshot.child("patientUid").value}"
                    if(patientUid == "") {
                        showDialog("Please register your patient with caretaker email:\n\n${firebaseAuth.currentUser?.email}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("dbError", error.message)
                }

            })
    }

    // Show dialog
    private fun showDialog(message : String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.add_contact_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvAddContactMessage = dialog.findViewById<TextView>(R.id.tvAddContactMessage)
        tvAddContactMessage.text = message
        val btnDismiss = dialog.findViewById<Button>(R.id.btnDismiss)
        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}