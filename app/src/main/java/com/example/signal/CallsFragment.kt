package com.example.signal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.LinearLayout

class CallsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calls, container, false)
        val searchOverlay = view.findViewById<LinearLayout>(R.id.searchOverlay)
        val searchIcon = view.findViewById<ImageView>(R.id.searchicon)
        val searchBackIcon = view.findViewById<ImageView>(R.id.searchBackIcon)
        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)

        // Find the settings (three dots) icon
        val settingsIcon = view.findViewById<ImageView>(R.id.settingsIcon)
        val profileIcon = view.findViewById<ImageView>(R.id.profileIcon)

        // Set an OnClickListener to show the popup menu when clicked on the three dots icon
        settingsIcon.setOnClickListener {
            showPopupMenu(it)
        }
        // Show search overlay when search icon is clicked
        searchIcon.setOnClickListener {
            searchOverlay.visibility = View.VISIBLE
            searchEditText.requestFocus()
        }

// Hide search overlay when back icon is clicked
        searchBackIcon.setOnClickListener {
            searchOverlay.visibility = View.GONE
            searchEditText.setText("") // Clear search text if needed
        }


        // Set an OnClickListener to navigate to Settings when the profile icon is clicked
        profileIcon.setOnClickListener {
            // Open the SettingsFragment
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, SettingsFragment())  // Adjust the container ID as necessary
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

    // Method to display a popup menu
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.calls_menu, popup.menu) // Reference to menu_calls.xml
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.filter_missed_calls -> {
                    // Handle filter missed calls action
                    true
                }
                R.id.settings -> {
                    // Handle settings action
                    true
                }
                R.id.notification_profile -> {
                    // Handle notification profile action
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
