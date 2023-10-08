package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.fdcare.databinding.ActivitySignUpEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpEmailBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var name = ""
    private var email = ""
    private var phoneCode = ""
    private var phoneNumber = ""
    private var password = ""
    private var confirmPassword = ""
    private var emergencyName = ""
    private var emergencyEmail = ""
    private var emergencyPhoneCode = ""
    private var emergencyPhoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // Get instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Setup ProgressDialogue to show while signup
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back
        binding.ivSignupBack.setOnClickListener {
            onBackPressed()
        }

        // Sign up
        binding.btnSignup.setOnClickListener {
            validateData()
        }
    }


    // Validate email and password
    private fun validateData() {
        name = binding.etSignupName.text.toString().trim()
        email = binding.etSignupEmail.text.toString().trim()
        phoneCode = binding.ccpSignup.selectedCountryCodeWithPlus
        phoneNumber = binding.etSignupPhone.text.toString().trim()
        password = binding.etSignupPassword.text.toString().trim()
        confirmPassword = binding.etSignupConfirmPassword.text.toString().trim()
        emergencyName = binding.etSignupEmergencyName.text.toString().trim()
        emergencyEmail = binding.etSignupEmergencyEmail.text.toString().trim()
        emergencyPhoneCode = binding.ccpSignupEmergency.selectedCountryCodeWithPlus
        emergencyPhoneNumber = binding.etSignupEmergencyPhone.text.toString().trim()

        if(name.isEmpty()) {
            binding.etSignupName.error = "Enter your name"
            binding.etSignupName.requestFocus()
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignupEmail.error = "Invalid email format"
            binding.etSignupEmail.requestFocus()
        } else if(phoneNumber.length != 10) {
            binding.etSignupPhone.error = "Invalid phone number"
            binding.etSignupPhone.requestFocus()
        } else if(password.isEmpty()) {
            binding.etSignupPassword.error = "Enter password"
            binding.etSignupPassword.requestFocus()
        } else if(confirmPassword.isEmpty()) {
            binding.etSignupConfirmPassword.error = "Confirm password"
            binding.etSignupConfirmPassword.requestFocus()
        } else if(password != confirmPassword) {
            binding.etSignupConfirmPassword.error = "Password doesn't match"
            binding.etSignupConfirmPassword.requestFocus()
        } else if(emergencyName.isEmpty()) {
            binding.etSignupEmergencyName.error = "Enter an emergency name"
            binding.etSignupEmergencyName.requestFocus()
        } else if(emergencyEmail.isEmpty()) {
            binding.etSignupEmergencyEmail.error = "Enter an emergency email"
            binding.etSignupEmergencyEmail.requestFocus()
        } else if(emergencyPhoneNumber.length != 10) {
            binding.etSignupEmergencyPhone.error = "Invalid phone number"
            binding.etSignupEmergencyPhone.requestFocus()
        } else {
            signupFirebase()
        }
    }


    // Sign Up Firebase
    private fun signupFirebase() {
        progressDialog.setMessage("Creating account")
        progressDialog.show()

        val email = binding.etSignupEmail.text.toString()
        val password = binding.etSignupPassword.text.toString()

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
            if(it.isSuccessful) {
                updateUserInfo()
            } else {
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }

    private fun updateUserInfo() {
        progressDialog.setMessage("Saving User Info")

        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserId = firebaseAuth.uid

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = name
        hashMap["email"] = registeredUserEmail
        hashMap["uid"] = registeredUserId
        hashMap["phoneCode"] = phoneCode
        hashMap["phoneNumber"] = phoneNumber
        hashMap["profileImageUrl"] = ""
        hashMap["onlineStatus"] = true
        hashMap["emergencyName"] = emergencyName
        hashMap["emergencyEmail"] = emergencyEmail
        hashMap["emergencyPhoneCode"] = emergencyPhoneCode
        hashMap["emergencyPhoneNumber"] = emergencyPhoneNumber

        // set data to firebase realtime db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserId!!).setValue(hashMap).addOnSuccessListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finishAffinity()
        }.addOnFailureListener { e ->
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            progressDialog.dismiss()
        }
    }
}