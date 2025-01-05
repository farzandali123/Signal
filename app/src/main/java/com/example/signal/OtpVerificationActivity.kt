package com.example.signal
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OtpVerificationActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null
    private lateinit var otpInput: EditText
    private lateinit var verifyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Get verificationId passed from the previous activity
        verificationId = intent.getStringExtra("verificationId")

        otpInput = findViewById(R.id.otpInput)
        verifyButton = findViewById(R.id.verifyButton)

        // Enable the Verify button only when the OTP field is filled
        otpInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                verifyButton.isEnabled = s?.length == 6  // assuming OTP is 6 digits long
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Handle OTP verification
        verifyButton.setOnClickListener {
            val otp = otpInput.text.toString().trim()

            if (verificationId != null && otp.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Please enter a valid OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // If verification is successful, proceed to the next screen
                    val user = task.result?.user
                    Toast.makeText(this, "Phone number verified successfully!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ProfileSetupActivity::class.java)
                    startActivity(intent)
                } else {
                    // If verification fails, show error message
                    Toast.makeText(this, "Verification failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
