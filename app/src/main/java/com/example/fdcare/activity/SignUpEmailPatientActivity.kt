package com.example.fdcare.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
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
    private var caretakerName = ""
    private var caretakerEmail = ""
    private var caretakerPhoneCode = ""
    private var caretakerPhoneNumber = ""

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
        caretakerName = binding.etSignupPatientCaretakerName.text.toString().trim()
        caretakerEmail = binding.etSignupPatientCaretakerEmail.text.toString().trim()
        caretakerPhoneCode = binding.ccpSignupPatientCaretaker.selectedCountryCodeWithPlus
        caretakerPhoneNumber = binding.etSignupPatientCaretakerPhone.text.toString().trim()

        if (name.isEmpty()) {
            binding.etSignupPatientName.error = "Enter name"
            binding.etSignupPatientName.requestFocus()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etSignupPatientEmail.error = "Invalid email format"
            binding.etSignupPatientEmail.requestFocus()
        } else if (phoneNumber.length != 10) {
            binding.etSignupPatientPhone.error = "Invalid phone number"
            binding.etSignupPatientPhone.requestFocus()
        } else if (password.isEmpty()) {
            binding.etSignupPatientPassword.error = "Enter password"
            binding.etSignupPatientPassword.requestFocus()
        } else if (confirmPassword.isEmpty()) {
            binding.etSignupPatientConfirmPassword.error = "Confirm password"
            binding.etSignupPatientConfirmPassword.requestFocus()
        } else if (password != confirmPassword) {
            binding.etSignupPatientConfirmPassword.error = "Password doesn't match"
            binding.etSignupPatientConfirmPassword.requestFocus()
        } else if (caretakerName.isEmpty()) {
            binding.etSignupPatientCaretakerName.error = "Enter caretaker name"
            binding.etSignupPatientCaretakerName.requestFocus()
        } else if (caretakerEmail.isEmpty()) {
            binding.etSignupPatientCaretakerEmail.error = "Enter caretaker email"
            binding.etSignupPatientCaretakerEmail.requestFocus()
        } else if(!Patterns.EMAIL_ADDRESS.matcher(caretakerEmail).matches()) {
            binding.etSignupPatientEmail.error = "Invalid email format"
            binding.etSignupPatientEmail.requestFocus()
        } else if (caretakerPhoneNumber.length != 10) {
            binding.etSignupPatientCaretakerPhone.error = "Invalid phone number"
            binding.etSignupPatientCaretakerPhone.requestFocus()
        } else if (email == caretakerEmail) {
            binding.etSignupPatientEmail.error = "Same patient and caretaker email"
            binding.etSignupPatientEmail.requestFocus()
            binding.etSignupPatientCaretakerEmail.error = "Same patient and caretaker email"
            binding.etSignupPatientCaretakerEmail.requestFocus()
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
        progressDialog.setMessage("Saving Patient Info")

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
            hashMap["caretakerUid"] = ""
            hashMap["caretakerName"] = caretakerName
            hashMap["caretakerEmail"] = caretakerEmail
            hashMap["caretakerPhoneCode"] = caretakerPhoneCode
            hashMap["caretakerPhoneNumber"] = caretakerPhoneNumber
            hashMap["caretakerToken"] = ""

            // set data to firebase realtime db
            val reference = FirebaseDatabase.getInstance().getReference("Patients")
            reference.child(firebaseAuth.uid!!).setValue(hashMap).addOnSuccessListener {
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