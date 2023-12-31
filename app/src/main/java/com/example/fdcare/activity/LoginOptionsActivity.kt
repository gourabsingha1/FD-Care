package com.example.fdcare.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fdcare.databinding.ActivityLoginOptionsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginOptionsBinding
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // initializations
        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser != null) {

            val ref = FirebaseDatabase.getInstance().getReference("Patients")
            ref.child(firebaseAuth.uid!!).addValueEventListener(object: ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        startActivity(Intent(this@LoginOptionsActivity, HomePatientActivity::class.java))
                        finishAffinity()
                    }
                    else {
                        startActivity(Intent(this@LoginOptionsActivity, HomeCaretakerActivity::class.java))
                        finishAffinity()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        // Patient login
        binding.btnLoginOptionsPatient.setOnClickListener {
            startActivity(Intent(this, LogInEmailPatientActivity::class.java))
        }

        // Caretaker login
        binding.btnLoginOptionsCaretaker.setOnClickListener {
            startActivity(Intent(this, LogInEmailCaretakerActivity::class.java))
        }
    }
}