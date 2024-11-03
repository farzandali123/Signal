package com.example.signal

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        applyUserTheme() // Apply user-selected theme before setting the content view
        applyUserFontSize()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up bottom navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chats -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ChatsFragment())
                        .commit()
                    true
                }
                R.id.nav_calls -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, CallsFragment())
                        .commit()
                    true
                }
                R.id.nav_stories -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, StoriesFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // Load the default fragment
        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_chats
        }
    }

    // Function to show or hide the BottomNavigationView
    fun showNavBar(show: Boolean) {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun applyUserTheme() {
        val themePreference = sharedPreferences.getString("selected_theme", "default")
        val themeId = when (themePreference) {
            "light" -> R.style.Theme_Signal_Light
            "dark" -> R.style.Theme_Signal_Dark
            else -> R.style.Theme_Signal // Default to system theme
        }
        setTheme(themeId)
    }

    private fun applyUserFontSize() {
        val fontSizePreference = sharedPreferences.getString("selected_font_size", "normal")
        val fontStyle = when (fontSizePreference) {
            "small" -> R.style.Theme_Signal_SmallFont
            "normal" -> R.style.Theme_Signal_NormalFont
            "large" -> R.style.Theme_Signal_LargeFont
            "extra_large" -> R.style.Theme_Signal_ExtraLargeFont
            else -> R.style.Theme_Signal_NormalFont
        }
        setTheme(fontStyle)
    }

    // Method to restart activity with the new theme
    fun restartActivityWithNewTheme() {
        finish()
        startActivity(intent)
    }
}
