package com.example.loyalisttest.main

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.example.loyalisttest.models.*
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AdminQrScannerScreen(
    navController: NavHostController,
    productId: String
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var product by remember { mutableStateOf<Product?>(null) }
    var userRole by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Проверяем роль и загружаем информацию о продукте
    LaunchedEffect(currentUser, productId) {
        try {
            if (currentUser == null) {
                errorMessage = "Необходима авторизация"
                return@LaunchedEffect
            }

            // Проверяем роль пользователя
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            userRole = userDoc.getString("role")
            if (userRole != UserRole.SUPER_ADMIN.name && userRole != UserRole.ADMIN.name) {
                errorMessage = "Недостаточно прав для сканирования"
                return@LaunchedEffect
            }

            // Загружаем продукт
            val productDoc = firestore.collection("products")
                .document(productId)
                .get()
                .await()

            product = productDoc.toObject(Product::class.java)?.copy(id = productId)

            // Проверяем права на управление кафе для админа
            if (userRole == UserRole.ADMIN.name) {
                val managedCafes = userDoc.get("managedCafes") as? List<String> ?: emptyList()
                if (!managedCafes.contains(product?.cafeId)) {
                    errorMessage = "У вас нет прав на управление этим кафе"
                    return@LaunchedEffect
                }
            }
        } catch (e: Exception) {
            errorMessage = "Ошибка: ${e.message}"
        }
    }

    // Обработчик результата сканирования
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            navController.popBackStack()
            return@rememberLauncherForActivityResult
        }

        isLoading = true
        errorMessage = null
        val scannedUserId = result.contents

        product?.let { p ->
            // Получаем или создаем документ с прогрессом пользователя
            val userPointsRef = firestore.collection("userPoints")
                .document("${scannedUserId}_${p.cafeId}_${p.id}")

            firestore.runTransaction { transaction ->
                val userPointsDoc = transaction.get(userPointsRef)
                val currentProgress = userPointsDoc.getLong("currentProgress")?.toInt() ?: 0
                val totalScans = userPointsDoc.getLong("totalScans")?.toInt() ?: 0
                val rewardsReceived = userPointsDoc.getLong("rewardsReceived")?.toInt() ?: 0

                // Увеличиваем прогресс
                val newProgress = currentProgress + 1
                val rewardAchieved = newProgress >= p.scaleSize
                val finalProgress = if (rewardAchieved) 0 else newProgress

                val pointsData = hashMapOf(
                    "userId" to scannedUserId,
                    "cafeId" to p.cafeId,
                    "productId" to p.id,
                    "currentProgress" to finalProgress,
                    "totalScans" to (totalScans + 1),
                    "rewardsReceived" to (rewardsReceived + (if (rewardAchieved) 1 else 0)),
                    "lastUpdated" to System.currentTimeMillis()
                )

                transaction.set(userPointsRef, pointsData)

                // Создаем запись в истории
                val historyRef = firestore.collection("pointsHistory").document()
                val historyData = hashMapOf(
                    "id" to historyRef.id,
                    "userId" to scannedUserId,
                    "adminId" to currentUser?.uid,
                    "cafeId" to p.cafeId,
                    "productId" to p.id,
                    "description" to "Отметка за ${p.name}",
                    "timestamp" to System.currentTimeMillis(),
                    "progress" to finalProgress,
                    "isReward" to rewardAchieved
                )
                transaction.set(historyRef, historyData)

                rewardAchieved
            }.addOnSuccessListener { rewardAchieved ->
                isLoading = false
                if (rewardAchieved) {
                    Toast.makeText(
                        context,
                        "Поздравляем! Клиент получил награду!",
                        Toast.LENGTH_LONG
                    ).show()
                }
                Toast.makeText(
                    context,
                    "Прогресс обновлен!",
                    Toast.LENGTH_SHORT
                ).show()
                navController.popBackStack()
            }.addOnFailureListener { e ->
                errorMessage = "Ошибка при обновлении прогресса: ${e.message}"
                isLoading = false
            }
        }
    }

    // Запрашиваем разрешение на использование камеры
    @OptIn(ExperimentalPermissionsApi::class)
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Отсканируйте QR-код пользователя")
                .setBeepEnabled(false)
                .setCameraId(0)
            scanLauncher.launch(options)
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            errorMessage != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(errorMessage!!)
                    Button(
                        onClick = {
                            if (cameraPermissionState.status.isGranted) {
                                errorMessage = null
                                val options = ScanOptions()
                                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                    .setPrompt("Отсканируйте QR-код пользователя")
                                    .setBeepEnabled(false)
                                    .setCameraId(0)
                                scanLauncher.launch(options)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Text("Попробовать снова")
                    }
                }
            }
            !cameraPermissionState.status.isGranted -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Для сканирования QR-кода необходим доступ к камере")
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() }
                    ) {
                        Text("Предоставить доступ")
                    }
                }
            }
        }
    }
}