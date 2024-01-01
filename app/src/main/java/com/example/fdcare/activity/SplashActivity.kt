package com.example.fdcare.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.example.fdcare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide action bar
        supportActionBar?.hide()

        // initializations
        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed({
            if(firebaseAuth.currentUser == null) {
                Intent(this, LoginOptionsActivity::class.java).also { intent ->
                    startActivity(intent)
                    finishAffinity()
                }
            }
            else {
                val ref = FirebaseDatabase.getInstance().getReference("Patients")
                ref.child(firebaseAuth.uid!!).addValueEventListener(object: ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()) {
                            Intent(this@SplashActivity, HomePatientActivity::class.java).also { intent ->
                                startActivity(intent)
                                finishAffinity()
                            }
                        }
                        else {
                            Intent(this@SplashActivity, HomeCaretakerActivity::class.java).also { intent ->
                                startActivity(intent)
                                finishAffinity()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("dbError", error.message)
                    }
                })
            }
        }, 3000)
    }
}