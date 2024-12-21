package com.example.loyalisttest.main

import android.Manifest
import android.util.Log
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    navController: NavHostController,
    productId: String
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var product by remember { mutableStateOf<Product?>(null) }
    var userRole by remember { mutableStateOf<String?>(null) }
    var managedCafes by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val TAG = "QrScannerScreen"

    // Загружаем информацию о продукте и правах пользователя
    LaunchedEffect(currentUser, productId) {
        try {
            if (currentUser == null) {
                errorMessage = "Необходима авторизация"
                return@LaunchedEffect
            }

            // Получаем информацию о пользователе
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            userRole = userDoc.getString("role")
            managedCafes = (userDoc.get("managedCafes") as? List<String>) ?: emptyList()

            Log.d(TAG, "User role: $userRole")
            Log.d(TAG, "Managed cafes: $managedCafes")

            // Получаем информацию о продукте
            val productDoc = firestore.collection("products")
                .document(productId)
                .get()
                .await()

            product = productDoc.toObject(Product::class.java)?.copy(id = productId)
            Log.d(TAG, "Product loaded: ${product?.cafeId}")

            // Проверяем права доступа
            when (userRole) {
                "SUPER_ADMIN" -> {
                    // Супер-админ имеет доступ ко всем кафе
                    Log.d(TAG, "User is SUPER_ADMIN")
                }
                "ADMIN" -> {
                    // Проверяем, есть ли у админа доступ к кафе
                    if (!managedCafes.contains(product?.cafeId)) {
                        errorMessage = "У вас нет прав на управление этим кафе"
                        Log.d(TAG, "Admin doesn't have access to cafe ${product?.cafeId}")
                    }
                }
                else -> {
                    errorMessage = "Недостаточно прав для сканирования"
                    Log.d(TAG, "User has insufficient rights: $userRole")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading data", e)
            errorMessage = "Ошибка загрузки данных: ${e.message}"
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
        Log.d(TAG, "Scanned user ID: $scannedUserId")

        product?.let { p ->
            // Проверяем права на начисление баллов
            if (userRole != "SUPER_ADMIN" && userRole != "ADMIN") {
                errorMessage = "Недостаточно прав для начисления баллов"
                isLoading = false
                return@let
            }

            if (userRole == "ADMIN" && !managedCafes.contains(p.cafeId)) {
                errorMessage = "У вас нет прав на управление этим кафе"
                isLoading = false
                return@let
            }

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
                Log.e(TAG, "Error updating progress", e)
                errorMessage = "Ошибка при обновлении прогресса: ${e.message}"
                isLoading = false
            }
        }
    }

    // Запрос разрешения камеры
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("Отсканируйте QR-код клиента")
                setBeepEnabled(false)
                setCameraId(0)
            }
            scanLauncher.launch(options)
        }
    }

    // UI для отображения состояния и ошибок
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
                                val options = ScanOptions().apply {
                                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                    setPrompt("Отсканируйте QR-код клиента")
                                    setBeepEnabled(false)
                                    setCameraId(0)
                                }
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