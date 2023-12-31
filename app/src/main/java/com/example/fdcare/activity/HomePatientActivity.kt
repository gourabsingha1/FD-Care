package com.example.fdcare.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.fdcare.R
import com.example.fdcare.databinding.ActivityHomePatientBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlin.math.sqrt

class HomePatientActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityHomePatientBinding
    private lateinit var toggle: ActionBarDrawerToggle
    lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sensorManager: SensorManager
    var X = 0f
    var Y = 0f
    var Z = 0f
    var T = 0L
    private val serverKey = "AAAAyEJ6_jc:APA91bHDhMfWJGaoJBRU7_1kx_qdVqALp4ybhjat6x4QvYz9fUNAPctciFvR81IEQ9bzKHquRmerqefkAig8cc0ZGgwrvair_8R0qiufKY3E_YzjaPN3dM1citcUsHreHHb5-tsSX8ly"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigation Drawer
        navigationDrawer()

        // Firebase Authorization
        firebaseAuth = FirebaseAuth.getInstance()
        FirebaseApp.initializeApp(this)

        // Keeps phone in light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Gyroscope
        setUpSensorStuff()

        // Send notification
        binding.btnSendNotification.setOnClickListener {
            // We need caretaker's token to send notification
            getCaretakerDeviceToken()
        }

        // If caretaker does not exist, a dialog will pop up
        checkPatientsCaretaker()
    }


    // **** Navigation Drawer ****
    private fun navigationDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayoutHomePatient)
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        // when toggle is opened and back is pressed, the navigation bar will close
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val navDrawer = findViewById<NavigationView>(R.id.navDrawer)
        navDrawer.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navPatientProfile -> startActivity(Intent(this, ProfilePatientActivity::class.java))
//                R.id.navEditProfile -> startActivity(Intent(this, EditProfileActivity::class.java))
                R.id.navLogout -> {
                    setPatientOnlineStatus(false)

                    firebaseAuth.signOut()
                    Intent(this, SplashActivity::class.java).also { intent ->
                        startActivity(intent)
                        finishAffinity()
                    }
                }
            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
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
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Checks for the sensor we have registered
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // x = Tilting phone left(10) and right(-10)
            val x = event.values[0]
            // y = Tilting phone up(10), flat (0), upside-down(-10)
            val y = event.values[1]
            // z = Moving phone forward and backward
            val z = event.values[2]

            // acceleration calculation
            val dt = (event.timestamp - T)
            val ax = (x - X) / dt
            val ay = (y - Y) / dt
            val az = (z - Z) / dt
            val acceleration1 = sqrt(ax * ax + ay * ay + az * az) * 1e13 // probably correct with limit 10
            val acceleration2 = sqrt(x * x + y * y + z * z)
            binding.tvAcceleration1Val.text = acceleration1.toString()
            binding.tvAcceleration2Val.text = acceleration2.toString()
            runOnUiThread {
                binding.tvAccelerationX.text = "X Axis: ${"%.2f".format(x)} m/s²"
                binding.tvAccelerationY.text = "Y Axis: ${"%.2f".format(y)} m/s²"
                binding.tvAccelerationZ.text = "Z Axis: ${"%.2f".format(z)} m/s²"
            }
            X = x
            Y = y
            Z = z
            T = System.currentTimeMillis()

            if (acceleration1 > 10) {
                Toast.makeText(this, "Call Emergency!", Toast.LENGTH_SHORT).show()
                // Send notification to caretaker
//                getCaretakerDeviceToken()
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


    // **** For sending notification ****
    private fun getCaretakerDeviceToken() {
        FirebaseDatabase.getInstance().getReference("Patients").child(firebaseAuth.uid!!).child("caretakerToken")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val caretakerToken = snapshot.value.toString()
                    sendNotification(caretakerToken)
                    makeItRing(caretakerToken)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("dbError", error.message)
                }

            })
    }

    private fun sendNotification(caretakerToken: String) {
        val title = "Emergency"
        val body = "Patient Fell Down"
        val notificationBody = JSONObject().apply {
            put("to", caretakerToken)
            put("notification", JSONObject().apply {
                put("title", title)
                put("body", body)
            })
        }.toString()

        val requestBody = notificationBody.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .header("Authorization", "key=$serverKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {

            }

        })
    }

    private fun makeItRing(caretakerToken: String) {
        val additionalData = JSONObject()
        additionalData.put("call_action", "initiate_call")
        additionalData.put("phone_number", "8794451269")
        additionalData.put("start_ringing", "true")

        val notificationBody = JSONObject().apply {
            put("to", caretakerToken)
            put("data", additionalData)
        }.toString()

        val requestBody = notificationBody.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .header("Authorization", "key=$serverKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {

            }

        })
    }

    private fun setPatientOnlineStatus(value : Boolean) {
        if(firebaseAuth.currentUser != null) {
            FirebaseDatabase.getInstance().getReference("Patients")
                .child(firebaseAuth.uid!!).child("onlineStatus").setValue(value.toString())
        }
    }

    // Check if patient has a caretaker. If not, show dialog
    private fun checkPatientsCaretaker() {
        // check caretakerUid is set or not in O(1) time
        FirebaseDatabase.getInstance().getReference("Patients")
            .child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val caretakerUid = "${snapshot.child("caretakerUid").value}"
                    val caretakerEmail = "${snapshot.child("caretakerEmail").value}"
                    if(caretakerUid == "") {
                        showDialog("Please register your caretaker with email:\n\n$caretakerEmail")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("dbError", error.message)
                }

            })
    }

    // Show dialog
    private fun showDialog(message : String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.add_contact_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvAddContactMessage = dialog.findViewById<TextView>(R.id.tvAddContactMessage)
        tvAddContactMessage.text = message
        val btnDismiss = dialog.findViewById<Button>(R.id.btnDismiss)
        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}