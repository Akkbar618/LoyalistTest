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

    // Загрузка истории
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            errorMessage = "Пользователь не авторизован"
            isLoading = false
            return@LaunchedEffect
        }

        // Проверяем роль пользователя
        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { userDoc ->
                currentUserRole = userDoc.getString("role")

                // В зависимости от роли формируем запрос
                val query = when (currentUserRole) {
                    UserRole.SUPER_ADMIN.name -> {
                        // Супер-админ видит всю историю
                        firestore.collection("pointsHistory")
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                    }
                    UserRole.ADMIN.name -> {
                        // Админ видит историю своих кафе
                        val managedCafes = userDoc.get("managedCafes") as? List<String> ?: emptyList()
                        if (managedCafes.isEmpty()) {
                            errorMessage = "У вас нет прикрепленных кафе"
                            isLoading = false
                            return@addOnSuccessListener
                        }
                        firestore.collection("pointsHistory")
                            .whereIn("cafeId", managedCafes)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                    }
                    else -> {
                        // Обычный пользователь видит только свою историю
                        firestore.collection("pointsHistory")
                            .whereEqualTo("userId", currentUser.uid)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                    }
                }

                query.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        errorMessage = "Ошибка загрузки истории: ${e.message}"
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
                title = { Text("История прогресса") },
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

    // Загружаем дополнительную информацию
    LaunchedEffect(record) {
        // Загружаем имя пользователя
        firestore.collection("users")
            .document(record.userId)
            .get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("name") ?: "Неизвестный пользователь"
            }

        // Если супер-админ, загружаем информацию о кафе и товаре
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
                        text = "Получена награда!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Прогресс: ${record.progress}",
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
                        text = "Пользователь: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                cafeName?.let {
                    Text(
                        text = "Кафе: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                productName?.let {
                    Text(
                        text = "Товар: $it",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}