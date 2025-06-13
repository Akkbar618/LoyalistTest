package com.example.loyalisttest

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.example.loyalisttest.language.LanguageManager
import com.example.loyalisttest.theme.ThemeManager

class LoyalistApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize language first to ensure proper localization
        LanguageManager.init(this)
        ThemeManager.init(this)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings
    }
}