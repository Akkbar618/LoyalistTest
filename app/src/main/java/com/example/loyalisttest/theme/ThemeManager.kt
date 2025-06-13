package com.example.loyalisttest.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object ThemeManager {
    private const val PREF_NAME = "theme_preferences"
    private const val KEY_DARK_MODE = "dark_mode"

    private lateinit var preferences: SharedPreferences
    private val _darkTheme = mutableStateOf(false)
    val darkTheme: State<Boolean> = _darkTheme

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        _darkTheme.value = preferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun toggleTheme(context: Context) {
        val newValue = !_darkTheme.value
        _darkTheme.value = newValue
        preferences.edit().putBoolean(KEY_DARK_MODE, newValue).apply()
    }
}
