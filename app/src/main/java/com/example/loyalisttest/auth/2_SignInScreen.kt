package com.example.loyalisttest.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import com.example.loyalisttest.R

@Composable
fun SignInScreen(
    onBackClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignInClick: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Black,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Gray,
        focusedLeadingIconColor = Color.Black,
        unfocusedLeadingIconColor = Color.Gray,
        focusedTrailingIconColor = Color.Black,
        unfocusedTrailingIconColor = Color.Gray
    )

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
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .layoutId("backButton")
                .width(40.dp)
                .height(40.dp)
                .background(
                    color = Color.Black,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "Назад",
                tint = Color.White,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "Авторизация",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .layoutId("title")
                .width(600.dp)
                .height(39.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Электронная почта") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .layoutId("emailField")
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            colors = textFieldColors,
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (isPasswordVisible) R.drawable.icon_visible_on
                            else R.drawable.icon_visible_off
                        ),
                        contentDescription = if (isPasswordVisible) "Скрыть пароль" else "Показать пароль"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .layoutId("passwordField")
        )

        TextButton(
            onClick = onForgotPasswordClick,
            modifier = Modifier.layoutId("forgotPassword")
        ) {
            Text(
                text = "Забыли пароль?",
                color = Color.Black
            )
        }

        Button(
            onClick = { onSignInClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .layoutId("signInButton"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            Text(
                text = "Войти",
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier.layoutId("divider"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            )
            Text(text = "или")
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
                onClick = { /* Handle Facebook login */ },
                modifier = Modifier.size(width = 100.dp, height = 48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.facebook),
                    contentDescription = "Facebook"
                )
            }

            OutlinedButton(
                onClick = { /* Handle Google login */ },
                modifier = Modifier.size(width = 100.dp, height = 48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = "Google"
                )
            }

            OutlinedButton(
                onClick = { /* Handle Apple login */ },
                modifier = Modifier.size(width = 100.dp, height = 48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.apple),
                    contentDescription = "Apple"
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
                text = "Нет аккаунта? ",
                color = Color.Gray
            )
            TextButton(
                onClick = onRegisterClick,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Black,
                    containerColor = Color.Transparent
                ),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Text(
                    text = "Зарегистрироваться",
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    MaterialTheme {
        SignInScreen(
            onBackClick = {},
            onForgotPasswordClick = {},
            onSignInClick = { _, _ -> },
            onRegisterClick = {}
        )
    }
}

