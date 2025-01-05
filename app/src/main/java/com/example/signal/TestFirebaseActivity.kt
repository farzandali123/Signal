package com.example.signal

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TestFirebaseActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var saveButton: Button
    private lateinit var retrievedData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_firebase)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Initialize UI components
        nameInput = findViewById(R.id.name_input)
        emailInput = findViewById(R.id.email_input)
        saveButton = findViewById(R.id.save_button)
        retrievedData = findViewById(R.id.retrieved_data)

        // Save data to Firebase when the button is clicked
        saveButton.setOnClickListener {
            val name = nameInput.text.toString()
            val email = emailInput.text.toString()

            if (name.isNotBlank() && email.isNotBlank()) {
                saveDataToFirebase(name, email)
            } else {
                Toast.makeText(this, "Please enter both name and email", Toast.LENGTH_SHORT).show()
            }
        }

        // Read data from Firebase on activity start
        readDataFromFirebase()
    }

    private fun saveDataToFirebase(name: String, email: String) {
        val userId = database.push().key ?: return // Generate a unique key for each entry
        val user = User(name, email)

        // Save data under 'users' node with the generated userId
        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
                clearInputs() // Clear input fields after successful save
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun readDataFromFirebase() {
        database.child("users").get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.children.firstOrNull()?.getValue(User::class.java)
                    if (user != null) {
                        retrievedData.text = "Name: ${user.name}\nEmail: ${user.email}"
                    }
                } else {
                    retrievedData.text = "No data available"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to retrieve data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputs() {
        nameInput.text.clear()
        emailInput.text.clear()
    }

    // Data model for user
    data class User(val name: String, val email: String)
}
