package com.example.signal

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var profileImageView: ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userPhoneTextView: TextView
    private lateinit var firebaseDatabase: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImage)
        userNameTextView = view.findViewById(R.id.userName)
        userPhoneTextView = view.findViewById(R.id.userPhone)

        // Initialize Firebase Database reference
        firebaseDatabase = FirebaseDatabase.getInstance().reference

        // Fetch user profile from Firebase
        fetchProfileFromFirebase()

        // Set up navigation for settings options
        setUpNavigation(view)

        // Handle back button click
        val backButton = view.findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun fetchProfileFromFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Fetch profile details from Firebase Realtime Database
            firebaseDatabase.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val firstName = snapshot.child("first_name").getValue(String::class.java) ?: "Unknown"
                        val lastName = snapshot.child("last_name").getValue(String::class.java) ?: ""
                        val profileImageUrl = snapshot.child("profile_image_uri").getValue(String::class.java)
                        val phone = snapshot.child("phone").getValue(String::class.java) ?: "No Phone"

                        // Update UI
                        userNameTextView.text = "$firstName $lastName"
                        userPhoneTextView.text = phone

                        // Load profile image
                        if (!profileImageUrl.isNullOrEmpty()) {
                            try {
                                Glide.with(this@SettingsFragment)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile)
                                    .into(profileImageView)
                            } catch (e: Exception) {
                                Log.e("SettingsFragment", "Error loading profile image: ${e.message}")
                                profileImageView.setImageResource(R.drawable.profile)
                            }
                        } else {
                            profileImageView.setImageResource(R.drawable.profile)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SettingsFragment", "Failed to fetch profile from Firebase: ${error.message}")
                        Toast.makeText(requireContext(), "Failed to fetch profile.", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            Log.e("SettingsFragment", "No authenticated user found.")
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpNavigation(view: View) {
        view.findViewById<TextView>(R.id.accountOption).setOnClickListener {
            navigateTo(AccountFragment())
        }
        view.findViewById<TextView>(R.id.linkedDevicesOption).setOnClickListener {
            navigateTo(LinkedDevicesFragment())
        }
        view.findViewById<TextView>(R.id.donateOption).setOnClickListener {
            navigateTo(DonateFragment())
        }
        view.findViewById<TextView>(R.id.chatsOption).setOnClickListener {
            navigateTo(CFragment())
        }
        view.findViewById<TextView>(R.id.storiesOption).setOnClickListener {
            navigateTo(SFragment())
        }
        view.findViewById<TextView>(R.id.notificationsOption).setOnClickListener {
            navigateTo(NotificationsFragment())
        }
        view.findViewById<TextView>(R.id.privacyOption).setOnClickListener {
            navigateTo(PrivacyFragment())
        }
        view.findViewById<TextView>(R.id.appearanceOption).setOnClickListener {
            navigateTo(AppearanceFragment())
        }
        view.findViewById<TextView>(R.id.dataAndStorageOption).setOnClickListener {
            navigateTo(DataStorageFragment())
        }
    }

    private fun navigateTo(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar(false)
    }
}
