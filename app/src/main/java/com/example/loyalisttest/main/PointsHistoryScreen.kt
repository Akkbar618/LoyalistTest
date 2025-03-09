package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.loyalisttest.R
import com.example.loyalisttest.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsHistoryScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(true) }
    var historyRecords by remember { mutableStateOf<List<PointsHistoryRecord>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentUserRole by remember { mutableStateOf<String?>(null) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Load history
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            errorMessage = context.getString(R.string.error_user_not_authenticated)
            isLoading = false
            return@LaunchedEffect
        }

        // Check user role
        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                currentUserRole = userDoc.getString("role")

                // Form query based on role
                val query = when (currentUserRole) {
                    UserRole.SUPER_ADMIN.name -> {
                        // Super admin sees all history
                        firestore.collection("pointsHistory")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                    }
                    UserRole.ADMIN.name -> {
                        // Admin sees history of their cafes
                        val managedCafes = userDoc.get("managedCafes") as? List<String> ?: emptyList()
                        if (managedCafes.isEmpty()) {
                            errorMessage = context.getString(R.string.error_no_managed_cafes)
                            isLoading = false
                            return@addOnSuccessListener
                        }
                        firestore.collection("pointsHistory")
                            .whereIn("cafeId", managedCafes)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                    }
                    else -> {
                        // Regular user sees only their history
                        firestore.collection("pointsHistory")
                            .whereEqualTo("userId", currentUser.uid)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                    }
                }

                query.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        errorMessage = context.getString(R.string.error_loading_history, e.message ?: "")
                        isLoading = false
                        return@addSnapshotListener
                    }

                    historyRecords = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(PointsHistoryRecord::class.java)
                    } ?: emptyList()

                    isLoading = false
                }
            }
            .addOnFailureListener { e ->
                errorMessage = context.getString(R.string.error_checking_rights, e.message ?: "")
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                historyRecords.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.history_empty),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(historyRecords) { record ->
                            HistoryRecordCard(
                                record = record,
                                isSuperAdmin = currentUserRole == UserRole.SUPER_ADMIN.name
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRecordCard(
    record: PointsHistoryRecord,
    isSuperAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    var userName by remember { mutableStateOf<String?>(null) }
    var cafeName by remember { mutableStateOf<String?>(null) }
    var productName by remember { mutableStateOf<String?>(null) }

    val firestore = FirebaseFirestore.getInstance()
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    // Load additional information
    LaunchedEffect(record) {
        // Load user name
        firestore.collection("users")
            .document(record.userId)
            .get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("name") ?: context.getString(R.string.unknown_user)
            }

        // If super admin, load cafe and product info
        if (isSuperAdmin) {
            firestore.collection("cafes")
                .document(record.cafeId)
                .get()
                .addOnSuccessListener { doc ->
                    cafeName = doc.getString("name")
                }

            firestore.collection("products")
                .document(record.productId)
                .get()
                .addOnSuccessListener { doc ->
                    productName = doc.getString("name")
                }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (record.isReward) {
                    Text(
                        text = stringResource(R.string.reward_received),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = stringResource(R.string.progress_count, record.progress),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = dateFormatter.format(record.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = record.description,
                style = MaterialTheme.typography.bodyMedium
            )

            if (isSuperAdmin) {
                Spacer(modifier = Modifier.height(4.dp))

                userName?.let {
                    Text(
                        text = stringResource(R.string.history_user_label, it),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                cafeName?.let {
                    Text(
                        text = stringResource(R.string.history_cafe_label, it),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                productName?.let {
                    Text(
                        text = stringResource(R.string.history_product_label, it),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}