package com.example.signal

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.signal.databinding.FragmentMemoryStorageBinding

class MemoryStorageFragment : Fragment(R.layout.fragment_memory_storage) {

    private var _binding: FragmentMemoryStorageBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMemoryStorageBinding.bind(view)

        // Set up the back button click listener to go back to the previous fragment
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
