package com.example.signal

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult

class ThemeDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_theme_selection, null)

        val radioGroup = view.findViewById<RadioGroup>(R.id.themeRadioGroup)
        val themeOptions = listOf("System default", "Light", "Dark")

        themeOptions.forEachIndexed { index, theme ->
            val radioButton = RadioButton(context)
            radioButton.text = theme
            radioButton.id = index
            radioGroup.addView(radioButton)
        }

        val savedTheme = getSavedTheme(requireContext())
        val defaultTheme = when (savedTheme) {
            "light" -> 1
            "dark" -> 2
            else -> 0
        }
        radioGroup.check(defaultTheme)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedTheme = when (checkedId) {
                1 -> "light"
                2 -> "dark"
                else -> "default"
            }
            saveTheme(requireContext(), selectedTheme)
            setFragmentResult("ThemeDialogDismissed", bundleOf())
            dismiss()
        }

        builder.setView(view)
            .setTitle("Theme")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

    private fun getSavedTheme(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("selected_theme", "default") ?: "default"
    }

    private fun saveTheme(context: Context, themeCode: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_theme", themeCode).apply()
    }
}
