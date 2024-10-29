package com.example.loyalisttest.auth.inProgres//package com.example.loyalisttest.auth
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.layoutId
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.constraintlayout.compose.ConstraintLayout
//import androidx.constraintlayout.compose.ConstraintSet
//
//@Composable
//fun PasswordChangedScreen(
//    onSignInClick: () -> Unit
//) {
//    PasswordChangedContent(
//        onSignInClick = onSignInClick,
//        isPreview = false
//    )
//}
//
//@Composable
//private fun PasswordChangedContent(
//    onSignInClick: () -> Unit,
//    isPreview: Boolean
//) {
//    val constraints = ConstraintSet {
//        val title = createRefFor("title")
//        val description = createRefFor("description")
//        val signInButton = createRefFor("signInButton")
//
//        constrain(title) {
//            top.linkTo(parent.top)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//            bottom.linkTo(description.top)
//        }
//
//        constrain(description) {
//            top.linkTo(title.bottom, margin = 8.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//            bottom.linkTo(signInButton.top)
//        }
//
//        constrain(signInButton) {
//            top.linkTo(description.bottom, margin = 32.dp)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//            bottom.linkTo(parent.bottom)
//        }
//
//        createVerticalChain(title, description, signInButton, chainStyle = androidx.constraintlayout.compose.ChainStyle.Packed)
//    }
//
//    ConstraintLayout(
//        constraintSet = constraints,
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp)
//            .padding(vertical = 32.dp)
//    ) {
//        Text(
//            text = "Пароль изменен",
//            fontSize = 30.sp,
//            fontWeight = FontWeight.SemiBold,
//            textAlign = TextAlign.Center,
//            modifier = Modifier
//                .layoutId("title")
//                .fillMaxWidth()
//        )
//
//        Text(
//            text = "Ваш пароль был успешно изменен.",
//            fontSize = 16.sp,
//            color = Color.Gray,
//            textAlign = TextAlign.Center,
//            modifier = Modifier
//                .layoutId("description")
//                .fillMaxWidth()
//        )
//
//        Button(
//            onClick = onSignInClick,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp)
//                .layoutId("signInButton"),
//            shape = RoundedCornerShape(10.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Black
//            )
//        ) {
//            Text(
//                text = "Перейти к авторизации",
//                fontSize = 16.sp
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PasswordChangedPreview() {
//    MaterialTheme {
//        PasswordChangedContent(
//            onSignInClick = {},
//            isPreview = true
//        )
//    }
//}