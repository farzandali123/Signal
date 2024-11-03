package com.example.signal

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.content.SharedPreferences
import android.widget.RadioButton
import android.widget.RadioGroup
import java.util.*

class LanguageDialogFragment : DialogFragment() {

    private val PREFS_NAME = "app_preferences"
    private val LANGUAGE_KEY = "selected_language"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_language_selection, null)

        val radioGroup = view.findViewById<RadioGroup>(R.id.languageRadioGroup)
        val languages = listOf("English", "Afrikaans", "العربية", "Azerbaijani", "বাংলা", "Bosanski", "Български", "Català", "Čeština")
        val languageCodes = listOf("en", "af", "ar", "az", "bn", "bs", "bg", "ca", "cs")

        // Add radio buttons dynamically
        languages.forEachIndexed { index, language ->
            val radioButton = RadioButton(context)
            radioButton.text = language
            radioButton.id = index // Set an id for each radio button
            radioGroup.addView(radioButton)
        }

        // Set default selection from saved language
        val savedLanguage = getSavedLanguage(requireContext())
        val defaultIndex = languageCodes.indexOf(savedLanguage).takeIf { it >= 0 } ?: 0
        radioGroup.check(defaultIndex)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = languageCodes[checkedId]
            saveLanguage(requireContext(), languageCode)
            setLocale(requireContext(), languageCode)
        }

        builder.setView(view)
            .setTitle("Language")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

    private fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }

    private fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, "en") ?: "en"
    }

    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
