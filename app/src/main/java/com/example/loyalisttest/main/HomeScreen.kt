package com.example.loyalisttest.main.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.loyalisttest.R
import com.example.loyalisttest.navigation.NavigationRoutes
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
    val userName = remember { currentUser?.displayName ?: "Пользователь" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        // Верхняя панель с приветствием и уведомлениями
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color.Black,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "С возвращением,\n$userName!",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Уведомления",
                        tint = Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // QR код секция
            Text(
                text = "Мой QR-код",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qr_code),
                    contentDescription = "QR код",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Info, "Яркость")
                    }

                    IconButton(
                        onClick = {
                            navController.navigate(NavigationRoutes.QrCodeFullscreen.route)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Search, "На весь экран")
                    }
                }
            }
            // Горячие предложения
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Горячие Предложения",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { }) {
                    Text("Смотреть все")
                }
            }

            // Здесь будут карточки горячих предложений
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Контент карточки
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Контент карточки
                }
            }

            // Рекомендации
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Рекомендации",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { }) {
                    Text("Смотреть все")
                }
            }

            // Здесь будут карточки рекомендаций
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Контент карточки
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Контент карточки
                }
            }
        }
    }
}