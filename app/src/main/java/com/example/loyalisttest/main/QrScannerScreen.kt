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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    navController: NavHostController,
    productId: String
) {
    val context = LocalContext.current
    val currentUser = remember { FirebaseAuth.getInstance().currentUser }
        ?: run {
            LaunchedEffect(Unit) {
                navController.popBackStack()
            }
            return
        }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    // Запрашиваем разрешение на использование камеры
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            navController.popBackStack()
            return@rememberLauncherForActivityResult
        }

        isLoading = true
        error = null

        val db = FirebaseFirestore.getInstance()
        db.collection("products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val expectedQrCode = document.getString("qrCode")
                    val points = document.getLong("points")?.toInt() ?: 0

                    if (expectedQrCode == result.contents) {
                        val userRef = db.collection("users").document(currentUser.uid)
                        db.runTransaction { transaction ->
                            val userSnapshot = transaction.get(userRef)
                            val currentPoints = userSnapshot.getLong("points")?.toInt() ?: 0
                            transaction.update(userRef, "points", currentPoints + points)
                        }.addOnSuccessListener {
                            success = true
                            isLoading = false
                            Toast.makeText(
                                context,
                                "Начислено $points баллов!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.popBackStack()
                        }.addOnFailureListener {
                            error = "Ошибка при начислении баллов"
                            isLoading = false
                        }
                    } else {
                        error = "Неверный QR-код"
                        isLoading = false
                    }
                } else {
                    error = "Товар не найден"
                    isLoading = false
                }
            }
            .addOnFailureListener {
                error = "Ошибка при проверке QR-кода"
                isLoading = false
            }
    }

    fun launchScanner() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("Отсканируйте QR-код товара")
            .setBeepEnabled(false)
            .setCameraId(0)
        scanLauncher.launch(options)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            !cameraPermissionState.status.isGranted -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        if (cameraPermissionState.status.shouldShowRationale) {
                            "Для сканирования QR-кода необходим доступ к камере"
                        } else {
                            "Для работы приложения требуется доступ к камере"
                        }
                    )
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Предоставить доступ")
                    }
                }
            }
            isLoading -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(error!!)
                    Button(
                        onClick = {
                            error = null
                            launchScanner()
                        }
                    ) {
                        Text("Попробовать снова")
                    }
                }
            }
            else -> {
                LaunchedEffect(cameraPermissionState.status.isGranted) {
                    if (cameraPermissionState.status.isGranted) {
                        launchScanner()
                    }
                }
            }
        }
    }
}