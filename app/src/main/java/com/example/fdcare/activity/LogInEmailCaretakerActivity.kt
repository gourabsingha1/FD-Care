package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fdcare.databinding.ActivityLoginEmailCaretakerBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class LogInEmailCaretakerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailCaretakerBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailCaretakerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // Get instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Setup ProgressDialogue to show while login
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back
        binding.ivLoginEmailCaretakerBack.setOnClickListener {
            onBackPressed()
        }

        // Login
        binding.btnLoginCaretaker.setOnClickListener {
            validateData()
        }

        // Sign up caretaker activity
        binding.tvLoginEmailCaretakerSignup.setOnClickListener {
            startActivity(Intent(this, SignUpEmailCaretakerActivity::class.java))
        }
    }


    // Validate email and password
    private fun validateData() {
        email = binding.etLoginEmailCaretakerEmail.text.toString().trim()
        password = binding.etLoginEmailCaretakerPassword.text.toString().trim()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etLoginEmailCaretakerEmail.error = "Invalid Email Format"
            binding.etLoginEmailCaretakerEmail.requestFocus()
        } else if(password.isEmpty()) {
            binding.etLoginEmailCaretakerPassword.error = "Enter Password"
            binding.etLoginEmailCaretakerPassword.requestFocus()
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
                setPatientUid()
                setCaretakerOnlineStatus(true)

                Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
                Intent(this, HomeCaretakerActivity::class.java).also { intent ->
                    startActivity(intent)
                    finishAffinity()
                }
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }


    // Need to update caretaker's token when they login from another device
    private fun updateToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val newToken = task.result
            val reference = FirebaseDatabase.getInstance().getReference("Caretakers")
            reference.child("${firebaseAuth.uid}").child("fcmToken").setValue(newToken)
        })
    }

    // Caretaker to patient mapping is done below
    private fun setPatientUid() {
        FirebaseDatabase.getInstance().getReference("Caretakers")
            .child("${firebaseAuth.uid}").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val email = snapshot.child("email").value
                    var patientUid = snapshot.child("patientUid").value

                    // check patientUid is set or not in O(1) time
                    if(patientUid == "") {
                        // scan all patients with emergency email as caretaker's email
                        FirebaseDatabase.getInstance().getReference("Patients")
                            .addValueEventListener(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for(ds in snapshot.children) {
                                        val emergencyEmail = "${ds.child("emergencyEmail").value}"
                                        patientUid = "${ds.child("uid").value}"

                                        if(emergencyEmail == email) {
                                            // set patientUid
                                            FirebaseDatabase.getInstance().getReference("Caretakers")
                                                .child("${firebaseAuth.uid}").child("patientUid")
                                                .setValue(patientUid)
                                            return
                                        }
                                    }
                                    progressDialog.dismiss()
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }
                }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setCaretakerOnlineStatus(value : Boolean) {
        FirebaseDatabase.getInstance().getReference("Caretakers")
            .child(firebaseAuth.uid!!).child("onlineStatus").setValue(value.toString())
    }
}