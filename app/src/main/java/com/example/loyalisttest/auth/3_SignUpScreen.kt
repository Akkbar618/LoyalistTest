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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.lifecycle.lifecycleScope
import com.example.loyalisttest.R
import com.example.loyalisttest.components.AuthButton
import com.example.loyalisttest.components.AuthTextField
import com.example.loyalisttest.components.BackButton
import com.example.loyalisttest.utils.FirestoreInitUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    val lifecycleScope = LocalLifecycleOwner.current.lifecycleScope

    fun validateInput(): Boolean {
        return when {
            name.isBlank() -> {
                Toast.makeText(context, "Введите имя", Toast.LENGTH_SHORT).show()
                false
            }
            email.isBlank() -> {
                Toast.makeText(context, "Введите email", Toast.LENGTH_SHORT).show()
                false
            }
            password.isBlank() -> {
                Toast.makeText(context, "Введите пароль", Toast.LENGTH_SHORT).show()
                false
            }
            confirmPassword.isBlank() -> {
                Toast.makeText(context, "Подтвердите пароль", Toast.LENGTH_SHORT).show()
                false
            }
            password != confirmPassword -> {
                Toast.makeText(context, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(context, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    fun handleSignUp(name: String, email: String, password: String) {
        if (!validateInput()) return

        if (auth == null) {
            onSignUpClick(name, email, password)
            return
        }

        isLoading = true
        lifecycleScope.launch {
            try {
                // Проверяем, первый ли это пользователь
                val isFirstUser = FirestoreInitUtils.isFirstUser()

                // Создаем пользователя в Firebase Auth
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user ?: throw Exception("Failed to create user")

                // Обновляем профиль пользователя
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()

                // Инициализируем коллекции Firestore
                FirestoreInitUtils.initializeCollections(user.uid, email, name, isFirstUser)
                    .onSuccess {
                        Toast.makeText(context, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                        onSignUpClick(name, email, password)
                    }
                    .onFailure { e ->
                        throw e
                    }
            } catch (e: Exception) {
                Log.e("SignUpScreen", "Ошибка регистрации", e)
                Toast.makeText(
                    context,
                    "Ошибка регистрации: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                isLoading = false
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