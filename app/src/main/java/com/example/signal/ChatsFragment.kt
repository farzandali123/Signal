package com.example.signal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ChatsFragment : Fragment() {
    companion object {
        private const val PROFILE_PREFERENCES = "profile_prefs"
        private const val PROFILE_IMAGE_URI = "profile_image_uri"
    }

    private val CAMERA_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        // Camera FAB
        val fabCamera = view.findViewById<FloatingActionButton>(R.id.fab_camera)
        fabCamera.setOnClickListener {
            openCamera()
        }
        // Load the profile image from SharedPreferences


        // Other initialization code
        val searchOverlay = view.findViewById<LinearLayout>(R.id.searchOverlay)
        val searchIcon = view.findViewById<ImageView>(R.id.searchicon)
        val searchBackIcon = view.findViewById<ImageView>(R.id.searchBackIcon)
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        val profileIcon = view.findViewById<ImageView>(R.id.profileIcon)
        

        profileIcon.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, SettingsFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        // Show search overlay when search icon is clicked
        searchIcon.setOnClickListener {
            searchOverlay.visibility = View.VISIBLE
            searchEditText.requestFocus()
        }

        // Hide search overlay when back icon is clicked
        searchBackIcon.setOnClickListener {
            searchOverlay.visibility = View.GONE
            searchEditText.setText("")
        }

        val settingsIcon = view.findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            showPopupMenu(it)
        }

        val chatContainer = view.findViewById<LinearLayout>(R.id.chatContainer)
        addHardcodedChats(chatContainer)

        return view
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted, open the camera
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, open the camera
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            } else {
                // Permission denied
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.menu_overflow)

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.new_group -> true
                R.id.mark_all_read -> true
                R.id.invite_friends -> true
                R.id.filter_unread_chats -> true
                R.id.settings -> {
                    activity?.supportFragmentManager?.beginTransaction()
                        ?.replace(R.id.container, SettingsFragment())
                        ?.addToBackStack(null)
                        ?.commit()
                    true
                }
                R.id.notification_profile -> true
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun addHardcodedChats(chatContainer: LinearLayout) {
        val chats = listOf(
            Pair("Family", "Mom: Look at these old photos I found from back when we lived in Munich."),
            Pair("Paige Hall", "Yeah, I just got the group link - thanks for adding me 😎"),
            Pair("Rock climbers", "Ok, I’m picking everyone up at 8am tomorrow."),
            Pair("Maya Johnson", "View-once media"),
            Pair("Note to Self", "Groceries: Coffee, yogurt, grapefruit, biscuits"),
            Pair("Roommates", "Kai: Working late - can one of you please feed Spooky?"),
            Pair("Michael Kirk", "This brownie recipe is good beyond words.")
        )

        for ((name, message) in chats) {
            val chatItem = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 20, 20, 20)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                isClickable = true
                isFocusable = true
            }

            val profileImage = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                    marginEnd = 16
                }
                setImageResource(R.drawable.maleicon)
                contentDescription = "$name profile image"
                setPadding(8, 8, 8, 8)
            }

            val chatTextContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val chatName = TextView(requireContext()).apply {
                text = name
                setTextColor(resources.getColor(android.R.color.white, null))
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val chatMessage = TextView(requireContext()).apply {
                text = message
                setTextColor(resources.getColor(android.R.color.darker_gray, null))
                textSize = 14f
            }

            chatTextContainer.addView(chatName)
            chatTextContainer.addView(chatMessage)

            chatItem.addView(profileImage)
            chatItem.addView(chatTextContainer)

            chatItem.setOnClickListener {
                openChatDetail(name)
            }

            chatContainer.addView(chatItem)
        }
    }

    private fun openChatDetail(chatName: String) {
        val fragment = ChatDetailFragment()
        val args = Bundle()
        args.putString("chat_name", chatName)
        fragment.arguments = args

        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar(true)
    }

    override fun onStop() {
        super.onStop()
        (activity as? MainActivity)?.showNavBar(false)
    }
}
