package com.example.loyalisttest.language

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.example.loyalisttest.MainActivity
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
        val savedLanguage = preferences.getString(KEY_LANGUAGE, getSystemLanguage(context).code)
        _currentLanguage.value = AppLanguage.fromCode(savedLanguage ?: AppLanguage.RUSSIAN.code)
        updateResources(context, _currentLanguage.value)
    }

    fun setLanguage(language: AppLanguage, context: Context) {
        preferences.edit().putString(KEY_LANGUAGE, language.code).apply()
        _currentLanguage.value = language
        updateResources(context, language)

        // Пересоздаем контекст для обновления ресурсов
        if (context is MainActivity) {
            context.recreate()
        }
    }

    private fun getSystemLanguage(context: Context): AppLanguage {
        val locale = ConfigurationCompat.getLocales(context.resources.configuration).get(0)
        return AppLanguage.values().find { it.code == locale?.language } ?: AppLanguage.ENGLISH
    }

    private fun updateResources(context: Context, language: AppLanguage) {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}

@Composable
fun LocalizedContent(content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current

    DisposableEffect(LanguageManager.currentLanguage.value) {
        val locale = Locale(LanguageManager.currentLanguage.value.code)
        Locale.setDefault(locale)

        val config = configuration.apply {
            setLocale(locale)
        }

        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        onDispose { }
    }

    content()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSwitcher(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = LanguageManager.currentLanguage.value.displayName,
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
                        LanguageManager.setLanguage(language, context)
                        isDropdownExpanded = false
                    }
                )
            }
        }
    }
}