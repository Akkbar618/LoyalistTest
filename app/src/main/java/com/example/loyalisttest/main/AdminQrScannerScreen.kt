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

private const val POINTS_TO_REWARD = 100 // Значение по умолчанию

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AdminQrScannerScreen(
    navController: NavHostController,
    productId: String
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var product by remember { mutableStateOf<Product?>(null) }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Загружаем информацию о продукте
    LaunchedEffect(productId) {
        firestore.collection("products").document(productId)
            .get()
            .addOnSuccessListener { productDoc ->
                product = productDoc.toObject(Product::class.java)
            }
    }

    // Запрашиваем разрешение на использование камеры
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Настраиваем сканер QR-кода
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            navController.popBackStack()
            return@rememberLauncherForActivityResult
        }

        isLoading = true
        errorMessage = null
        val scannedUserId = result.contents

        // Проверяем права администратора
        currentUser?.let { admin ->
            firestore.collection("users").document(admin.uid)
                .get()
                .addOnSuccessListener { adminDoc ->
                    if (adminDoc.getString("role") == "ADMIN") {
                        product?.let { p ->
                            // Получаем текущие баллы пользователя для данного кафе
                            val userPointsRef = firestore.collection("userPoints")
                                .document("${scannedUserId}_${p.cafeId}")

                            firestore.runTransaction { transaction ->
                                val userPointsDoc = transaction.get(userPointsRef)
                                val currentPoints = userPointsDoc.getLong("currentPoints")?.toInt() ?: 0
                                val totalEarned = userPointsDoc.getLong("totalEarnedPoints")?.toInt() ?: 0
                                val rewardsReceived = userPointsDoc.getLong("rewardsReceived")?.toInt() ?: 0

                                val newPoints = currentPoints + p.points
                                val (pointsToSave, shouldReward) = if (newPoints >= POINTS_TO_REWARD) {
                                    Pair(0, true)
                                } else {
                                    Pair(newPoints, false)
                                }

                                val pointsData = hashMapOf(
                                    "userId" to scannedUserId,
                                    "cafeId" to p.cafeId,
                                    "currentPoints" to pointsToSave,
                                    "totalEarnedPoints" to (totalEarned + p.points),
                                    "rewardsReceived" to (rewardsReceived + if (shouldReward) 1 else 0),
                                    "lastUpdated" to System.currentTimeMillis()
                                )

                                transaction.set(userPointsRef, pointsData)

                                // Создаем запись в истории начисления
                                val transactionData = hashMapOf(
                                    "userId" to scannedUserId,
                                    "adminId" to admin.uid,
                                    "cafeId" to p.cafeId,
                                    "productId" to p.id,
                                    "points" to p.points,
                                    "description" to "Начисление за ${p.name}",
                                    "timestamp" to System.currentTimeMillis()
                                )

                                val transactionRef = firestore.collection("pointsTransactions").document()
                                transaction.set(transactionRef, transactionData)

                                shouldReward
                            }.addOnSuccessListener { shouldReward ->
                                isLoading = false
                                if (shouldReward) {
                                    Toast.makeText(
                                        context,
                                        "Поздравляем! Клиент получил награду!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                Toast.makeText(
                                    context,
                                    "Начислено ${p.points} баллов!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            }.addOnFailureListener { e ->
                                errorMessage = "Ошибка при начислении баллов: ${e.message}"
                                isLoading = false
                            }
                        }
                    } else {
                        errorMessage = "Недостаточно прав для начисления баллов"
                        isLoading = false
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage = "Ошибка проверки прав: ${e.message}"
                    isLoading = false
                }
        }
    }

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