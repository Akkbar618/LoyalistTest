package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.loyalisttest.models.Product
import com.example.loyalisttest.models.Cafe
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCafe by remember { mutableStateOf<Cafe?>(null) }
    var cafes by remember { mutableStateOf<List<Cafe>>(emptyList()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Загружаем список кафе
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val snapshot = firestore.collection("cafes")
                .whereEqualTo("active", true)
                .get()
                .await()

            cafes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Cafe::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            error = "Ошибка загрузки списка кафе: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun addProduct() {
        if (selectedCafe == null) {
            error = "Выберите кафе"
            return
        }
        if (name.isBlank()) {
            error = "Введите название товара"
            return
        }
        if (description.isBlank()) {
            error = "Введите описание"
            return
        }
        if (points.isBlank() || points.toIntOrNull() == null) {
            error = "Введите корректное количество баллов"
            return
        }
        if (price.isBlank() || price.toDoubleOrNull() == null) {
            error = "Введите корректную цену"
            return
        }

        isLoading = true

        // Создаем данные продукта в правильном формате для Firestore
        val productData = mapOf(
            "name" to name.trim(),
            "description" to description.trim(),
            "points" to (points.toIntOrNull() ?: 0),
            "price" to (price.toDoubleOrNull() ?: 0.0),
            "cafeId" to (selectedCafe?.id ?: ""),
            "active" to true,
            "createdAt" to System.currentTimeMillis(),
            "createdBy" to (currentUser?.uid ?: ""),
            "category" to "",
            "imageUrl" to ""
        )

        firestore.collection("products")
            .add(productData)
            .addOnSuccessListener { docRef ->
                // Обновляем ID документа
                docRef.update(mapOf("id" to docRef.id))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Товар успешно добавлен", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        error = "Ошибка обновления ID товара: ${e.message}"
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                error = "Ошибка при добавлении товара: ${e.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить товар") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Выбор кафе
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCafe?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Выберите кафе") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) }
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    cafes.forEach { cafe ->
                        DropdownMenuItem(
                            text = { Text(cafe.name) },
                            onClick = {
                                selectedCafe = cafe
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название товара") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isLoading
            )

            OutlinedTextField(
                value = points,
                onValueChange = { points = it.filter { char -> char.isDigit() } },
                label = { Text("Баллы за покупку") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text("Цена") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !isLoading
            )

            Button(
                onClick = { addProduct() },
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
    }
}