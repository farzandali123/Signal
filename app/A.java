package com.example.signal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import java.util.concurrent.TimeUnit

class PhoneNumberActivity : ComponentActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_number_layout)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(this)

        val continueButton: Button = findViewById(R.id.continueButton)
        val phoneNumberInput: EditText = findViewById(R.id.phoneNumberInput)

        // Enable the Continue button when the user enters their phone number
        phoneNumberInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                continueButton.isEnabled = s?.isNotEmpty() == true
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // When Continue is clicked
        continueButton.setOnClickListener {
            val phoneNumber = phoneNumberInput.text.toString().trim()

            if (dbHelper.isPhoneNumberRegistered(phoneNumber)) {
                // Phone number already registered
                Toast.makeText(this, "Phone number is already registered!", Toast.LENGTH_SHORT).show()
            } else {
                // Register phone number and send OTP
                if (dbHelper.registerPhoneNumber(phoneNumber)) {
                    Toast.makeText(this, "Phone number registered successfully!", Toast.LENGTH_SHORT).show()
                    sendOtp(phoneNumber)
                } else {
                    Toast.makeText(this, "Error registering phone number.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Spinner setup
        val countryCodeSpinner: Spinner = findViewById(R.id.countryCodeSpinner)
        val countryCodes = arrayOf("+1", "+44", "+91", "+81", "+86", "+61")

        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            countryCodes
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countryCodeSpinner.adapter = adapter
    }

    // Function to send OTP
    private fun sendOtp(phoneNumber: String) {
        val countryCode = findViewById<Spinner>(R.id.countryCodeSpinner).selectedItem.toString()
        val fullPhoneNumber = "$countryCode$phoneNumber"  // Use selected country code

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(fullPhoneNumber)  // Phone number to send OTP
            .setTimeout(60L, TimeUnit.SECONDS)  // OTP expiry time
            .setActivity(this)  // Activity context
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Use FirebaseException for onVerificationFailed
                    Toast.makeText(this@PhoneNumberActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // OTP has been sent, pass the verificationId to OtpVerificationActivity
                    val intent = Intent(this@PhoneNumberActivity, OtpVerificationActivity::class.java)
                    intent.putExtra("verificationId", verificationId)
                    startActivity(intent)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Function to handle OTP sign-in (called from OtpVerificationActivity)
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Phone number successfully verified, proceed to Profile Setup
                    val user = task.result?.user
                    val intent = Intent(this, ProfileSetupActivity::class.java)
                    startActivity(intent)
                } else {
                    // Verification failed, show error message
                    Toast.makeText(this, "Verification failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
