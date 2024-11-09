package com.example.loyalisttest.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun AuthCheck(
    onAuthenticated: (FirebaseUser) -> Unit,
    onNeedAuthentication: () -> Unit
) {
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            onAuthenticated(currentUser)
        } else {
            onNeedAuthentication()
        }
    }
}