package com.example.signal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class DataStorageFragment : Fragment(R.layout.fragment_data_storage) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val manageStorageTextView = view.findViewById<TextView>(R.id.manageStorageTextView)
        val mobileDataTextView = view.findViewById<TextView>(R.id.mobileDataTextView)
        val wifiTextView = view.findViewById<TextView>(R.id.wifiTextView)
        val roamingTextView = view.findViewById<TextView>(R.id.roamingTextView)
        val mediaQualityTextView = view.findViewById<TextView>(R.id.mediaQualityTextView)
        val callsTextView = view.findViewById<TextView>(R.id.callsTextView)

        manageStorageTextView.setOnClickListener {
            // Code to navigate to MemoryStorageFragment
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.container, MemoryStorageFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        mobileDataTextView.setOnClickListener { showMediaAutoDownloadDialog("When using mobile data") }
        wifiTextView.setOnClickListener { showMediaAutoDownloadDialog("When using Wi-Fi") }
        roamingTextView.setOnClickListener { showMediaAutoDownloadDialog("When roaming") }
        mediaQualityTextView.setOnClickListener { showMediaQualityDialog() }
        callsTextView.setOnClickListener { showCallsDataUsageDialog() }
    }

    private fun showMediaAutoDownloadDialog(title: String) {
        val options = arrayOf("Images", "Audio", "Video", "Documents")
        val checkedItems = booleanArrayOf(true, true, false, false) // Example selection

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMultiChoiceItems(options, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMediaQualityDialog() {
        val options = arrayOf("Standard", "High")
        AlertDialog.Builder(requireContext())
            .setTitle("Sent media quality")
            .setSingleChoiceItems(options, 0) { dialog, which ->
                // Handle media quality selection (0 for Standard, 1 for High)
            }
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCallsDataUsageDialog() {
        val options = arrayOf("Never", "Mobile data only", "WiFi and mobile data")
        AlertDialog.Builder(requireContext())
            .setTitle("Use less data for calls")
            .setSingleChoiceItems(options, 0) { dialog, which ->
                // Handle calls data usage selection
            }
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
