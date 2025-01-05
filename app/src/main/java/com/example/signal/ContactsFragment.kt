package com.example.signal

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ContactsFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var contactsContainer: LinearLayout
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        contactsContainer = view.findViewById(R.id.contactsContainer)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().reference
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        if (currentUserId.isEmpty()) {
            Toast.makeText(requireContext(), "User is not authenticated.", Toast.LENGTH_SHORT).show()
            Log.e("ContactsFragment", "Current user ID is null or empty.")
            return view
        }

        // Load contacts from Firebase
        loadContacts()

        return view
    }

    private fun loadContacts() {
        if (currentUserId.isEmpty()) {
            Log.e("ContactsFragment", "Cannot load contacts: Current user ID is empty.")
            return
        }

        // Fetch users from Firebase
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contactsContainer.removeAllViews() // Clear container before adding new data

                if (snapshot.exists()) {
                    snapshot.children.forEach { userSnapshot ->
                        val userId = userSnapshot.key
                        val firstName = userSnapshot.child("first_name").value?.toString() ?: "Unknown"
                        val lastName = userSnapshot.child("last_name").value?.toString() ?: "User"

                        // Skip adding current user to the contacts list
                        if (userId == currentUserId) return@forEach

                        if (!userId.isNullOrEmpty()) {
                            val contactView = TextView(requireContext()).apply {
                                text = "$firstName $lastName"
                                textSize = 18f
                                setPadding(16, 16, 16, 16)
                                setOnClickListener { startChat(userId, "$firstName $lastName") }
                            }
                            contactsContainer.addView(contactView)
                        } else {
                            Log.e("ContactsFragment", "Skipped adding a user with null/empty ID.")
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "No contacts found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load contacts: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ContactsFragment", "Error loading contacts: ${error.message}")
            }
        })
    }

    private fun startChat(otherUserId: String, otherUserName: String) {
        if (currentUserId.isEmpty()) {
            Log.e("ContactsFragment", "Cannot start chat: Current user ID is empty.")
            Toast.makeText(requireContext(), "User is not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("chats").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var existingChatId: String? = null

                for (chatSnapshot in snapshot.children) {
                    val participants = chatSnapshot.child("participants").value as? List<*>
                    if (participants != null && participants.contains(currentUserId) && participants.contains(otherUserId)) {
                        existingChatId = chatSnapshot.key
                        break
                    }
                }

                if (existingChatId != null) {
                    navigateToChatDetail(existingChatId, otherUserName)
                } else {
                    createNewChat(otherUserId, otherUserName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to check chats: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ContactsFragment", "Error checking chats: ${error.message}")
            }
        })
    }


    private fun createNewChat(otherUserId: String, otherUserName: String) {
        val newChatId = database.child("chats").push().key
        if (newChatId == null) {
            Toast.makeText(requireContext(), "Error: Could not create chat ID.", Toast.LENGTH_SHORT).show()
            Log.e("ContactsFragment", "Failed to generate a new chat ID.")
            return
        }

        // Store participants as a simple map
        val chatData = mapOf(
            "participants" to listOf(currentUserId, otherUserId),
            "last_message" to "No messages yet." // Optional field to store last message
        )

        database.child("chats").child(newChatId)
            .setValue(chatData)
            .addOnSuccessListener {
                navigateToChatDetail(newChatId, otherUserName)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to start chat: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("ContactsFragment", "Failed to create new chat: ${exception.message}")
            }
    }


    private fun navigateToChatDetail(chatId: String, otherUserName: String) {
        val fragment = ChatDetailFragment()
        val args = Bundle().apply {
            putString("chat_id", chatId)
            putString("chat_name", otherUserName)
        }
        fragment.arguments = args

        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }
}
