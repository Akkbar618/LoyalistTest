package com.example.loyalisttest.main

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.loyalisttest.R
import com.example.loyalisttest.models.*
import com.example.loyalisttest.navigation.NavigationRoutes
import com.example.loyalisttest.components.ProgressScale
import com.example.loyalisttest.utils.QrCodeGenerator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    var isAdmin by remember { mutableStateOf(false) }
    var userPointsList by remember { mutableStateOf<List<UserPoints>>(emptyList()) }
    var cafes by remember { mutableStateOf<Map<String, Cafe>>(emptyMap()) }
    var products by remember { mutableStateOf<Map<String, Product>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = remember { currentUser?.displayName ?: "Пользователь" }
    val userId = currentUser?.uid ?: ""
    val firestore = FirebaseFirestore.getInstance()

    // Логика загрузки данных остается прежней

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        // Верхняя панель
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_greeting, userName),
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
                Row {
                    IconButton(onClick = {
                        navController.navigate(NavigationRoutes.PointsHistory.route)
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = stringResource(R.string.home_history),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        if (!isAdmin) {
            // QR-код для пользователей
            var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var isQrLoading by remember { mutableStateOf(true) }

            LaunchedEffect(userId) {
                if (userId.isNotBlank()) {
                    try {
                        qrCodeBitmap = QrCodeGenerator.generateQrCode(userId, 256, 256)
                    } finally {
                        isQrLoading = false
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.home_your_qr),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { navController.navigate(NavigationRoutes.QrCodeFullscreen.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isQrLoading) {
                            CircularProgressIndicator()
                        } else if (qrCodeBitmap != null) {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = stringResource(R.string.home_your_qr),
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        
    }}


@Preview
@Composable
fun HomeScreen(){
    Text(text = "tesy)
}