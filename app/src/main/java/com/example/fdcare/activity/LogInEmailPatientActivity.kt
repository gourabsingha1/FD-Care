package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fdcare.activity.HomePatientActivity
import com.example.fdcare.databinding.ActivityLoginEmailPatientBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class LogInEmailPatientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailPatientBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // Get instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Setup ProgressDialogue to show while login
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back
        binding.ivLoginEmailPatientBack.setOnClickListener {
            onBackPressed()
        }

        // Login
        binding.btnLoginPatient.setOnClickListener {
            validateData()
        }

        // Sign up patient activity
        binding.tvLoginEmailPatientSignup.setOnClickListener {
            startActivity(Intent(this, SignUpEmailPatientActivity::class.java))
        }
    }


    // Validate email and password
    private fun validateData() {
        email = binding.etLoginEmailPatientEmail.text.toString().trim()
        password = binding.etLoginEmailPatientPassword.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etLoginEmailPatientEmail.error = "Invalid Email Format"
            binding.etLoginEmailPatientEmail.requestFocus()
        } else if(password.isEmpty()) {
            binding.etLoginEmailPatientPassword.error = "Enter Password"
            binding.etLoginEmailPatientPassword.requestFocus()
        } else {
            loginFirebase()
        }
    }

    // Login Firebase
    private fun loginFirebase() {
        progressDialog.setMessage("Logging In")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                updateToken()
                setCaretakerUid()
                setPatientOnlineStatus(true)

                Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                Intent(this, HomePatientActivity::class.java).also { intent ->
                    startActivity(intent)
                    finishAffinity()
                }
            } else {
                Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }


    // Need to update patient's token when they login from another device
    private fun updateToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val newToken = task.result
            FirebaseDatabase.getInstance().getReference("Patients").child(firebaseAuth.uid!!).child("token").setValue(newToken)
        })
    }

    private fun setCaretakerUid() {
        // check caretakerUid is set or not in O(1) time
        FirebaseDatabase.getInstance().getReference("Patients")
            .child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val caretakerEmail = snapshot.child("caretakerEmail").value
                    var caretakerUid = snapshot.child("caretakerUid").value

                    if(caretakerUid == "") {
                        // scan all caretakers with email as emergencyEmail
                        FirebaseDatabase.getInstance().getReference("Caretakers")
                            .addValueEventListener(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(ds in snapshot.children) {
                                        val email = "${ds.child("email").value}"
                                        caretakerUid = "${ds.child("uid").value}"

                                        if(caretakerEmail == email) {
                                            // set caretakerUid
                                            FirebaseDatabase.getInstance().getReference("Patients")
                                                .child(firebaseAuth.uid!!).child("caretakerUid").setValue(caretakerUid)
                                            return
                                        }
                                    }
                                    progressDialog.dismiss()
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

    private fun setPatientOnlineStatus(value : Boolean) {
        if(firebaseAuth.currentUser != null) {
            FirebaseDatabase.getInstance().getReference("Patients")
                .child(firebaseAuth.uid!!).child("onlineStatus").setValue(value.toString())
        }
    }
}