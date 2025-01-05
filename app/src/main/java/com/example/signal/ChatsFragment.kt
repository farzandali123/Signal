package com.example.signal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatsFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var chatContainer: LinearLayout
    private lateinit var currentUserId: String
    private val CAMERA_REQUEST_CODE = 100

    // UI Elements
    private lateinit var searchOverlay: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var searchBackIcon: ImageView
    private lateinit var settingsIcon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        // Initialize Firebase Database and SQLite Helper
        database = FirebaseDatabase.getInstance().reference
        dbHelper = DatabaseHelper(requireContext())
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        // Initialize UI Elements
        chatContainer = view.findViewById(R.id.chatContainer)
        searchOverlay = view.findViewById(R.id.searchOverlay)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchBackIcon = view.findViewById(R.id.searchBackIcon)
        settingsIcon = view.findViewById(R.id.settingsIcon)
        val emptyStateContainer = view.findViewById<LinearLayout>(R.id.emptyStateContainer)
        val chatsScrollView = view.findViewById<ScrollView>(R.id.chatsScrollView)
        val fabNewChat = view.findViewById<FloatingActionButton>(R.id.fab_new_chat)
        val fabCamera = view.findViewById<FloatingActionButton>(R.id.fab_camera)
        val profileIcon = view.findViewById<ImageView>(R.id.profileIcon)
        val searchIcon = view.findViewById<ImageView>(R.id.searchicon)

        // Load user profile picture from Firebase
        loadUserProfilePictureFromFirebase(profileIcon)

        // Load chats from Firebase and SQLite
        loadChats(emptyStateContainer, chatsScrollView)

        // Floating Action Button for starting a new chat
        fabNewChat.setOnClickListener { openContactsFragment() }

        // Open Camera
        fabCamera.setOnClickListener { openCamera() }

        // Navigate to profile settings
        profileIcon.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, SettingsFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        // Handle search icon click
        searchIcon.setOnClickListener { showSearchOverlay() }

        // Handle search back button click
        searchBackIcon.setOnClickListener { hideSearchOverlay() }

        // Handle settings icon click
        settingsIcon.setOnClickListener {
            Toast.makeText(requireContext(), "Settings menu clicked!", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadUserProfilePictureFromFirebase(profileIcon: ImageView) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Fetch profile picture URI from Firebase Realtime Database
            database.child("users").child(userId).child("profile_image_uri")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val profileImageUri = snapshot.getValue(String::class.java)

                        // Load the profile image using Glide
                        if (!profileImageUri.isNullOrEmpty()) {
                            Glide.with(this@ChatsFragment)
                                .load(profileImageUri)
                                .placeholder(R.drawable.profile)
                                .error(R.drawable.profile)
                                .into(profileIcon)
                        } else {
                            profileIcon.setImageResource(R.drawable.profile)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Failed to load profile image.", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun loadChats(emptyStateContainer: LinearLayout, chatsScrollView: ScrollView) {
        // First, load chats from SQLite for offline support
        val localChats = dbHelper.getAllChats()
        if (localChats.isNotEmpty()) {
            chatContainer.removeAllViews()
            for (chat in localChats) {
                addChatView(chat.chatId, chat.otherUserName, chat.lastMessage)
            }
            updateEmptyStateVisibility(false, emptyStateContainer, chatsScrollView)
        }

        // Load chats from Firebase for real-time updates
        if (currentUserId.isNotEmpty()) {
            database.child("chats").orderByChild("participants/$currentUserId").equalTo(true)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        chatContainer.removeAllViews()
                        val chatList = mutableListOf<String>()

                        for (chatSnapshot in snapshot.children) {
                            val chatId = chatSnapshot.key ?: continue
                            val participants = chatSnapshot.child("participants").children
                            val otherUserId = participants.firstOrNull { it.key != currentUserId }?.key
                            val lastMessage = chatSnapshot.child("last_message").getValue(String::class.java) ?: "No messages yet."

                            if (otherUserId != null) {
                                database.child("users").child(otherUserId).get()
                                    .addOnSuccessListener { userSnapshot ->
                                        val otherUserName = userSnapshot.child("first_name").getValue(String::class.java) ?: "Unknown User"
                                        chatList.add(chatId)

                                        // Save chat locally in SQLite
                                        dbHelper.saveChatLocally(chatId, otherUserName, lastMessage)
                                        addChatView(chatId, otherUserName, lastMessage)
                                    }
                                    .addOnFailureListener {
                                        Log.e("ChatsFragment", "Error loading user data.")
                                        Toast.makeText(requireContext(), "Error loading user data.", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }

                        updateEmptyStateVisibility(chatList.isEmpty(), emptyStateContainer, chatsScrollView)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(requireContext(), "Failed to load chats: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun updateEmptyStateVisibility(
        isChatListEmpty: Boolean,
        emptyStateContainer: LinearLayout,
        chatsScrollView: ScrollView
    ) {
        if (isChatListEmpty) {
            emptyStateContainer.visibility = View.VISIBLE
            chatsScrollView.visibility = View.GONE
        } else {
            emptyStateContainer.visibility = View.GONE
            chatsScrollView.visibility = View.VISIBLE
        }
    }

    private fun addChatView(chatId: String, otherUserName: String, lastMessage: String) {
        val chatItem = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(20, 20, 20, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 16, 8)
            }
            isClickable = true
            isFocusable = true
        }

        val chatTextContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val chatName = TextView(requireContext()).apply {
            text = otherUserName
            setTextColor(resources.getColor(android.R.color.white, null))
            textSize = 18f
        }

        val chatMessage = TextView(requireContext()).apply {
            text = lastMessage
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            textSize = 14f
        }

        chatTextContainer.addView(chatName)
        chatTextContainer.addView(chatMessage)

        chatItem.addView(chatTextContainer)

        chatItem.setOnClickListener { openChatDetail(chatId, otherUserName) }

        chatContainer.addView(chatItem)
    }

    private fun openChatDetail(chatId: String, otherUserName: String) {
        val fragment = ChatDetailFragment()
        val args = Bundle()
        args.putString("chat_id", chatId)
        args.putString("chat_name", otherUserName)
        fragment.arguments = args

        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun openContactsFragment() {
        val fragment = ContactsFragment()
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    private fun showSearchOverlay() {
        searchOverlay.visibility = View.VISIBLE
    }

    private fun hideSearchOverlay() {
        searchOverlay.visibility = View.GONE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar(true) // Ensures the navbar is shown
        val emptyStateContainer = view?.findViewById<LinearLayout>(R.id.emptyStateContainer)
        val chatsScrollView = view?.findViewById<ScrollView>(R.id.chatsScrollView)
        if (emptyStateContainer != null && chatsScrollView != null) {
            loadChats(emptyStateContainer, chatsScrollView)
        }
    }
}
