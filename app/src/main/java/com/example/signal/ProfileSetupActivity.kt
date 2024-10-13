package com.example.signal

import android.os.Bundle
import androidx.activity.ComponentActivity

class ProfileSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting the content view to your profile setup XML layout
        setContentView(R.layout.activity_profile_setup)
    }
}
