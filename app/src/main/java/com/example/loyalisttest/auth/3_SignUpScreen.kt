package com.example.loyalisttest.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SignUpScreen(
    onBackClick: () -> Unit,
    onSignUpClick: (name: String, email: String, password: String) -> Unit,
    onSignInClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

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
        val nameField = createRefFor("nameField")
        val emailField = createRefFor("emailField")
        val passwordField = createRefFor("passwordField")
        val confirmPasswordField = createRefFor("confirmPasswordField")
        val signUpButton = createRefFor("signUpButton")
        val divider = createRefFor("divider")
        val socialButtons = createRefFor("socialButtons")
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

        constrain(divider) {
            top.linkTo(signUpButton.bottom, margin = 32.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(socialButtons) {
            top.linkTo(divider.bottom, margin = 24.dp)
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
            text = "Регистрация",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .layoutId("title")
                .width(600.dp)
                .height(39.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Ваше имя") },
            singleLine = true,
            colors = textFieldColors,
            modifier = Modifier
                .fillMaxWidth()
                .layoutId("nameField")
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
            label = { Text("Придумайте пароль") },
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

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Подтвердите пароль") },
            singleLine = true,
            visualTransformation = if (isConfirmPasswordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            colors = textFieldColors,
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (isConfirmPasswordVisible) R.drawable.icon_visible_on
                            else R.drawable.icon_visible_off
                        ),
                        contentDescription = if (isConfirmPasswordVisible) "Скрыть пароль" else "Показать пароль"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .layoutId("confirmPasswordField")
        )

        Button(
            onClick = {
                if (password == confirmPassword) {
                    onSignUpClick(name, email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .layoutId("signUpButton"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            Text(
                text = "Создать аккаунт",
                fontSize = 16.sp
            )
        }

        Row(
            modifier = Modifier
                .layoutId("signInButton")
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Уже есть аккаунт? ",
                color = Color.Gray
            )
            TextButton(
                onClick = onSignInClick,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Black,
                    containerColor = Color.Transparent
                )
            ) {
                Text(
                    text = "Войти",
                    color = Color.Black
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPreview() {
    MaterialTheme {
        SignUpScreen(
            onBackClick = {},
            onSignUpClick = { _, _, _ -> },
            onSignInClick = {}
        )
    }
}

