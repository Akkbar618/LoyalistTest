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
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.example.loyalisttest.models.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    navController: NavHostController,
    productId: String
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var product by remember { mutableStateOf<Product?>(null) }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Обработчик результата сканирования
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            navController.popBackStack()
            return@rememberLauncherForActivityResult
        }

        isLoading = true
        errorMessage = null
        val scannedUserId = result.contents

        currentUser?.let { admin ->
            firestore.collection("users").document(admin.uid)
                .get()
                .addOnSuccessListener { adminDoc ->
                    if (adminDoc.getString("role") == "ADMIN") {
                        product?.let { p ->
                            val userPointsRef = firestore.collection("userPoints")
                                .document("${scannedUserId}_${p.cafeId}")

                            userPointsRef.get().addOnSuccessListener { pointsDoc ->
                                val currentPoints = pointsDoc.getLong("currentPoints")?.toInt() ?: 0
                                val totalEarned = pointsDoc.getLong("totalEarnedPoints")?.toInt() ?: 0

                                firestore.runTransaction { transaction ->
                                    // Обновляем или создаем документ с баллами
                                    val pointsData = hashMapOf(
                                        "userId" to scannedUserId,
                                        "cafeId" to p.cafeId,
                                        "currentPoints" to (currentPoints + p.points),
                                        "totalEarnedPoints" to (totalEarned + p.points),
                                        "lastUpdated" to System.currentTimeMillis()
                                    )

                                    if (pointsDoc.exists()) {
                                        transaction.update(userPointsRef,
                                            pointsData as Map<String, Any>
                                        )
                                    } else {
                                        transaction.set(userPointsRef, pointsData)
                                    }

                                    // Создаем запись в истории
                                    val historyRef = firestore.collection("pointsHistory").document()
                                    val historyData = hashMapOf(
                                        "id" to historyRef.id,
                                        "userId" to scannedUserId,
                                        "adminId" to admin.uid,
                                        "cafeId" to p.cafeId,
                                        "productId" to p.id,
                                        "pointsAdded" to p.points,
                                        "description" to "Начисление за ${p.name}",
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    transaction.set(historyRef, historyData)
                                }.addOnSuccessListener {
                                    isLoading = false
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
                            }.addOnFailureListener { e ->
                                errorMessage = "Ошибка при проверке баллов: ${e.message}"
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

    // Загружаем информацию о продукте
    LaunchedEffect(productId) {
        firestore.collection("products").document(productId)
            .get()
            .addOnSuccessListener { productDoc ->
                product = productDoc.toObject(Product::class.java)?.copy(id = productDoc.id)
            }
            .addOnFailureListener { e ->
                errorMessage = "Ошибка загрузки информации о товаре: ${e.message}"
            }
    }

    // Запрос разрешения камеры и UI остаются без изменений
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