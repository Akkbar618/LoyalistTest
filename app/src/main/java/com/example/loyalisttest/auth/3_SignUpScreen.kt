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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(
    onBackClick: () -> Unit,
    onSignUpClick: (name: String, email: String, password: String) -> Unit,
    onSignInClick: () -> Unit,
    auth: FirebaseAuth? = Firebase.auth
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    fun validateInput(): Boolean {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(context, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(context, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    fun createUserProfile(userId: String, name: String, email: String) {
        val userProfile = hashMapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "registrationDate" to System.currentTimeMillis(),
            "totalPoints" to 0,
            "visitCount" to 0
        )

        firestore.collection("users")
            .document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                Log.d("SignUpScreen", "Профиль пользователя создан")
            }
            .addOnFailureListener { e ->
                Log.e("SignUpScreen", "Ошибка создания профиля", e)
                Toast.makeText(
                    context,
                    "Ошибка создания профиля: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    fun handleSignUp(name: String, email: String, password: String) {
        if (!validateInput()) return

        if (auth == null) {
            onSignUpClick(name, email, password)
            return
        }

        isLoading = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                // Создаем профиль пользователя в Firestore
                                user.uid.let { userId ->
                                    createUserProfile(userId, name, email)
                                }

                                isLoading = false
                                Log.d("SignUpScreen", "Регистрация успешна")
                                Toast.makeText(
                                    context,
                                    "Регистрация успешна",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onSignUpClick(name, email, password)
                            } else {
                                isLoading = false
                                Log.e("SignUpScreen", "Ошибка обновления профиля", profileTask.exception)
                                Toast.makeText(
                                    context,
                                    "Ошибка обновления профиля: ${profileTask.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    isLoading = false
                    Log.e("SignUpScreen", "Ошибка регистрации", task.exception)
                    Toast.makeText(
                        context,
                        "Ошибка регистрации: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    val constraints = ConstraintSet {
        val backButton = createRefFor("backButton")
        val title = createRefFor("title")
        val nameField = createRefFor("nameField")
        val emailField = createRefFor("emailField")
        val passwordField = createRefFor("passwordField")
        val confirmPasswordField = createRefFor("confirmPasswordField")
        val signUpButton = createRefFor("signUpButton")
        val signInButton = createRefFor("signInButton")

        constrain(backButton) {
            top.linkTo(parent.top, margin = 48.dp)
            start.linkTo(parent.start)
        }

        constrain(title) {
            top.linkTo(backButton.bottom, margin = 32.dp)
            start.linkTo(parent.start)
        }

        constrain(nameField) {
            top.linkTo(title.bottom, margin = 32.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(emailField) {
            top.linkTo(nameField.bottom, margin = 16.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(passwordField) {
            top.linkTo(emailField.bottom, margin = 16.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(confirmPasswordField) {
            top.linkTo(passwordField.bottom, margin = 16.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(signUpButton) {
            top.linkTo(confirmPasswordField.bottom, margin = 32.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(signInButton) {
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
            text = stringResource(R.string.sign_up_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.layoutId("title")
        )

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.name_label),
            modifier = Modifier.layoutId("nameField")
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

        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = stringResource(R.string.confirm_password_label),
            isPassword = true,
            isPasswordVisible = isPasswordVisible,
            onVisibilityChange = { isPasswordVisible = !isPasswordVisible },
            modifier = Modifier.layoutId("confirmPasswordField")
        )

        AuthButton(
            text = stringResource(R.string.sign_up_button),
            onClick = { handleSignUp(name, email, password) },
            isLoading = isLoading,
            modifier = Modifier.layoutId("signUpButton")
        )

        Row(
            modifier = Modifier
                .layoutId("signInButton")
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.already_have_account),
                color = Color.Gray
            )
            TextButton(
                onClick = onSignInClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(R.string.sign_in_link),
                    color = Color.Black
                )
            }
        }
    }
}