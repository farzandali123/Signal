package com.example.signal

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting the content view to your phone number XML layout
        setContentView(R.layout.phone_number_layout)


        val countryCodeSpinner: Spinner = findViewById(R.id.countryCodeSpinner)


        val countryCodes = arrayOf("+1", "+44", "+91", "+81", "+86", "+61")

        // Set up an ArrayAdapter
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,  // Default layout for spinner items
            countryCodes
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        countryCodeSpinner.adapter = adapter
    }
}
