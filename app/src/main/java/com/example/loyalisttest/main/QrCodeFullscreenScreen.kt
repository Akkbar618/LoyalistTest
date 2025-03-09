package com.example.loyalisttest.main

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.loyalisttest.R
import com.example.loyalisttest.utils.QrCodeGenerator
import com.google.firebase.auth.FirebaseAuth

@Composable
fun QrCodeFullscreenScreen(navController: NavHostController) {
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val userId = currentUser?.uid ?: ""
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            try {
                qrCodeBitmap = QrCodeGenerator.generateQrCode(userId, 512, 512)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { navController.popBackStack() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else if (qrCodeBitmap != null) {
            Image(
                bitmap = qrCodeBitmap!!.asImageBitmap(),
                contentDescription = stringResource(R.string.qr_code_fullscreen),
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
            )
        }
    }
}