package com.example.signal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle back button click
        val backButton = view.findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Inside SettingsFragment or where you handle the Account navigation
        val accountTextView = view.findViewById<TextView>(R.id.accountOption)
        accountTextView.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, AccountFragment())  // Adjust container ID as needed
                ?.addToBackStack(null)
                ?.commit()
        }

        val linkedDevicesOption = view.findViewById<TextView>(R.id.linkedDevicesOption)
        linkedDevicesOption.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, LinkedDevicesFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        // Handle "Donate to Signal" button click
        val DonateOption = view.findViewById<TextView>(R.id.donateOption)
        DonateOption.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, DonateFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        val chatsOption = view.findViewById<TextView>(R.id.chatsOption)
        chatsOption.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, CFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        val storiesOption = view.findViewById<TextView>(R.id.storiesOption)
        storiesOption.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, SFragment())
                ?.addToBackStack(null)
                ?.commit()

        }

        val notificationsOption = view.findViewById<TextView>(R.id.notificationsOption)
        notificationsOption.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, NotificationsFragment())
                ?.addToBackStack(null)
                ?.commit()
        }



        val privacyOption = view.findViewById<TextView>(R.id.privacyOption)
        privacyOption.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, PrivacyFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        // Navigate to AppearanceFragment on clicking Appearance option
        val appearanceOption = view.findViewById<TextView>(R.id.appearanceOption)
        appearanceOption.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, AppearanceFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        // Data and Storage settings option
        val dataStorageOption = view.findViewById<TextView>(R.id.dataAndStorageOption)
        dataStorageOption.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, DataStorageFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.showNavBar(false)
    }
}