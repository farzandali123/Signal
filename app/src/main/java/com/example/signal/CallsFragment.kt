package com.example.signal

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CallsFragment : Fragment() {

    private val CALL_PHONE_REQUEST_CODE = 101
    private val READ_CONTACTS_REQUEST_CODE = 102
    private val PICK_CONTACT_REQUEST_CODE = 103

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calls, container, false)

        // UI Elements
        val fabCall = view.findViewById<FloatingActionButton>(R.id.fab_call)
        val searchOverlay = view.findViewById<LinearLayout>(R.id.searchOverlay)
        val searchIcon = view.findViewById<ImageView>(R.id.searchicon)
        val searchBackIcon = view.findViewById<ImageView>(R.id.searchBackIcon)
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        val settingsIcon = view.findViewById<ImageView>(R.id.settingsIcon)
        val profileIcon = view.findViewById<ImageView>(R.id.profileIcon)

        // Set up FAB to initiate a call
        fabCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                openContactPicker()
            } else {
                requestContactsPermission()
            }
        }

        // Show search overlay when search icon is clicked
        searchIcon.setOnClickListener {
            searchOverlay.visibility = View.VISIBLE
            searchEditText.requestFocus()
        }

        // Hide search overlay when back icon is clicked
        searchBackIcon.setOnClickListener {
            searchOverlay.visibility = View.GONE
            searchEditText.text.clear()
        }

        // Handle settings icon click
        settingsIcon.setOnClickListener { showPopupMenu(it) }

        // Navigate to profile settings
        profileIcon.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, SettingsFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar(true)
    }

    override fun onStop() {
        super.onStop()
        (activity as? MainActivity)?.showNavBar(false)
    }

    // Request the READ_CONTACTS permission
    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_CONTACTS),
            READ_CONTACTS_REQUEST_CODE
        )
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_CONTACTS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker()
            } else {
                Toast.makeText(requireContext(), "Permission denied to read contacts.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Open Contact Picker
    private fun openContactPicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            startActivityForResult(intent, PICK_CONTACT_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e("CallsFragment", "Error opening contact picker: ${e.message}")
            Toast.makeText(requireContext(), "Unable to open contact picker.", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle Contact Picker Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val contactUri = data.data
            if (contactUri != null) {
                val phoneNumber = getPhoneNumberFromContactUri(contactUri)
                if (!phoneNumber.isNullOrEmpty()) {
                    initiateCall(phoneNumber)
                } else {
                    Log.e("CallsFragment", "No phone number found for contact.")
                    Toast.makeText(requireContext(), "No phone number found.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("CallsFragment", "Contact URI is null.")
                Toast.makeText(requireContext(), "No contact selected.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Retrieve Phone Number from Contact URI
    private fun getPhoneNumberFromContactUri(contactUri: Uri): String? {
        try {
            val contentResolver = requireContext().contentResolver
            val cursor: Cursor? = contentResolver.query(
                contactUri,
                null, // Get all columns
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhoneNumber = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    if (hasPhoneNumber > 0) {
                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )
                        phoneCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                return pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CallsFragment", "Error retrieving phone number: ${e.message}")
        }
        return null
    }

    // Initiate a Call
    private fun initiateCall(phoneNumber: String) {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:$phoneNumber")
                startActivity(callIntent)
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CALL_PHONE),
                    CALL_PHONE_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
            Log.e("CallsFragment", "Error initiating call: ${e.message}")
            Toast.makeText(requireContext(), "Failed to initiate call.", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to display a popup menu
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.calls_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_missed_calls -> true
                R.id.settings -> true
                R.id.notification_profile -> true
                else -> false
            }
        }
        popup.show()
    }
}
