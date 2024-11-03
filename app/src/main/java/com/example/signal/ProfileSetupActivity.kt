package com.example.signal

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileSetupActivity : ComponentActivity() {

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 101
        private const val PROFILE_PREFERENCES = "profile_prefs"
        private const val PROFILE_IMAGE_URI = "profile_image_uri"
    }

    private lateinit var profileIcon: ImageView
    private lateinit var cameraIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // Find views
        val firstNameInput: EditText = findViewById(R.id.first_name_input)
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

        // Move to the Create PIN page when Next is clicked
        nextButton.setOnClickListener {
            val intent = Intent(this, CreatePinActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

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
}
