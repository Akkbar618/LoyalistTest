package com.example.loyalisttest.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import com.example.loyalisttest.R

@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
) {
    val constraints = ConstraintSet {
        val picture = createRefFor("picture")
        val title = createRefFor("title")
        val subtitle = createRefFor("subtitle")
        val authButton = createRefFor("authButton")
        val regButton = createRefFor("regButton")

        constrain(picture) {
            top.linkTo(parent.top, margin = 112.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(title) {
            top.linkTo(picture.bottom, margin = 38.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(subtitle) {
            top.linkTo(title.bottom, margin = 22.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(authButton) {
            top.linkTo(subtitle.bottom, margin = 82.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }

        constrain(regButton) {
            top.linkTo(authButton.bottom, margin = 16.dp)
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
        Image(
            painter = painterResource(id = R.drawable.pic_welcome),
            contentDescription = "Welcome Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 324.dp, height = 240.dp)
                .clip(RoundedCornerShape(10.dp))
                .layoutId("picture")
        )

        Text(
            text = "Лоялист",
            fontSize = 30.sp,
            color = colorResource(R.color.light_black),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .layoutId("title")
        )

        Text(
            text = "Все заведения в одном месте\nи под твоим контролем!",
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            color = colorResource(R.color.my_gray),
            modifier = Modifier.layoutId("subtitle")
        )

        Button(
            onClick = onNavigateToSignIn,
            modifier = Modifier
                .width(324.dp)
                .height(56.dp)
                .layoutId("authButton"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.light_black),
                contentColor = colorResource(R.color.my_white),
            )
        ) {
            Text(
                text = "Авторизация",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        OutlinedButton(
            onClick = onNavigateToSignUp,
            modifier = Modifier
                .width(324.dp)
                .height(56.dp)
                .layoutId("regButton"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorResource(R.color.my_gray)
            )
        ) {
            Text(
                text = "Регистрация",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        WelcomeScreen()
    }
}