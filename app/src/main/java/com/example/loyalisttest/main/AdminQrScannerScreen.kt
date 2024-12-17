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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AdminQrScannerScreen(
    navController: NavHostController,
    pointsToAdd: Int = 10 // По умолчанию начисляем 10 баллов
) {
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

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
                        // Начисляем баллы пользователю
                        firestore.collection("users").document(scannedUserId)
                            .get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val currentPoints = userDoc.getLong("points")?.toInt() ?: 0

                                    firestore.collection("users").document(scannedUserId)
                                        .update("points", currentPoints + pointsToAdd)
                                        .addOnSuccessListener {
                                            // Создаем запись в истории начисления баллов
                                            val historyRecord = hashMapOf(
                                                "userId" to scannedUserId,
                                                "adminId" to admin.uid,
                                                "pointsAdded" to pointsToAdd,
                                                "timestamp" to System.currentTimeMillis()
                                            )

                                            firestore.collection("pointsHistory")
                                                .add(historyRecord)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        context,
                                                        "Начислено $pointsToAdd баллов!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    isLoading = false
                                                    navController.popBackStack()
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = "Ошибка при сохранении истории: ${e.message}"
                                                    isLoading = false
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Ошибка при начислении баллов: ${e.message}"
                                            isLoading = false
                                        }
                                } else {
                                    errorMessage = "Пользователь не найден"
                                    isLoading = false
                                }
                            }
                            .addOnFailureListener { e ->
                                errorMessage = "Ошибка при поиске пользователя: ${e.message}"
                                isLoading = false
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