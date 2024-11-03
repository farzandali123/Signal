package com.example.signal

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class StatusViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_view)

        val statusImageView: ImageView = findViewById(R.id.statusImageView)

        // Get the URI passed from StoriesFragment
        val statusUri = intent.getParcelableExtra<Uri>("STATUS_URI")
        statusImageView.setImageURI(statusUri)
    }
}
