package com.example.fdcare.activity

import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import com.example.fdcare.R
import com.example.fdcare.databinding.ActivityHomeBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.sqrt

class HomeActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var binding: ActivityHomeBinding
    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sensorManager: SensorManager
    private lateinit var square: TextView
    var X = 0f
    var Y = 0f
    var Z = 0f
    var T = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation Drawer
        navigationDrawer()

        // Firebase Authorization
        firebaseAuth = FirebaseAuth.getInstance()

        // Gyroscope
        // Keeps phone in light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        square = binding.tvSquare
        setUpSensorStuff()
    }


    // **** Navigation Drawer ****
    private fun navigationDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayoutHome)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // when toggle is opened and back is pressed, the navigation bar will close
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val navDrawer = findViewById<NavigationView>(R.id.navDrawer)
        navDrawer.setNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navProfile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.navEditProfile -> startActivity(Intent(this, EditProfileActivity::class.java))
                R.id.navLogout -> {
                    firebaseAuth.signOut()
                    startActivity(Intent(this, LoginOptionsActivity::class.java))
                    finishAffinity()
                }
            }
            true
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    // **** Gyroscope ****
    private fun setUpSensorStuff() {
        // Create the sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Specify the sensor you want to listen to
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Checks for the sensor we have registered
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // x = Tilting phone left(10) and right(-10)
            val x = event.values[0]
            // y = Tilting phone up(10), flat (0), upside-down(-10)
            val y = event.values[1]
            // z = Moving phone forward and backward
            val z = event.values[2]

            square.apply {
                rotationX = y * 3f
                rotationY = x * 3f
                rotation = -x
                translationX = x * -10
                translationY = y * 10
            }

            // Changes the colour of the square if it's completely flat
            val color = if (y.toInt() == 0 && x.toInt() == 0) Color.GREEN else Color.RED
            square.setBackgroundColor(color)

            val dt = (event.timestamp - T)
            val ax = (x - X) / dt
            val ay = (y - Y) / dt
            val az = (z - Z) / dt
            val acceleration = sqrt(ax * ax + ay * ay + az * az) * 1e13

            binding.tvAcceleration.text = acceleration.toString()
            square.text = "Up/Down ${y.toInt()}\nLeft/Right ${x.toInt()}"
            X = x
            Y = y
            Z = z
            T = System.currentTimeMillis()

            if(acceleration > 10) {
                Toast.makeText(this, "Call Emergency!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}