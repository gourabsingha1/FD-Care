package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.fdcare.activity.HomePatientActivity
import com.example.fdcare.databinding.ActivitySignUpEmailPatientBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class SignUpEmailPatientActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpEmailPatientBinding
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
        binding = ActivitySignUpEmailPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // Get instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Setup ProgressDialogue to show while signup
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back
        binding.ivSignupPatientBack.setOnClickListener {
            onBackPressed()
        }

        // Sign up
        binding.btnSignupPatient.setOnClickListener {
            validateData()
        }
    }


    // Validate email and password
    private fun validateData() {
        name = binding.etSignupPatientName.text.toString().trim()
        email = binding.etSignupPatientEmail.text.toString().trim()
        phoneCode = binding.ccpSignupPatient.selectedCountryCodeWithPlus
        phoneNumber = binding.etSignupPatientPhone.text.toString().trim()
        password = binding.etSignupPatientPassword.text.toString().trim()
        confirmPassword = binding.etSignupPatientConfirmPassword.text.toString().trim()
        emergencyName = binding.etSignupPatientEmergencyName.text.toString().trim()
        emergencyEmail = binding.etSignupPatientEmergencyEmail.text.toString().trim()
        emergencyPhoneCode = binding.ccpSignupPatientEmergency.selectedCountryCodeWithPlus
        emergencyPhoneNumber = binding.etSignupPatientEmergencyPhone.text.toString().trim()

        if(name.isEmpty()) {
            binding.etSignupPatientName.error = "Enter your name"
            binding.etSignupPatientName.requestFocus()
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignupPatientEmail.error = "Invalid email format"
            binding.etSignupPatientEmail.requestFocus()
        } else if(phoneNumber.length != 10) {
            binding.etSignupPatientPhone.error = "Invalid phone number"
            binding.etSignupPatientPhone.requestFocus()
        } else if(password.isEmpty()) {
            binding.etSignupPatientPassword.error = "Enter password"
            binding.etSignupPatientPassword.requestFocus()
        } else if(confirmPassword.isEmpty()) {
            binding.etSignupPatientConfirmPassword.error = "Confirm password"
            binding.etSignupPatientConfirmPassword.requestFocus()
        } else if(password != confirmPassword) {
            binding.etSignupPatientConfirmPassword.error = "Password doesn't match"
            binding.etSignupPatientConfirmPassword.requestFocus()
        } else if(emergencyName.isEmpty()) {
            binding.etSignupPatientEmergencyName.error = "Enter an emergency name"
            binding.etSignupPatientEmergencyName.requestFocus()
        } else if(emergencyEmail.isEmpty()) {
            binding.etSignupPatientEmergencyEmail.error = "Enter an emergency email"
            binding.etSignupPatientEmergencyEmail.requestFocus()
        } else if(emergencyPhoneNumber.length != 10) {
            binding.etSignupPatientEmergencyPhone.error = "Invalid phone number"
            binding.etSignupPatientEmergencyPhone.requestFocus()
        } else {
            signupFirebase()
        }
    }


    // Sign Up Firebase
    private fun signupFirebase() {
        progressDialog.setMessage("Creating account")
        progressDialog.show()

        val email = binding.etSignupPatientEmail.text.toString()
        val password = binding.etSignupPatientPassword.text.toString()

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
        progressDialog.setMessage("Saving Patient Info")

        // Get new FCM registration token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            val token = task.result

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
            hashMap["fcmToken"] = token
            hashMap["caretakerUid"] = ""

            // set data to firebase realtime db
            val reference = FirebaseDatabase.getInstance().getReference("Patients")
            reference.child(registeredUserId!!).setValue(hashMap).addOnSuccessListener {
                Toast.makeText(this, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomePatientActivity::class.java))
                finishAffinity()
            }.addOnFailureListener { e ->
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        })

        progressDialog.dismiss()
    }
}