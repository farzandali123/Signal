package com.example.signal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class NotificationsFragment : Fragment(R.layout.fragment_notfications) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle back button click
        val backButton = view.findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Initialize other UI components if needed
    }
}
