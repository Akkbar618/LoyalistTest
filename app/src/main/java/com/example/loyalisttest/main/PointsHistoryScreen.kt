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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.loyalisttest.models.PointsHistoryRecord
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

    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Загрузка истории начисления баллов
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            errorMessage = "Пользователь не авторизован"
            isLoading = false
            return@LaunchedEffect
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                val isAdmin = userDoc.getString("role") == "ADMIN"

                // В зависимости от роли пользователя показываем разную историю
                val query = if (isAdmin) {
                    firestore.collection("pointsHistory")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(100)
                } else {
                    firestore.collection("pointsHistory")
                        .whereEqualTo("userId", currentUser.uid)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(50)
                }

                query.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        errorMessage = "Ошибка загрузки истории: ${error.message}"
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
                errorMessage = "Ошибка проверки прав: ${e.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История начисления баллов") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
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
                        text = "История пуста",
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
                            HistoryRecordCard(record)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryRecordCard(record: PointsHistoryRecord) {
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    var userName by remember { mutableStateOf<String?>(null) }
    val firestore = FirebaseFirestore.getInstance()

    // Загружаем имя пользователя
    LaunchedEffect(record.userId) {
        firestore.collection("users")
            .document(record.userId)
            .get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("name") ?: "Неизвестный пользователь"
            }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+${record.pointsAdded} баллов",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormatter.format(record.timestamp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            userName?.let { name ->
                Text(
                    text = "Пользователь: $name",
                    fontSize = 14.sp
                )
            }

            if (record.description.isNotEmpty()) {
                Text(
                    text = record.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}