package com.example.loyalisttest.auth.inProgres//package com.example.loyalisttest.auth
//
//import android.content.Context
//import android.os.CountDownTimer
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.layoutId
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.constraintlayout.compose.ConstraintSet
//import com.example.loyalisttest.R
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//@Composable
//fun VerificationCodeScreen(
//    email: String,
//    onBackClick: () -> Unit,
//    onVerifyClick: (String) -> Unit
//) {
//    VerificationCodeContent(
//        email = email,
//        onBackClick = onBackClick,
//        onVerifyClick = onVerifyClick,
//        isPreview = false
//    )
//}
//
//@Composable
//private fun VerificationCodeContent(
//    email: String,
//    onBackClick: () -> Unit,
//    onVerifyClick: (String) -> Unit,
//    isPreview: Boolean
//) {
//    var code by remember { mutableStateOf(List(4) { "" }) }
//    var currentIndex by remember { mutableStateOf(0) }
//    var isLoading by remember { mutableStateOf(false) }
//    var timeLeft by remember { mutableStateOf(20) } // 20 seconds timer
//    val focusRequesters = remember { List(4) { FocusRequester() } }
//    val context = LocalContext.current
//
//    // Timer for resend code
//    LaunchedEffect(Unit) {
//        while (timeLeft > 0) {
//            delay(1000)
//            timeLeft--
//        }
//    }
//
//    fun handleResendCode() {
//        if (timeLeft <= 0) {
//            timeLeft = 20
//            if (!isPreview) {
//                Toast.makeText(context, "Код отправлен повторно", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    fun validateAndSubmit() {
//        val fullCode = code.joinToString("")
//        if (fullCode.length != 4) {
//            if (!isPreview) {
//                Toast.makeText(context, "Введите 4-значный код", Toast.LENGTH_SHORT).show()
//            }
//            return
//        }
//        onVerifyClick(fullCode)
//    }
//
//    fun validateCode(code: String) {
//        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
//        val savedCode = sharedPrefs.getString("verification_code", null)
//        val savedTimestamp = sharedPrefs.getLong("code_timestamp", 0)
//        val currentTime = System.currentTimeMillis()
//
//        // Проверяем, не истек ли срок действия кода (20 минут)
//        if (currentTime - savedTimestamp > 20 * 60 * 1000) {
//            Toast.makeText(context, "Код истек. Запросите новый", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        if (code == savedCode) {
//            // Код верный, переходим к сбросу пароля
//            onVerifyClick(code)
//        } else {
//            Toast.makeText(context, "Неверный код", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    val constraints = ConstraintSet {
//        val backButton = createRefFor("backButton")
//        val title = createRefFor("title")
//        val description = createRefFor("description")
//        val codeInputs = createRefFor("codeInputs")
//        val verifyButton = createRefFor("verifyButton")
//        val resendCode = createRefFor("resendCode")
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
//        constrain(codeInputs) {
//            top.linkTo(description.bottom, margin = 32.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//        }
//
//        constrain(verifyButton) {
//            top.linkTo(codeInputs.bottom, margin = 32.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//        }
//
//        constrain(resendCode) {
//            top.linkTo(verifyButton.bottom, margin = 16.dp)
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
//            text = "Проверьте вашу\nэлектронную почту",
//            fontSize = 30.sp,
//            fontWeight = FontWeight.SemiBold,
//            lineHeight = 35.sp,
//            modifier = Modifier.layoutId("title")
//        )
//
//        Text(
//            text = "Мы отправили вам код\nна $email",
//            fontSize = 16.sp,
//            color = Color.Gray,
//            textAlign = TextAlign.Start,
//            modifier = Modifier
//                .layoutId("description")
//                .fillMaxWidth()
//        )
//
//        // Code input boxes
//        Row(
//            modifier = Modifier
//                .layoutId("codeInputs")
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            code.forEachIndexed { index, _ ->
//                OutlinedTextField(
//                    value = code[index],
//                    onValueChange = { newValue ->
//                        if (newValue.length <= 1 && newValue.all { it.isDigit() }) {
//                            val newCode = code.toMutableList()
//                            newCode[index] = newValue
//                            code = newCode
//
//                            if (newValue.isNotEmpty() && index < 3) {
//                                focusRequesters[index + 1].requestFocus()
//                            }
//                        }
//                    },
//                    modifier = Modifier
//                        .width(64.dp)
//                        .focusRequester(focusRequesters[index]),
//                    textStyle = LocalTextStyle.current.copy(
//                        textAlign = TextAlign.Center,
//                        fontSize = 24.sp,
//                        fontWeight = FontWeight.Bold
//                    ),
//                    singleLine = true,
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = Color.Black,
//                        unfocusedBorderColor = Color.Gray
//                    ),
//                    shape = RoundedCornerShape(12.dp)
//                )
//            }
//        }
//
//        Button(
//            onClick = { validateAndSubmit() },
//            enabled = !isLoading && code.all { it.isNotEmpty() },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp)
//                .layoutId("verifyButton"),
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
//                    text = "Подтвердить",
//                    fontSize = 16.sp
//                )
//            }
//        }
//
//        TextButton(
//            onClick = { handleResendCode() },
//            enabled = timeLeft <= 0,
//            modifier = Modifier.layoutId("resendCode")
//        ) {
//            Text(
//                text = if (timeLeft > 0)
//                    "Прислать код повторно ${String.format("%02d:%02d", timeLeft / 60, timeLeft % 60)}"
//                else
//                    "Прислать код повторно",
//                color = if (timeLeft <= 0) Color.Black else Color.Gray
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun VerificationCodePreview() {
//    MaterialTheme {
//        VerificationCodeContent(
//            email = "example@gmail.com",
//            onBackClick = {},
//            onVerifyClick = {},
//            isPreview = true
//        )
//    }
//}