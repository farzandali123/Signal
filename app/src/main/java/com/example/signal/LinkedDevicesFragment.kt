package com.example.signal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class LinkedDevicesFragment : Fragment(R.layout.fragment_linked_devices) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the back button and set click listener
        val backButton = view.findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}
