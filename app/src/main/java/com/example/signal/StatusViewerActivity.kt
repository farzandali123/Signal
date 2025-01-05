package com.example.signal

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.text.format.DateFormat

class StatusViewerActivity : AppCompatActivity() {



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_status_viewer)

            val statusImageView: ImageView = findViewById(R.id.statusImageView)
            val userNameTextView: TextView = findViewById(R.id.userNameTextView)
            val timestampTextView: TextView = findViewById(R.id.timestampTextView)

            // Get username from intent
            val userName = intent.getStringExtra("userName") ?: "Unknown"
            userNameTextView.text = userName

            // Get statuses from cache
            val statuses = StatusCache.currentStatuses

            if (statuses.isNullOrEmpty()) {
                timestampTextView.text = "No statuses available."
                return
            }

            // Display the first status
            val status = statuses.first()
            try {
                val imageBytes = Base64.decode(status.imageUrl, Base64.DEFAULT)
                if (imageBytes.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    statusImageView.setImageBitmap(bitmap)
                    timestampTextView.text = DateFormat.format("dd-MM-yyyy hh:mm:ss", status.timestamp)
                } else {
                    statusImageView.setImageResource(android.R.color.transparent)
                    timestampTextView.text = "Failed to load status image."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                statusImageView.setImageResource(android.R.color.transparent)
                timestampTextView.text = "Error loading image."
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            // Clear cached statuses to free memory
            StatusCache.currentStatuses = null
        }
    }


