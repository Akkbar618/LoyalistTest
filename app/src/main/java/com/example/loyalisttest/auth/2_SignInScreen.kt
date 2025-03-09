package com.example.loyalisttest.auth

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import com.example.loyalisttest.R
import com.example.loyalisttest.components.AuthButton
import com.example.loyalisttest.components.AuthTextField
import com.example.loyalisttest.components.BackButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignInScreen(
    onBackClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignInClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit,
    auth: FirebaseAuth? = Firebase.auth
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    fun checkAndCreateUserProfile(userId: String, email: String) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    // Create profile if it doesn't exist
                    val userProfile = hashMapOf(
                        "userId" to userId,
                        "email" to email,
                        "name" to (auth?.currentUser?.displayName ?: email.substringBefore("@")),
                        "registrationDate" to System.currentTimeMillis(),
                        "totalPoints" to 0,
                        "visitCount" to 0
                    )

                    firestore.collection("users")
                        .document(userId)
                        .set(userProfile)
                        .addOnSuccessListener {
                            Log.d("SignInScreen", "User profile created")
                        }
                        .addOnFailureListener { e ->
                            Log.e("SignInScreen", "Error creating profile", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("SignInScreen", "Error checking profile", e)
            }
    }

    fun handleSignIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, context.getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        if (auth == null) {
            onSignInClick(email, password)
            return
        }

        isLoading = true
        Log.d("SignInScreen", "Attempting to sign in with email: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                Log.d("SignInScreen", "Sign in task completed. Success: ${task.isSuccessful}")

                if (task.isSuccessful) {
                    // Check and create user profile if needed
                    auth.currentUser?.uid?.let { userId ->
                        firestore.collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    // Create profile if it doesn't exist
                                    val userProfile = hashMapOf(
                                        "userId" to userId,
                                        "email" to email,
                                        "name" to (auth.currentUser?.displayName ?: email.substringBefore("@")),
                                        "role" to "USER",
                                        "registrationDate" to System.currentTimeMillis(),
                                        "totalPoints" to 0,
                                        "visitCount" to 0
                                    )

                                    firestore.collection("users")
                                        .document(userId)
                                        .set(userProfile)
                                        .addOnSuccessListener {
                                            Log.d("SignInScreen", "User profile created")
                                            isLoading = false
                                            Toast.makeText(context, context.getString(R.string.success_auth), Toast.LENGTH_SHORT).show()
                                            onSignInClick(email, password)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("SignInScreen", "Error creating profile", e)
                                            isLoading = false
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.error_profile_creation, e.message ?: ""),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                } else {
                                    // Profile exists, continue
                                    Log.d("SignInScreen", "User profile exists")
                                    isLoading = false
                                    Toast.makeText(context, context.getString(R.string.success_auth), Toast.LENGTH_SHORT).show()
                                    onSignInClick(email, password)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("SignInScreen", "Error checking profile", e)
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.error_profile_check, e.message ?: ""),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                } else {
                    Log.e("SignInScreen", "Sign in failed", task.exception)
                    isLoading = false
                    Toast.makeText(
                        context,
                        task.exception?.message ?: context.getString(R.string.error_auth),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    val constraints = ConstraintSet {
        val backButton = createRefFor("backButton")
        val title = createRefFor("title")
        val emailField = createRefFor("emailField")
        val passwordField = createRefFor("passwordField")
        val forgotPassword = createRefFor("forgotPassword")
        val signInButton = createRefFor("signInButton")
        val divider = createRefFor("divider")
        val socialButtons = createRefFor("socialButtons")
        val registerButton = createRefFor("registerButton")

        constrain(backButton) {
            top.linkTo(parent.top, margin = 48.dp)
            start.linkTo(parent.start)
        }

        constrain(title) {
            top.linkTo(backButton.bottom, margin = 32.dp)
            start.linkTo(parent.start)
        }

        constrain(emailField) {
            top.linkTo(title.bottom, margin = 32.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(passwordField) {
            top.linkTo(emailField.bottom, margin = 16.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(forgotPassword) {
            top.linkTo(passwordField.bottom, margin = 16.dp)
            end.linkTo(parent.end)
        }

        constrain(signInButton) {
            top.linkTo(forgotPassword.bottom, margin = 32.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(divider) {
            top.linkTo(signInButton.bottom, margin = 32.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(socialButtons) {
            top.linkTo(divider.bottom, margin = 24.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(registerButton) {
            bottom.linkTo(parent.bottom, margin = 18.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
    }

    ConstraintLayout(
        constraintSet = constraints,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 25.dp)
    ) {
        BackButton(
            onClick = onBackClick,
            modifier = Modifier.layoutId("backButton")
        )

        Text(
            text = stringResource(R.string.sign_in_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.layoutId("title")
        )

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.email_label),
            modifier = Modifier.layoutId("emailField")
        )

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.password_label),
            isPassword = true,
            isPasswordVisible = isPasswordVisible,
            onVisibilityChange = { isPasswordVisible = !isPasswordVisible },
            modifier = Modifier.layoutId("passwordField")
        )

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.layoutId("forgotPassword")
        ) {
            Text(
                text = stringResource(R.string.forgot_password),
                color = Color.Black
            )
        }

        AuthButton(
            text = stringResource(R.string.sign_in_button),
            onClick = { handleSignIn(email, password) },
            isLoading = isLoading,
            modifier = Modifier.layoutId("signInButton")
        )

        Row(
            modifier = Modifier.layoutId("divider"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            )
            Text(text = stringResource(R.string.or))
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
        }

        Row(
            modifier = Modifier
                .layoutId("socialButtons")
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { },
                modifier = Modifier.size(width = 100.dp, height = 48.dp),
                shape = MaterialTheme.shapes.medium,
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.facebook),
                    contentDescription = "Facebook",
                    tint = Color.Unspecified,
                )
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.size(width = 100.dp, height = 48.dp),
                shape = MaterialTheme.shapes.medium,
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google",
                    tint = Color.Unspecified,
                )
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.size(width = 100.dp, height = 48.dp),
                shape = MaterialTheme.shapes.medium,
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.apple),
                    contentDescription = "Apple",
                    tint = Color.Unspecified,
                )
            }
        }

        Row(
            modifier = Modifier
                .layoutId("registerButton")
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dont_have_account),
                color = Color.Gray
            )
            TextButton(
                onClick = onRegisterClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.sign_up_link),
                    color = Color.Black
                )
            }
        }
    }
}