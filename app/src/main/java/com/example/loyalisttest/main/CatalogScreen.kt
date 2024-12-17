package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.loyalisttest.models.PointsHistoryRecord
import com.example.loyalisttest.navigation.NavigationRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(navController: NavHostController) {
    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Проверяем, является ли пользователь администратором
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            firestore.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    isAdmin = document.getString("role") == "ADMIN"
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Каталог") },
                actions = {
                    if (isAdmin) {
                        // Кнопка сканирования QR-кода
                        IconButton(
                            onClick = {
                                navController.navigate(NavigationRoutes.AdminQrScanner.route)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Сканировать QR-код"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (isAdmin) {
                    AdminCatalogContent(navController)
                } else {
                    UserCatalogContent(navController)
                }
            }
        }
    }
}

@Composable
private fun AdminCatalogContent(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Секция управления баллами
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Управление баллами",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Button(
                    onClick = {
                        navController.navigate("main/admin/qr_scanner")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Сканировать QR-код пользователя")
                }
            }
        }

        // История начисления баллов
        var pointsHistory by remember { mutableStateOf<List<PointsHistoryRecord>>(emptyList()) }
        var isHistoryLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            FirebaseFirestore.getInstance()
                .collection("pointsHistory")
                .orderBy("timestamp")
                .limit(10)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    pointsHistory = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(PointsHistoryRecord::class.java)
                    } ?: emptyList()

                    isHistoryLoading = false
                }
        }

        Text(
            text = "История начисления баллов",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isHistoryLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pointsHistory) { record ->
                    HistoryCard(record)
                }
            }
        }
    }
}

@Composable
private fun UserCatalogContent(navController: NavHostController) {
    // Контент для обычных пользователей
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Каталог товаров",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Здесь можно добавить список товаров или другой контент для пользователей
    }
}

@Composable
private fun HistoryCard(record: PointsHistoryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Начислено баллов: ${record.pointsAdded}",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Пользователь: ${record.userId}",
                fontSize = 14.sp
            )
            Text(
                text = "Дата: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(record.timestamp)}",
                fontSize = 12.sp
            )
        }
    }
}