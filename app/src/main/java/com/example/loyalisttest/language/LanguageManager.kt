//package com.example.loyalisttest.language
//
//import android.content.Context
//import android.content.SharedPreferences
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import java.util.Locale
//
//object LanguageManager {
//    private const val PREF_NAME = "language_preferences"
//    private const val KEY_LANGUAGE = "selected_language"
//
//    private lateinit var preferences: SharedPreferences
//    private val _currentLanguage = MutableStateFlow(AppLanguage.RUSSIAN)
//    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage
//
//    fun init(context: Context) {
//        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
//        val savedLanguage = preferences.getString(KEY_LANGUAGE, AppLanguage.RUSSIAN.code)
//        _currentLanguage.value = AppLanguage.fromCode(savedLanguage ?: AppLanguage.RUSSIAN.code)
//        updateResources(context, _currentLanguage.value)
//    }
//
//    fun setLanguage(language: AppLanguage, context: Context) {
//        preferences.edit().putString(KEY_LANGUAGE, language.code).apply()
//        _currentLanguage.value = language
//        updateResources(context, language)
//    }
//
//    private fun updateResources(context: Context, language: AppLanguage) {
//        val locale = Locale(language.code)
//        Locale.setDefault(locale)
//
//        val config = context.resources.configuration
//        config.setLocale(locale)
//        context.createConfigurationContext(config)
//        context.resources.updateConfiguration(config, context.resources.displayMetrics)
//    }
//}