package com.example.loyalisttest.auth.inProgres//package com.example.loyalisttest.auth
//
//import android.content.Context
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.layoutId
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.constraintlayout.compose.ConstraintSet
//import com.example.loyalisttest.R
//import com.google.firebase.Firebase
//import com.google.firebase.auth.ActionCodeSettings
//import com.google.firebase.auth.auth
//import kotlin.random.Random
//
//@Composable
//fun ResetPasswordScreen(
//    onBackClick: () -> Unit,
//    onSignInClick: () -> Unit,
//    onResetClick: (String) -> Unit
//) {
//    ResetPasswordContent(
//        onBackClick = onBackClick,
//        onSignInClick = onSignInClick,
//        onResetClick = onResetClick,
//        isPreview = false
//    )
//}
//
//@Composable
//private fun ResetPasswordContent(
//    onBackClick: () -> Unit,
//    onSignInClick: () -> Unit,
//    onResetClick: (String) -> Unit,
//    isPreview: Boolean
//) {
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var isPasswordVisible by remember { mutableStateOf(false) }
//    var isLoading by remember { mutableStateOf(false) }
//
//    val context = LocalContext.current
//    val auth = if (!isPreview) remember { Firebase.auth } else null
//
//    val textFieldColors = OutlinedTextFieldDefaults.colors(
//        focusedBorderColor = Color.Black,
//        unfocusedBorderColor = Color.Gray,
//        focusedLabelColor = Color.Black,
//        unfocusedLabelColor = Color.Gray,
//        focusedTrailingIconColor = Color.Black,
//        unfocusedTrailingIconColor = Color.Gray
//    )
//
//    fun validateInput(): Boolean {
//        if (password.isBlank() || confirmPassword.isBlank()) {
//            if (!isPreview) {
//                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
//            }
//            return false
//        }
//        if (password != confirmPassword) {
//            if (!isPreview) {
//                Toast.makeText(context, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
//            }
//            return false
//        }
//        if (password.length < 8) {
//            if (!isPreview) {
//                Toast.makeText(context, "Пароль должен быть не менее 8 символов", Toast.LENGTH_SHORT).show()
//            }
//            return false
//        }
//        return true
//    }
//
//    fun handleResetPassword(email: String) {
//        if (email.isBlank()) {
//            Toast.makeText(context, "Введите email", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        isLoading = true
//
//        // Генерируем случайный 4-значный код
//        val verificationCode = String.format("%04d", Random.nextInt(10000))
//
//        // Сохраняем email и код в SharedPreferences для последующей верификации
//        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
//        with(sharedPrefs.edit()) {
//            putString("reset_email", email)
//            putString("verification_code", verificationCode)
//            putLong("code_timestamp", System.currentTimeMillis())
//            apply()
//        }
//
//        // Отправляем email с кодом через Firebase Custom Email
//        val actionCodeSettings = ActionCodeSettings.newBuilder()
//            .setHandleCodeInApp(true)
//            .setAndroidPackageName(
//                context.packageName,
//                true, /* installIfNotAvailable */
//                null /* minimumVersion */
//            )
//            .build()
//
//        auth!!.sendSignInLinkToEmail(email, actionCodeSettings)
//            .addOnCompleteListener { task ->
//                isLoading = false
//                if (task.isSuccessful) {
//                    Log.d("ForgotPasswordScreen", "Код отправлен")
//                    Toast.makeText(
//                        context,
//                        "Код подтверждения отправлен",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    onResetClick(email) // Переходим к экрану ввода кода
//                } else {
//                    Log.e("ForgotPasswordScreen", "Ошибка отправки кода", task.exception)
//                    Toast.makeText(
//                        context,
//                        "Ошибка отправки кода: ${task.exception?.message}",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//            }
//    }
//
//    val constraints = ConstraintSet {
//        val backButton = createRefFor("backButton")
//        val title = createRefFor("title")
//        val description = createRefFor("description")
//        val passwordField = createRefFor("passwordField")
//        val confirmPasswordField = createRefFor("confirmPasswordField")
//        val resetButton = createRefFor("resetButton")
//        val signInButton = createRefFor("signInButton")
//
//        constrain(backButton) {
//            top.linkTo(parent.top, margin = 48.dp)
//            start.linkTo(parent.start)
//        }
//
//        constrain(title) {
//            top.linkTo(backButton.bottom, margin = 32.dp)
//            start.linkTo(parent.start)
//        }
//
//        constrain(description) {
//            top.linkTo(title.bottom, margin = 8.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//        }
//
//        constrain(passwordField) {
//            top.linkTo(description.bottom, margin = 32.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//        }
//
//        constrain(confirmPasswordField) {
//            top.linkTo(passwordField.bottom, margin = 16.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//        }
//
//        constrain(resetButton) {
//            top.linkTo(confirmPasswordField.bottom, margin = 32.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//        }
//
//        constrain(signInButton) {
//            bottom.linkTo(parent.bottom, margin = 18.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//        }
//    }
//
//    ConstraintLayout(
//        constraintSet = constraints,
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp)
//    ) {
//        IconButton(
//            onClick = onBackClick,
//            modifier = Modifier
//                .layoutId("backButton")
//                .width(40.dp)
//                .height(40.dp)
//                .background(
//                    color = Color.Black,
//                    shape = RoundedCornerShape(10.dp)
//                )
//                .padding(12.dp)
//        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.ic_back),
//                contentDescription = "Назад",
//                tint = Color.White,
//                modifier = Modifier.fillMaxSize()
//            )
//        }
//
//        Text(
//            text = "Восстановить\nпароль",
//            fontSize = 30.sp,
//            fontWeight = FontWeight.SemiBold,
//            lineHeight = 35.sp,
//            modifier = Modifier.layoutId("title")
//        )
//
//        Text(
//            text = "Придумайте новый пароль, чтоб восстановить доступ к вашему аккаунту.",
//            fontSize = 16.sp,
//            color = Color.Gray,
//            textAlign = TextAlign.Start,
//            modifier = Modifier
//                .layoutId("description")
//                .fillMaxWidth()
//        )
//
//        OutlinedTextField(
//            value = password,
//            onValueChange = { password = it },
//            label = { Text("Новый пароль") },
//            placeholder = { Text("не менее 8 символов") },
//            singleLine = true,
//            visualTransformation = if (isPasswordVisible)
//                VisualTransformation.None
//            else
//                PasswordVisualTransformation(),
//            colors = textFieldColors,
//            trailingIcon = {
//                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
//                    Icon(
//                        painter = painterResource(
//                            if (isPasswordVisible) R.drawable.icon_visible_on
//                            else R.drawable.icon_visible_off
//                        ),
//                        contentDescription = if (isPasswordVisible) "Скрыть пароль" else "Показать пароль"
//                    )
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .layoutId("passwordField")
//        )
//
//        OutlinedTextField(
//            value = confirmPassword,
//            onValueChange = { confirmPassword = it },
//            label = { Text("Подтвердить новый пароль") },
//            placeholder = { Text("повторите пароль") },
//            singleLine = true,
//            visualTransformation = if (isPasswordVisible)
//                VisualTransformation.None
//            else
//                PasswordVisualTransformation(),
//            colors = textFieldColors,
//            trailingIcon = {
//                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
//                    Icon(
//                        painter = painterResource(
//                            if (isPasswordVisible) R.drawable.icon_visible_on
//                            else R.drawable.icon_visible_off
//                        ),
//                        contentDescription = if (isPasswordVisible) "Скрыть пароль" else "Показать пароль"
//                    )
//                }
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .layoutId("confirmPasswordField")
//        )
//
//        Button(
//            onClick = { handleResetPassword(password) },
//            enabled = !isLoading,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp)
//                .layoutId("resetButton"),
//            shape = RoundedCornerShape(10.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Black,
//                disabledContainerColor = Color.Gray
//            )
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(
//                    color = Color.White,
//                    modifier = Modifier.size(24.dp)
//                )
//            } else {
//                Text(
//                    text = "Изменить пароль",
//                    fontSize = 16.sp
//                )
//            }
//        }
//
//        Row(
//            modifier = Modifier
//                .layoutId("signInButton")
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Уже есть аккаунт? ",
//                color = Color.Gray
//            )
//            TextButton(
//                onClick = onSignInClick,
//                contentPadding = PaddingValues(0.dp),
//                colors = ButtonDefaults.textButtonColors(
//                    contentColor = Color.Black,
//                    containerColor = Color.Transparent
//                )
//            ) {
//                Text(
//                    text = "Войти",
//                    color = Color.Black
//                )
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ResetPasswordPreview() {
//    MaterialTheme {
//        ResetPasswordContent(
//            onBackClick = {},
//            onSignInClick = {},
//            onResetClick = {},
//            isPreview = true
//        )
//    }
//}