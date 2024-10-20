package com.example.signal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.ComponentActivity

class PhoneNumberActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_number_layout)

        val continueButton: Button = findViewById(R.id.continueButton)
        val phoneNumberInput: EditText = findViewById(R.id.phoneNumberInput)

        // Enable the Continue button when the user enters their phone number
        phoneNumberInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Enable the button if there is text
                continueButton.isEnabled = s?.isNotEmpty() == true
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // When Continue is clicked, navigate to Profile Setup
        continueButton.setOnClickListener {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            startActivity(intent)
        }

        // Spinner setup (this was already included in your code)
        val countryCodeSpinner: Spinner = findViewById(R.id.countryCodeSpinner)
        val countryCodes = arrayOf("+1", "+44", "+91", "+81", "+86", "+61")

        // Set up an ArrayAdapter
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            countryCodes
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countryCodeSpinner.adapter = adapter
    }
}
