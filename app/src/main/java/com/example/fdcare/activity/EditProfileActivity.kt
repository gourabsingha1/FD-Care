package com.example.fdcare.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.fdcare.R
import com.example.fdcare.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var imageUri: Uri? = null

    private companion object {
        private const val TAG = "EDIT_PROFILE_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide action bar
        supportActionBar?.hide()

        // Get instance of firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Setup ProgressDialogue to show after updating profile
        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        // Go back
        binding.ivEditProfileBack.setOnClickListener {
            startActivity(Intent(this, HomePatientActivity::class.java))
            finish()
        }

        // Load Data
        loadMyData()

        binding.ivEditProfileProfilePic.setOnClickListener {
            imagePickDialog()
        }

        // Update profile
        binding.btnEditProfileUpdate.setOnClickListener {
            validateData()
        }
    }

    // load data on screen
    private fun loadMyData() {
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().getReference("Patients")
        ref.child(firebaseAuth.uid!!).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // get data
                val name = "${snapshot.child("name").value}"
                val email = "${snapshot.child("email").value}"
                val phoneCode = "${snapshot.child("phoneCode").value}"
                val phoneNumber = "${snapshot.child("phoneNumber").value}"
                val profileImageUrl = "${snapshot.child("profileImageUrl").value}"
                val onlineStatus = "${snapshot.child("onlineStatus").value}"
                val emergencyName = "${snapshot.child("emergencyName").value}"
                val emergencyEmail = "${snapshot.child("emergencyEmail").value}"
                val emergencyPhoneCode = "${snapshot.child("emergencyPhoneCode").value}"
                val emergencyPhoneNumber = "${snapshot.child("emergencyPhoneNumber").value}"

                // set data
                try {
                    Glide.with(this@EditProfileActivity)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.ivEditProfileProfilePic)
                } catch (e: Exception) {
                    Log.e(TAG, "onDataChange: ", e)
                }
                binding.etEditProfileName.setText(name)
                binding.etEditProfileEmail.setText(email)
                try {
                    val phoneCodeInt = phoneCode.replace("+", "").toInt()
                    binding.ccpEditProfile.setCountryForPhoneCode(phoneCodeInt)
                } catch (e: Exception) {
                    Log.e(TAG, "onDataChange: ", e)
                }
                binding.etEditProfilePhone.setText(phoneNumber)
                binding.etEditProfileEmergencyName.setText(emergencyName)
                binding.etEditProfileEmergencyEmail.setText(emergencyEmail)
                try {
                    val phoneCodeInt = emergencyPhoneCode.replace("+", "").toInt()
                    binding.ccpEditProfileEmergency.setCountryForPhoneCode(phoneCodeInt)
                } catch (e: Exception) {
                    Log.e(TAG, "onDataChange: ", e)
                }
                binding.etEditProfileEmergencyPhone.setText(emergencyPhoneNumber)

                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun imagePickDialog() {
        // Show popupmenu when clicked on imageView
        val popupMenu = PopupMenu(this, binding.ivEditProfileProfilePic)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    requestCameraPermissions.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery()
                } else {
                    requestStoragePermissions.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            true
        }
    }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var areAllGranted = true
            for (isGranted in result.values) {
                areAllGranted = areAllGranted && isGranted
            }
            if (areAllGranted) {
                pickImageCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera or Storage or both permissions denied",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun pickImageCamera() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")

        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultlauncher.launch(intent)
    }

    private val cameraActivityResultlauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.ivEditProfileProfilePic)
                } catch (e: Exception) {
                    Log.e(TAG, "cameraActivityResultlauncher: ", e)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }


    private val requestStoragePermissions =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickImageGallery()
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultlauncher.launch(intent)
    }

    private val galleryActivityResultlauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.ivEditProfileProfilePic)
                } catch (e: Exception) {
                    Log.e(TAG, "cameraActivityResultlauncher: ", e)
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            }
        }

    // variables to hold updated product data
    var name = ""
    var email = ""
    var phoneCode = ""
    var phoneNumber = ""
    var emergencyName = ""
    var emergencyEmail = ""
    var emergencyPhoneCode = ""
    var emergencyPhoneNumber = ""

    private fun validateData() {
        name = binding.etEditProfileName.text.toString().trim()
        email = binding.etEditProfileEmail.text.toString().trim()
        phoneCode = binding.ccpEditProfile.selectedCountryCodeWithPlus
        phoneNumber = binding.etEditProfilePhone.text.toString().trim()
        emergencyName = binding.etEditProfileEmergencyName.text.toString().trim()
        emergencyEmail = binding.etEditProfileEmergencyEmail.text.toString().trim()
        emergencyPhoneCode = binding.ccpEditProfileEmergency.selectedCountryCodeWithPlus
        emergencyPhoneNumber = binding.etEditProfileEmergencyPhone.text.toString().trim()

        if (imageUri == null) {
            updateProfileDb(null)
        } else {
            uploadProfileImageStorage()
        }
    }

    private fun uploadProfileImageStorage() {
        progressDialog.setMessage("Uploading")
        progressDialog.show()

        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"
        val ref = FirebaseStorage.getInstance().getReference(filePathAndName)
        ref.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // set imageUrl to firebase realtime
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    updateProfileDb(uploadedImageUrl)
                }
            }.addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
    }

    private fun updateProfileDb(uploadedImageUrl: String?) {
        progressDialog.setMessage("Updating patient info")
        progressDialog.show()

        val hashMap = HashMap<String, Any?>()
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["phoneCode"] = phoneCode
        hashMap["phoneNumber"] = phoneNumber
        hashMap["profileImageUrl"] = uploadedImageUrl
        hashMap["emergencyName"] = emergencyName
        hashMap["emergencyEmail"] = emergencyEmail
        hashMap["emergencyPhoneCode"] = emergencyPhoneCode
        hashMap["emergencyPhoneNumber"] = emergencyPhoneNumber

        progressDialog.dismiss()

        val reference = FirebaseDatabase.getInstance().getReference("Patients")
        reference.child("${firebaseAuth.uid}").updateChildren(hashMap).addOnSuccessListener {
            Toast.makeText(this, "Patient Profile Updated", Toast.LENGTH_LONG).show()
            imageUri = null
        }.addOnFailureListener { e ->
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }
}