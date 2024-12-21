package com.example.loyalisttest.main

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.loyalisttest.components.ProgressScale
import com.example.loyalisttest.models.*
import com.example.loyalisttest.navigation.NavigationRoutes
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

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = remember { currentUser?.displayName ?: "Пользователь" }
    val userId = currentUser?.uid ?: ""
    val firestore = FirebaseFirestore.getInstance()

    LaunchedEffect(userId) {
        if (userId.isBlank()) {
            error = "Пользователь не авторизован"
            isLoading = false
            return@LaunchedEffect
        }

        // Слушаем изменения пользователя
        firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }

                isAdmin = snapshot?.getString("role") == "ADMIN"
            }

        // Слушаем изменения кафе
        firestore.collection("cafes")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }

                cafes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Cafe::class.java)?.let { cafe ->
                        doc.id to cafe.copy(id = doc.id)
                    }
                }?.toMap() ?: emptyMap()
            }

        // Загружаем все продукты
        firestore.collection("products")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }

                products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.let { product ->
                        doc.id to product.copy(id = doc.id)
                    }
                }?.toMap() ?: emptyMap()
            }

        // Слушаем изменения прогресса пользователя
        firestore.collection("userPoints")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    return@addSnapshotListener
                }

                userPointsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(UserPoints::class.java)?.copy(
                        cafeId = doc.getString("cafeId") ?: "",
                        productId = doc.getString("productId") ?: "",
                        userId = userId,
                        currentProgress = doc.getLong("currentProgress")?.toInt() ?: 0,
                        totalScans = doc.getLong("totalScans")?.toInt() ?: 0,
                        rewardsReceived = doc.getLong("rewardsReceived")?.toInt() ?: 0
                    )
                } ?: emptyList()

                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        // Верхняя панель
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Привет,\n$userName!",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
                Row {
                    IconButton(onClick = {
                        navController.navigate(NavigationRoutes.PointsHistory.route)
                    }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "История прогресса",
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

            QrCodeCard(
                qrCodeBitmap = qrCodeBitmap,
                isLoading = isQrLoading,
                onFullscreenClick = {
                    navController.navigate(NavigationRoutes.QrCodeFullscreen.route)
                }
            )
        }

        // Основной контент
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error ?: "Произошла ошибка",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            userPointsList.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "У вас пока нет прогресса",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = userPointsList.groupBy { it.cafeId }.entries.toList(),
                        key = { it.key }
                    ) { (cafeId, pointsList) ->
                        cafes[cafeId]?.let { cafe ->
                            CafeCard(
                                cafe = cafe,
                                pointsList = pointsList,
                                products = products,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QrCodeCard(
    qrCodeBitmap: Bitmap?,
    isLoading: Boolean,
    onFullscreenClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ваш QR-код",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onFullscreenClick),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (qrCodeBitmap != null) {
                    Image(
                        bitmap = qrCodeBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun CafeCard(
    cafe: Cafe,
    pointsList: List<UserPoints>,
    products: Map<String, Product>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = cafe.name,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            pointsList.forEach { points ->
                products[points.productId]?.let { product ->
                    Column(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ProgressScale(
                            currentProgress = points.currentProgress,
                            maxProgress = product.scaleSize
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${points.currentProgress} из ${product.scaleSize}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Получено наград: ${points.rewardsReceived}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Text(
                text = "Последнее обновление: ${formatDate(pointsList.maxOf { it.lastUpdated })}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}