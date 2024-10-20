package com.example.signal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.fragment.app.Fragment

class ChatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        // Find the profile icon and set an OnClickListener to navigate to Settings
        val profileIcon = view.findViewById<ImageView>(R.id.profileIcon)
        profileIcon.setOnClickListener {
            // Open the SettingsFragment
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, SettingsFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        // Find the three dots icon (menuIcon) and set an OnClickListener to show the PopupMenu
        val menuIcon = view.findViewById<ImageView>(R.id.menuIcon)
        menuIcon.setOnClickListener {
            showPopupMenu(it) // Call function to show the menu
        }

        return view
    }

    // Function to show PopupMenu
    private fun showPopupMenu(view: View) {
        // Create a new PopupMenu
        val popupMenu = PopupMenu(requireContext(), view)
        // Inflate the popup menu from a menu resource
        popupMenu.inflate(R.menu.menu_overflow) // This is the menu file you will create

        // Set a click listener for menu items
        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.new_group -> {
                    // Handle New Group click
                    true
                }
                R.id.mark_all_read -> {
                    // Handle Mark All Read click
                    true
                }
                R.id.invite_friends -> {
                    // Handle Invite Friends click
                    true
                }
                R.id.filter_unread_chats -> {
                    // Handle Filter Unread Chats click
                    true
                }
                R.id.settings -> {
                    // Handle Settings click
                    true
                }
                R.id.notification_profile -> {
                    // Handle Notification Profile click
                    true
                }
                else -> false
            }
        }

        // Show the popup menu
        popupMenu.show()
    }
}
