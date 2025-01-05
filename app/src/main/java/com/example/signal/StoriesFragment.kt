package com.example.signal

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap

class StoriesFragment : Fragment() {

    private lateinit var storiesRecyclerView: RecyclerView
    private lateinit var statusAdapter: UserStatusAdapter
    private val userStatusList = mutableListOf<UserStatus>()

    companion object {
        const val REQUEST_CODE_PICK_IMAGE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stories, container, false)

        // Initialize RecyclerView
        storiesRecyclerView = view.findViewById(R.id.storiesRecyclerView)
        storiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        statusAdapter = UserStatusAdapter(userStatusList) { userStatus ->
            openStatusViewer(userStatus)
        }
        storiesRecyclerView.adapter = statusAdapter

        // Set click listener for adding a new status
        val myStoriesIcon = view.findViewById<View>(R.id.myStoriesIcon)
        myStoriesIcon.setOnClickListener {
            openGallery()
        }

        // Fetch statuses from Firebase
        fetchStatuses()

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                uploadStatus(imageUri)
            } else {
                Toast.makeText(requireContext(), "Image selection failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadStatus(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown"

        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert image to Base64
        val base64Image = convertImageToBase64(imageUri)
        if (base64Image == null) {
            Toast.makeText(requireContext(), "Failed to process image.", Toast.LENGTH_SHORT).show()
            return
        }

        // Save Base64 image string to Realtime Database
        saveStatusToDatabase(userId, userName, base64Image)
    }

    private fun convertImageToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("StoriesFragment", "Failed to convert image to Base64: ${e.message}")
            null
        }
    }

    private fun saveStatusToDatabase(userId: String, userName: String, base64Image: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("statuses").child(userId)
        val statusId = databaseRef.child("statuses").push().key ?: return

        val status = Status(base64Image, System.currentTimeMillis(), userName)

        databaseRef.child("userName").setValue(userName) // Save userName at the user root level
        databaseRef.child("statuses").child(statusId).setValue(status) // Save the individual status
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Status uploaded successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("StoriesFragment", "Failed to save status: ${exception.message}")
                Toast.makeText(requireContext(), "Failed to save status.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchStatuses() {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("statuses")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userStatusList.clear()
                for (userSnapshot in snapshot.children) {
                    // Get the userName from the parent node
                    val userName = userSnapshot.child("userName").getValue(String::class.java) ?: "Unknown"
                    // Parse all statuses for this user
                    val statuses = userSnapshot.child("statuses").children.mapNotNull { statusSnapshot ->
                        val imageUrl = statusSnapshot.child("imageUrl").getValue(String::class.java) ?: return@mapNotNull null
                        val timestamp = statusSnapshot.child("timestamp").getValue(Long::class.java) ?: return@mapNotNull null
                        Status(imageUrl, timestamp, userName)
                    }
                    if (statuses.isNotEmpty()) {
                        userStatusList.add(UserStatus(userName, statuses))
                    }
                }
                statusAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StoriesFragment", "Failed to fetch statuses: ${error.message}")
                Toast.makeText(requireContext(), "Failed to load statuses.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openStatusViewer(userStatus: UserStatus) {
        if (userStatus.statuses.isNotEmpty()) {
            StatusCache.currentStatuses = userStatus.statuses
            val intent = Intent(requireContext(), StatusViewerActivity::class.java).apply {
                putExtra("userName", userStatus.userName)
            }
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "No statuses available to view.", Toast.LENGTH_SHORT).show()
        }
    }
}
