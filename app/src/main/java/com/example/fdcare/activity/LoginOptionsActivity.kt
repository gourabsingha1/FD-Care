package com.example.fdcare.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fdcare.databinding.ActivityLoginOptionsBinding

class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

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