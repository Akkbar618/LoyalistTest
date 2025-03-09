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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.example.loyalisttest.R
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

    // Load product info and user permissions
    LaunchedEffect(currentUser, productId) {
        try {
            if (currentUser == null) {
                errorMessage = context.getString(R.string.error_auth_required)
                return@LaunchedEffect
            }

            // Get user info
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            userRole = userDoc.getString("role")
            managedCafes = (userDoc.get("managedCafes") as? List<String>) ?: emptyList()

            Log.d(TAG, "User role: $userRole")
            Log.d(TAG, "Managed cafes: $managedCafes")

            // Get product info
            val productDoc = firestore.collection("products")
                .document(productId)
                .get()
                .await()

            product = productDoc.toObject(Product::class.java)?.copy(id = productId)
            Log.d(TAG, "Product loaded: ${product?.cafeId}")

            // Check access rights
            when (userRole) {
                "SUPER_ADMIN" -> {
                    // Super admin has access to all cafes
                    Log.d(TAG, "User is SUPER_ADMIN")
                }
                "ADMIN" -> {
                    // Check if admin has access to cafe
                    if (!managedCafes.contains(product?.cafeId)) {
                        errorMessage = context.getString(R.string.error_no_cafe_management_rights)
                        Log.d(TAG, "Admin doesn't have access to cafe ${product?.cafeId}")
                    }
                }
                else -> {
                    errorMessage = context.getString(R.string.error_insufficient_rights_scan)
                    Log.d(TAG, "User has insufficient rights: $userRole")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading data", e)
            errorMessage = context.getString(R.string.error_loading_data, e.message ?: "")
        }
    }

    // Scan result handler
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
            // Check permissions for adding points
            if (userRole != "SUPER_ADMIN" && userRole != "ADMIN") {
                errorMessage = context.getString(R.string.error_insufficient_rights_points)
                isLoading = false
                return@let
            }

            if (userRole == "ADMIN" && !managedCafes.contains(p.cafeId)) {
                errorMessage = context.getString(R.string.error_no_cafe_management_rights)
                isLoading = false
                return@let
            }

            // Get or create user progress document
            val userPointsRef = firestore.collection("userPoints")
                .document("${scannedUserId}_${p.cafeId}_${p.id}")

            firestore.runTransaction { transaction ->
                val userPointsDoc = transaction.get(userPointsRef)
                val currentProgress = userPointsDoc.getLong("currentProgress")?.toInt() ?: 0
                val totalScans = userPointsDoc.getLong("totalScans")?.toInt() ?: 0
                val rewardsReceived = userPointsDoc.getLong("rewardsReceived")?.toInt() ?: 0

                // Increase progress
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

                // Create history record
                val historyRef = firestore.collection("pointsHistory").document()
                val historyData = hashMapOf(
                    "id" to historyRef.id,
                    "userId" to scannedUserId,
                    "adminId" to currentUser?.uid,
                    "cafeId" to p.cafeId,
                    "productId" to p.id,
                    "description" to context.getString(R.string.mark_for_product, p.name),
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
                        context.getString(R.string.congratulations),
                        Toast.LENGTH_LONG
                    ).show()
                }
                Toast.makeText(
                    context,
                    context.getString(R.string.progress_updated),
                    Toast.LENGTH_SHORT
                ).show()
                navController.popBackStack()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error updating progress", e)
                errorMessage = context.getString(R.string.error_updating_progress, e.message ?: "")
                isLoading = false
            }
        }
    }

    // Request camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt(context.getString(R.string.scan_qr_prompt))
                setBeepEnabled(false)
                setCameraId(0)
            }
            scanLauncher.launch(options)
        }
    }

    // UI for status and errors
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
                                    setPrompt(context.getString(R.string.scan_qr_prompt))
                                    setBeepEnabled(false)
                                    setCameraId(0)
                                }
                                scanLauncher.launch(options)
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Text(stringResource(R.string.try_again))
                    }
                }
            }
            !cameraPermissionState.status.isGranted -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(R.string.scan_camera_permission))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() }
                    ) {
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
        }
    }
}