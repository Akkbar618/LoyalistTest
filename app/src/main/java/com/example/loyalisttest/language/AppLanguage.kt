package com.example.loyalisttest.language

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.example.loyalisttest.R
import java.util.*

enum class AppLanguage(val code: String, val displayName: String) {
    RUSSIAN("ru", "Русский"),
    ENGLISH("en", "English");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return values().find { it.code == code } ?: RUSSIAN
        }
    }
}

object LanguageManager {
    private const val PREF_NAME = "language_preferences"
    private const val KEY_LANGUAGE = "selected_language"

    private lateinit var preferences: SharedPreferences
    private val _currentLanguage = mutableStateOf(AppLanguage.RUSSIAN)
    val currentLanguage: State<AppLanguage> = _currentLanguage

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedLanguage = preferences.getString(KEY_LANGUAGE, null) ?: getSystemLanguageCode(context)
        _currentLanguage.value = AppLanguage.fromCode(savedLanguage)

        // Apply the saved language configuration
        updateResources(context, _currentLanguage.value)
    }

    fun setLanguage(language: AppLanguage, context: Context): Boolean {
        if (_currentLanguage.value == language) return false

        preferences.edit().putString(KEY_LANGUAGE, language.code).apply()
        _currentLanguage.value = language
        updateResources(context, language)
        return true
    }

    private fun getSystemLanguageCode(context: Context): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return locale?.language ?: "en"
    }

    private fun updateResources(context: Context, language: AppLanguage) {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = context.resources.configuration

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            config.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

@Composable
fun LocalizedContent(content: @Composable () -> Unit) {
    // This is now a more lightweight wrapper that just observes language changes
    val currentLanguage by LanguageManager.currentLanguage

    // The actual content
    content()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSwitcher(
    modifier: Modifier = Modifier,
    onLanguageChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentLanguage by LanguageManager.currentLanguage
    var isDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = currentLanguage.displayName,
            onValueChange = { },
            readOnly = true,
            label = { Text(stringResource(R.string.settings_language)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            AppLanguage.values().forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    onClick = {
                        if (language != currentLanguage) {
                            val changed = LanguageManager.setLanguage(language, context)
                            if (changed) {
                                onLanguageChanged()
                            }
                        }
                        isDropdownExpanded = false
                    }
                )
            }
        }
    }
}