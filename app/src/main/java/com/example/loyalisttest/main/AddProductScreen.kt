package com.example.loyalisttest.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavHostController) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Проверяем, были ли внесены изменения
    val hasChanges = name.isNotEmpty() || description.isNotEmpty() || points.isNotEmpty()

    // Обработка кнопки "назад"
    BackHandler(enabled = hasChanges) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Отменить добавление товара?") },
            text = { Text("Несохраненные изменения будут потеряны") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    navController.popBackStack()
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Нет")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить товар") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (hasChanges) {
                                showExitDialog = true
                            } else {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= 50) name = it
                    },
                    label = { Text("Название товара") },
                    supportingText = { Text("${name.length}/50") },
                    isError = error != null && name.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        if (it.length <= 200) description = it
                    },
                    label = { Text("Описание") },
                    supportingText = { Text("${description.length}/200") },
                    isError = error != null && description.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = points,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }
                        if (filtered.isEmpty() || (filtered.toIntOrNull() ?: 0) <= 1000) {
                            points = filtered
                        }
                    },
                    label = { Text("Количество баллов") },
                    supportingText = { Text("Максимум 1000 баллов") },
                    isError = error != null && (points.isBlank() || points.toIntOrNull() == 0),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        when {
                            name.isBlank() -> error = "Введите название товара"
                            description.isBlank() -> error = "Введите описание товара"
                            points.isBlank() || points.toIntOrNull() == 0 ->
                                error = "Введите количество баллов больше 0"
                            else -> {
                                error = null
                                isLoading = true
                                addProduct(
                                    name = name,
                                    description = description,
                                    points = points.toInt(),
                                    onSuccess = { navController.popBackStack() },
                                    onError = {
                                        error = it
                                        isLoading = false
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Добавить товар")
                    }
                }
            }

            // Полноэкранный индикатор загрузки
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun addProduct(
    name: String,
    description: String,
    points: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val productData = hashMapOf(
        "name" to name,
        "description" to description,
        "points" to points,
        "qrCode" to UUID.randomUUID().toString(),
        "createdAt" to com.google.firebase.Timestamp.now()
    )

    db.collection("products")
        .add(productData)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener {
            onError(it.message ?: "Ошибка при добавлении товара")
        }
}