package com.example.loyalisttest.main

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.loyalisttest.navigation.NavigationRoutes
import com.example.loyalisttest.utils.QrCodeGenerator
import com.google.firebase.auth.FirebaseAuth

data class Promotion(
    val id: String,
    val title: String,
    val discount: String,
    val imageUrl: String
)

data class Recommendation(
    val id: String,
    val name: String,
    val address: String,
    val imageUrl: String,
    val rating: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val userName = remember { currentUser?.displayName ?: "Пользователь" }
    val userId = currentUser?.uid ?: ""

    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Пример данных для промо и рекомендаций
    val promotions = remember {
        listOf(
            Promotion("1", "Сидите дома", "-15%", "url_to_image"),
            Promotion("2", "-17% на пиццу", "-17%", "url_to_image")
        )
    }

    val recommendations = remember {
        listOf(
            Recommendation("1", "БабаГриль", "ул. Ленинская, 28", "url_to_image", 4.8f),
            Recommendation("2", "DonerDon", "ул. Дубова, 13", "url_to_image", 4.7f)
        )
    }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            try {
                qrCodeBitmap = QrCodeGenerator.generateQrCode(userId, 256, 256)
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        // Верхняя панель с приветствием и уведомлениями
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Здесь можно добавить аватар пользователя если нужно
                    Text(
                        text = "С возвращением,\n$userName!",
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }
                IconButton(onClick = { /* Обработка уведомлений */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Уведомления",
                        tint = Color.White
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Мой QR-код",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable {
                            navController.navigate(NavigationRoutes.QrCodeFullscreen.route) {
                                launchSingleTop = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (qrCodeBitmap != null) {
                        Box {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                            // Кнопка полноэкранного режима
                            IconButton(
                                onClick = {
                                    navController.navigate(NavigationRoutes.QrCodeFullscreen.route) {
                                        launchSingleTop = true
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear, // Изменили иконку
                                    contentDescription = "Развернуть",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Горячие предложения
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Горячие предложения",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* Показать все предложения */ }) {
                    Text("Смотреть все")
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(promotions) { promotion ->
                    PromotionCard(promotion)
                }
            }
        }

        // Рекомендации
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Рекомендации",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* Показать все рекомендации */ }) {
                    Text("Смотреть все")
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recommendations) { recommendation ->
                    RecommendationCard(recommendation)
                }
            }
        }
    }
}

@Composable
fun PromotionCard(promotion: Promotion) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(150.dp)
            .clickable { /* Обработка нажатия */ },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Здесь должно быть изображение промо
            Text(
                text = promotion.title,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
            Text(
                text = promotion.discount,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun RecommendationCard(recommendation: Recommendation) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clickable { /* Обработка нажатия */ },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Здесь должно быть изображение заведения
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = recommendation.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = recommendation.address,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}