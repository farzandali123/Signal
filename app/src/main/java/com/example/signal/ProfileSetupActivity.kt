package com.example.signal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.ComponentActivity

class ProfileSetupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        // Find views
        val firstNameInput: EditText = findViewById(R.id.first_name_input)
        val lastNameInput: EditText = findViewById(R.id.last_name_input)
        val nextButton: Button = findViewById(R.id.next_button)

        // Enable the next button if the user has entered their first name
        firstNameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Enable the button if first name is not empty
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
}
