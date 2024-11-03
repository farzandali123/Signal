package com.example.signal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Switch
import androidx.fragment.app.Fragment

class AccountFragment : Fragment(R.layout.fragment_account) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle back button
        val backButton = view.findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Handle switches
        val pinRemindersSwitch = view.findViewById<Switch>(R.id.pinRemindersSwitch)
        val registrationLockSwitch = view.findViewById<Switch>(R.id.registrationLockSwitch)

        // Handle "Change your PIN" click
        val changePinTextView = view.findViewById<TextView>(R.id.changePinTextView)
        changePinTextView.setOnClickListener {
            // Start CreatePinActivity
            val intent = Intent(requireContext(), CreatePinActivity::class.java)
            startActivity(intent)
        }

        // Handle "Change phone number" click
        val changePhoneNumberTextView = view.findViewById<TextView>(R.id.changePhoneNumberTextView)
        changePhoneNumberTextView.setOnClickListener {
            // Start PhoneNumberActivity
            val intent = Intent(requireContext(), PhoneNumberActivity::class.java)
            startActivity(intent)
        }

        // Handle "Delete account" click
        val deleteAccountTextView = view.findViewById<TextView>(R.id.deleteAccountTextView)
        deleteAccountTextView.setOnClickListener {
            // Start PhoneNumberActivity (assuming it's the same activity for delete account)
            val intent = Intent(requireContext(), PhoneNumberActivity::class.java)
            startActivity(intent)
        }

        // Add any additional logic here
        pinRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle PIN reminders switch change
        }

        registrationLockSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Handle Registration Lock switch change
        }
    }
}
