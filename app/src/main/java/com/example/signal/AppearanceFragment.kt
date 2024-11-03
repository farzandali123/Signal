package com.example.signal

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.signal.databinding.FragmentAppearanceBinding

class AppearanceFragment : Fragment() {

    private var _binding: FragmentAppearanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPref: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppearanceBinding.inflate(inflater, container, false)
        sharedPref = requireContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the theme selection
        binding.themeValue.setOnClickListener {
            showThemeDialog()
        }

        // Set up the font size selection
        binding.fontSizeValue.setOnClickListener {
            showFontSizeDialog()
        }

        // Set up the language selection dialog
        binding.languageValue.setOnClickListener {
            val dialog = LanguageDialogFragment()
            dialog.show(parentFragmentManager, "LanguageDialog")
        }

        // Apply the saved theme and font size on start
        applySavedTheme()
        applySavedFontSize()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("System default", "Light", "Dark")
        val currentThemeIndex = getCurrentThemeIndex()

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, currentThemeIndex) { dialogInterface, which ->
                when (which) {
                    0 -> setTheme("default")
                    1 -> setTheme("light")
                    2 -> setTheme("dark")
                }
                dialogInterface.dismiss()
            }
            .create()
        dialog.show()
    }

    private fun showFontSizeDialog() {
        val fontSizes = arrayOf("Small", "Normal", "Large", "Extra large")
        val currentFontSizeIndex = getCurrentFontSizeIndex()

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Message font size")
            .setSingleChoiceItems(fontSizes, currentFontSizeIndex) { dialogInterface, which ->
                when (which) {
                    0 -> setFontSize("small")
                    1 -> setFontSize("normal")
                    2 -> setFontSize("large")
                    3 -> setFontSize("extra_large")
                }
                dialogInterface.dismiss()
                requireActivity().recreate() // Restart activity to apply font size change across the app
            }
            .create()
        dialog.show()
    }

    private fun setTheme(theme: String) {
        sharedPref.edit().putString("selected_theme", theme).apply()
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun setFontSize(fontSize: String) {
        sharedPref.edit().putString("selected_font_size", fontSize).apply()
    }

    private fun applySavedTheme() {
        val theme = sharedPref.getString("selected_theme", "default")
        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun applySavedFontSize() {
        val fontSize = sharedPref.getString("selected_font_size", "normal")
        when (fontSize) {
            "small" -> binding.root.scaleX = 0.8f
            "normal" -> binding.root.scaleX = 1.0f
            "large" -> binding.root.scaleX = 1.2f
            "extra_large" -> binding.root.scaleX = 1.4f
        }
    }

    private fun getCurrentThemeIndex(): Int {
        return when (sharedPref.getString("selected_theme", "default")) {
            "light" -> 1
            "dark" -> 2
            else -> 0
        }
    }

    private fun getCurrentFontSizeIndex(): Int {
        return when (sharedPref.getString("selected_font_size", "normal")) {
            "small" -> 0
            "normal" -> 1
            "large" -> 2
            "extra_large" -> 3
            else -> 1
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
