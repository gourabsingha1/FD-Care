package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.fdcare.databinding.ActivitySignUpEmailCaretakerBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignUpEmailCaretakerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpEmailCaretakerBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var name = ""
    private var email = ""
    private var phoneCode = ""
    private var phoneNumber = ""
    private var password = ""
    private var confirmPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpEmailCaretakerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // Get instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Setup ProgressDialogue to show while signup
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back
        binding.ivSignupCaretakerBack.setOnClickListener {
            onBackPressed()
        }

        // Sign up
        binding.btnSignupCaretaker.setOnClickListener {
            validateData()
        }
    }


    // Validate email and password
    private fun validateData() {
        name = binding.etSignupCaretakerName.text.toString().trim()
        email = binding.etSignupCaretakerEmail.text.toString().trim()
        phoneCode = binding.ccpSignupCaretaker.selectedCountryCodeWithPlus
        phoneNumber = binding.etSignupCaretakerPhone.text.toString().trim()
        password = binding.etSignupCaretakerPassword.text.toString().trim()
        confirmPassword = binding.etSignupCaretakerConfirmPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.etSignupCaretakerName.error = "Enter your name"
            binding.etSignupCaretakerName.requestFocus()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignupCaretakerEmail.error = "Invalid email format"
            binding.etSignupCaretakerEmail.requestFocus()
        } else if (phoneNumber.length != 10) {
            binding.etSignupCaretakerPhone.error = "Invalid phone number"
            binding.etSignupCaretakerPhone.requestFocus()
        } else if (password.isEmpty()) {
            binding.etSignupCaretakerPassword.error = "Enter password"
            binding.etSignupCaretakerPassword.requestFocus()
        } else if (confirmPassword.isEmpty()) {
            binding.etSignupCaretakerConfirmPassword.error = "Confirm password"
            binding.etSignupCaretakerConfirmPassword.requestFocus()
        } else if (password != confirmPassword) {
            binding.etSignupCaretakerConfirmPassword.error = "Password doesn't match"
            binding.etSignupCaretakerConfirmPassword.requestFocus()
        } else {
            signupFirebase()
        }
    }


    // Sign Up Firebase
    private fun signupFirebase() {
        progressDialog.setMessage("Creating account")
        progressDialog.show()

        val email = binding.etSignupCaretakerEmail.text.toString()
        val password = binding.etSignupCaretakerPassword.text.toString()

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                updateUserInfo()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Saving Caretaker Info")

        // Get new FCM registration token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            val token = task.result

            val hashMap = HashMap<String, Any?>()
            hashMap["uid"] = firebaseAuth.uid
            hashMap["name"] = name
            hashMap["email"] = email
            hashMap["phoneCode"] = phoneCode
            hashMap["phoneNumber"] = phoneNumber
            hashMap["profileImageUrl"] = ""
            hashMap["token"] = token
            hashMap["onlineStatus"] = "true"
            hashMap["patientUid"] = ""

            // set data to firebase realtime db
            val reference = FirebaseDatabase.getInstance().getReference("Caretakers")
            reference.child(firebaseAuth.uid!!).setValue(hashMap).addOnSuccessListener {
                Toast.makeText(this, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
                Intent(this, HomeCaretakerActivity::class.java).also { intent ->
                    startActivity(intent)
                    finishAffinity()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        })

        progressDialog.dismiss()
    }
}