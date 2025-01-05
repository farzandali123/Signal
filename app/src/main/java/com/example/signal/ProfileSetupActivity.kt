package com.example.signal

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileSetupActivity : Activity() {

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 101
        private const val PROFILE_PREFERENCES = "profile_prefs"
        private const val PROFILE_IMAGE_URI = "profile_image_uri"
    }

    private lateinit var profileIcon: ImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // Initialize Firebase references
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Find views
        val firstNameInput: EditText = findViewById(R.id.first_name_input)
        val lastNameInput: EditText = findViewById(R.id.last_name_input)
        val nextButton: Button = findViewById(R.id.next_button)
        profileIcon = findViewById(R.id.profileIcon)
        cameraIcon = findViewById(R.id.cameraIcon)

        // Set click listeners for camera and gallery
        cameraIcon.setOnClickListener { openCamera() }
        profileIcon.setOnClickListener { openGallery() }

        // Enable the next button if the user has entered their first name
        firstNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                nextButton.isEnabled = s?.isNotEmpty() == true
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Handle next button click
        nextButton.setOnClickListener {
            val firstName = firstNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val sharedPref = getSharedPreferences(PROFILE_PREFERENCES, Context.MODE_PRIVATE)
            val profileImageUri = sharedPref.getString(PROFILE_IMAGE_URI, null)

            if (firstName.isNotBlank() && lastName.isNotBlank()) {
                saveProfileToFirebase(firstName, lastName, profileImageUri)
            } else {
                Toast.makeText(this, "Please enter both first and last names.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Save user profile information to Firebase.
     */
    private fun saveProfileToFirebase(firstName: String, lastName: String, profileImageUri: String?) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val profileData = mutableMapOf<String, Any>(
            "first_name" to firstName,
            "last_name" to lastName
        )
        if (profileImageUri != null) {
            profileData["profile_image_uri"] = profileImageUri
        }

        database.child("users").child(currentUserId).updateChildren(profileData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                navigateToCreatePinActivity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Navigate to the CreatePinActivity.
     */
    private fun navigateToCreatePinActivity() {
        val intent = Intent(this, CreatePinActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Open the camera to capture a profile picture.
     */
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    /**
     * Open the gallery to select a profile picture.
     */
    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), GALLERY_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    photo?.let {
                        profileIcon.setImageBitmap(it)
                        profileIcon.scaleType = ImageView.ScaleType.CENTER_CROP
                        saveBitmapToPreferences(it)
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val imageUri: Uri? = data?.data
                    imageUri?.let {
                        profileIcon.setImageURI(it)
                        profileIcon.scaleType = ImageView.ScaleType.CENTER_CROP
                        saveImageUriToPreferences(it)
                    }
                }
            }
        }
    }

    private fun saveBitmapToPreferences(bitmap: Bitmap) {
        try {
            val file = File(cacheDir, "profile_image.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            saveImageUriToPreferences(Uri.fromFile(file))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveImageUriToPreferences(uri: Uri) {
        val sharedPref = getSharedPreferences(PROFILE_PREFERENCES, Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString(PROFILE_IMAGE_URI, uri.toString())
            apply()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
            GALLERY_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
