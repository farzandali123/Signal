package com.example.signal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity

class CreatePinActivity : ComponentActivity() {

    private lateinit var pinInput: EditText
    private lateinit var nextButton: Button
    private lateinit var alphanumericPinText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_pin)

        // Initialize views
        pinInput = findViewById(R.id.pin_input)
        nextButton = findViewById(R.id.next_button)
        alphanumericPinText = findViewById(R.id.create_alphanumeric_pin_link)

        // Add text listener to enable the "Next" button after entering 4 digits
        pinInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Enable the button only when 4 or more characters are entered
                nextButton.isEnabled = s?.length == 4
            }
        })

        // Add click listener to the "Next" button
        nextButton.setOnClickListener {
            // Navigate to MainActivity (Chat layout) when PIN is created
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Optionally finish this activity to prevent going back
        }
    }
}
