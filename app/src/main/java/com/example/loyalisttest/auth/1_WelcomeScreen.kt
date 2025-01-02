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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import com.example.loyalisttest.R
import com.example.loyalisttest.components.AuthButton
import com.example.loyalisttest.components.OutlinedAuthButton


@Composable
fun WelcomeScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
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
            .padding(horizontal = 25.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.pic_welcome),
            contentDescription = "Welcome Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(10.dp))
                .layoutId("picture")
        )

        Text(
            text = stringResource(R.string.welcome_title),
            fontSize = 30.sp,
            color = colorResource(R.color.light_black),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.layoutId("title")
        )

        Text(
            text = stringResource(R.string.welcome_subtitle),
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            color = colorResource(R.color.my_gray),
            modifier = Modifier.layoutId("subtitle")
        )

        AuthButton(
            text = stringResource(R.string.auth_button),
            onClick = onNavigateToSignIn,
            modifier = Modifier.layoutId("authButton")
        )

        OutlinedAuthButton(
            text = stringResource(R.string.register_button),
            onClick = onNavigateToSignUp,
            modifier = Modifier.layoutId("regButton")
        )
    }
}