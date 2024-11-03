package com.example.signal

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.content.Context

class StoriesFragment : Fragment() {

    private lateinit var profileIcon: ImageView
    private lateinit var myStoriesIcon: ImageView
    private lateinit var searchOverlay: LinearLayout
    private lateinit var searchEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stories, container, false)

        try {
            searchOverlay = view.findViewById(R.id.searchOverlay)
            val searchIcon = view.findViewById<ImageView>(R.id.searchicon)
            val searchBackIcon = view.findViewById<ImageView>(R.id.searchBackIcon)
            searchEditText = view.findViewById(R.id.searchEditText)

            val settingsIcon = view.findViewById<ImageView>(R.id.settingsIcon)
            profileIcon = view.findViewById(R.id.profileIcon)
            myStoriesIcon = view.findViewById(R.id.myStoriesIcon)

            // Set up listeners
            settingsIcon?.setOnClickListener { showPopupMenu(it) }

            // Open the search overlay when the search icon is clicked
            searchIcon?.setOnClickListener {
                searchOverlay.visibility = View.VISIBLE
                searchEditText.requestFocus()
                showKeyboard()
            }

            // Hide the search overlay when the back icon is clicked
            searchBackIcon?.setOnClickListener {
                searchOverlay.visibility = View.GONE
                searchEditText.setText("") // Clear the search text if needed
                hideKeyboard()
            }

            // Set up click listener only for the bottom profile icon (myStoriesIcon)
            myStoriesIcon?.setOnClickListener {
                showImagePickerDialog()
            }

        } catch (e: Exception) {
            Log.e("StoriesFragment", "Error setting up views in StoriesFragment", e)
        }

        return view
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        AlertDialog.Builder(requireContext())
            .setTitle("Update Status")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            myStoriesIcon.setImageURI(imageUri)  // Display the captured image on myStoriesIcon
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            myStoriesIcon.setImageURI(imageUri)  // Display the selected image on myStoriesIcon
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar(true)
    }

    override fun onStop() {
        super.onStop()
        (activity as? MainActivity)?.showNavBar(false)
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_stories, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_stories -> {
                    // Handle filter stories action
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 101
    }
}
