package com.example.signal

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ChatDetailFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var chatId: String
    private lateinit var messageContainer: LinearLayout
    private lateinit var dbHelper: DatabaseHelper
    private val IMAGE_PICK_CODE = 101
    private val CONTACT_PICK_CODE = 102
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat_detail, container, false)

        // Initialize Firebase Database and SQLite
        database = FirebaseDatabase.getInstance().reference
        dbHelper = DatabaseHelper(requireContext())
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        // Retrieve chat ID and name
        chatId = arguments?.getString("chat_id") ?: ""
        val chatName = arguments?.getString("chat_name") ?: "Chat"
        val chatTitle = view.findViewById<TextView>(R.id.chatTitle)
        chatTitle.text = chatName

        // UI Elements
        messageContainer = view.findViewById(R.id.messageContainer)
        val messageInput = view.findViewById<EditText>(R.id.messageInput)
        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val imageIcon = view.findViewById<ImageView>(R.id.imageIcon)
        val phoneIcon = view.findViewById<ImageView>(R.id.callIcon)

        // Load messages
        loadMessages()

        // Send text message
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageInput.text.clear()
            }
        }

        // Open gallery to select an image
        imageIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        // Open contacts to select a contact
        phoneIcon.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(intent, CONTACT_PICK_CODE)
        }

        return view
    }

    private fun loadMessages() {
        // Load messages from Firebase
        database.child("chats").child(chatId).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageContainer.removeAllViews()
                    for (messageSnapshot in snapshot.children) {
                        val sender = messageSnapshot.child("sender").value.toString()
                        val text = messageSnapshot.child("text").getValue(String::class.java)
                        val imageUrl = messageSnapshot.child("image_url").getValue(String::class.java)
                        val timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                        if (!text.isNullOrEmpty()) {
                            addMessageView(sender, text)
                        } else if (!imageUrl.isNullOrEmpty()) {
                            addImageMessageView(sender, imageUrl)
                        }

                        // Save message locally
                        dbHelper.saveMessageLocally(chatId, text, sender, imageUrl, timestamp)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatDetailFragment", "Failed to load messages: ${error.message}")
                    Toast.makeText(context, "Failed to load messages.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sendMessage(messageText: String) {
        val messagesRef = database.child("chats").child(chatId).child("messages")
        val messageId = messagesRef.push().key ?: return
        val messageData = mapOf(
            "sender" to currentUserId,
            "text" to messageText,
            "timestamp" to System.currentTimeMillis()
        )
        messagesRef.child(messageId).setValue(messageData)
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendImageMessage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("chat_images")
        val imageRef = storageRef.child("${chatId}_${System.currentTimeMillis()}.jpg")
        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val messagesRef = database.child("chats").child(chatId).child("messages")
                    val messageId = messagesRef.push().key ?: return@addOnSuccessListener
                    val messageData = mapOf(
                        "sender" to currentUserId,
                        "image_url" to downloadUri.toString(),
                        "timestamp" to System.currentTimeMillis()
                    )
                    messagesRef.child(messageId).setValue(messageData)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to send image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addMessageView(senderId: String, messageText: String) {
        val isCurrentUser = senderId == currentUserId
        val messageView = TextView(requireContext()).apply {
            text = messageText
            textSize = 16f
            setPadding(8, 8, 8, 8)
            if (isCurrentUser) {
                setBackgroundResource(R.drawable.outgoing_message_background)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(48, 8, 16, 8) }
            } else {
                setBackgroundResource(R.drawable.incoming_message_background)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(16, 8, 48, 8) }
            }
        }
        messageContainer.addView(messageView)
    }

    private fun addImageMessageView(senderId: String, imageUrl: String) {
        val isCurrentUser = senderId == currentUserId
        val imageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                500,
                500
            ).apply {
                setMargins(if (isCurrentUser) 48 else 16, 8, if (isCurrentUser) 16 else 48, 8)
            }
            Glide.with(this).load(imageUrl).into(this)
        }
        messageContainer.addView(imageView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            IMAGE_PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val imageUri = data.data
                    if (imageUri != null) {
                        sendImageMessage(imageUri)
                    }
                }
            }
            CONTACT_PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val contactUri = data.data ?: return
                    val cursor: Cursor? = requireContext().contentResolver.query(
                        contactUri,
                        null, null, null, null
                    )
                    cursor?.apply {
                        if (moveToFirst()) {
                            val contactName = getString(getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                            val hasPhoneNumber =
                                getInt(getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0
                            if (hasPhoneNumber) {
                                val contactId = getString(getColumnIndex(ContactsContract.Contacts._ID))
                                val phoneCursor = requireContext().contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                    arrayOf(contactId),
                                    null
                                )
                                phoneCursor?.apply {
                                    if (moveToFirst()) {
                                        val phoneNumber =
                                            getString(getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                        makeCall(phoneNumber)
                                    }
                                    close()
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Selected contact does not have a phone number",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        close()
                    }
                }
            }
        }
    }

    private fun makeCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$phoneNumber")
            startActivity(callIntent)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CALL_PHONE),
                1
            )
        }
    }
}
