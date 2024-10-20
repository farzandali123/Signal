package com.example.signal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment

class StoriesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_stories, container, false)

        // Find the settings (three dots) icon
        val settingsIcon = view.findViewById<ImageView>(R.id.settingsIcon)
        val profileIcon = view.findViewById<ImageView>(R.id.profileIcon)

        // Set an OnClickListener to show the popup menu when clicked on the three dots icon
        settingsIcon.setOnClickListener {
            showPopupMenu(it)
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

    // Method to display a popup menu
    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu_stories, popup.menu) // Reference to menu_stories.xml
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
}
