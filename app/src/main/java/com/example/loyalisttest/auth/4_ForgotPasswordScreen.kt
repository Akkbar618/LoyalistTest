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
import com.google.firebase.auth.auth

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth = remember { Firebase.auth }

    fun handleResetPassword(email: String) {
        if (email.isBlank()) {
            Toast.makeText(context, "Введите email", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    Log.d("ForgotPasswordScreen", "Письмо для сброса пароля отправлено")
                    Toast.makeText(
                        context,
                        "Инструкции для сброса пароля отправлены на ваш email",
                        Toast.LENGTH_LONG
                    ).show()
                    onSignInClick()
                } else {
                    Log.e("ForgotPasswordScreen", "Ошибка отправки письма", task.exception)
                    Toast.makeText(
                        context,
                        "Ошибка отправки письма: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    val constraints = ConstraintSet {
        val backButton = createRefFor("backButton")
        val title = createRefFor("title")
        val description = createRefFor("description")
        val emailField = createRefFor("emailField")
        val resetButton = createRefFor("resetButton")
        val signInButton = createRefFor("signInButton")

        constrain(backButton) {
            top.linkTo(parent.top, margin = 48.dp)
            start.linkTo(parent.start)
        }

        constrain(title) {
            top.linkTo(backButton.bottom, margin = 32.dp)
            start.linkTo(parent.start)
        }

        constrain(description) {
            top.linkTo(title.bottom, margin = 8.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(emailField) {
            top.linkTo(description.bottom, margin = 32.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(resetButton) {
            top.linkTo(emailField.bottom, margin = 32.dp)
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
            text = stringResource(R.string.forgot_password_title),
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.layoutId("title")
        )

        Text(
            text = stringResource(R.string.forgot_password_description),
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .layoutId("description")
                .fillMaxWidth()
        )

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.email_label),
            modifier = Modifier.layoutId("emailField")
        )

        AuthButton(
            text = stringResource(R.string.reset_password_button),
            onClick = { handleResetPassword(email) },
            isLoading = isLoading,
            modifier = Modifier.layoutId("resetButton")
        )

        Row(
            modifier = Modifier
                .layoutId("signInButton")
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.remember_password),
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