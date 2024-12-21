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
import com.example.loyalisttest.models.Cafe
import com.example.loyalisttest.models.UserRole
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var scaleSize by remember { mutableStateOf("10") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedCafe by remember { mutableStateOf<Cafe?>(null) }
    var cafes by remember { mutableStateOf<List<Cafe>>(emptyList()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var userRole by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Загружаем кафе в зависимости от роли пользователя
    LaunchedEffect(currentUser) {
        try {
            isLoading = true
            if (currentUser != null) {
                // Получаем роль пользователя
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                userRole = userDoc.getString("role")

                // В зависимости от роли загружаем кафе
                val cafesQuery = when (userRole) {
                    UserRole.SUPER_ADMIN.name -> {
                        // Супер-админ видит все активные кафе
                        firestore.collection("cafes")
                            .whereEqualTo("active", true)
                    }
                    UserRole.ADMIN.name -> {
                        // Админ видит только свои кафе
                        val managedCafes = userDoc.get("managedCafes") as? List<String> ?: emptyList()
                        if (managedCafes.isEmpty()) {
                            error = "У вас нет прикрепленных кафе"
                            return@LaunchedEffect
                        }
                        firestore.collection("cafes")
                            .whereEqualTo("active", true)
                            .whereIn("id", managedCafes)
                    }
                    else -> {
                        error = "Недостаточно прав для добавления товаров"
                        return@LaunchedEffect
                    }
                }

                val snapshot = cafesQuery.get().await()
                cafes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Cafe::class.java)?.copy(id = doc.id)
                }

                if (cafes.isEmpty()) {
                    error = "Нет доступных кафе"
                }
            }
        } catch (e: Exception) {
            error = "Ошибка загрузки списка кафе: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun validateInputs(): Boolean {
        return when {
            selectedCafe == null -> {
                error = "Выберите кафе"
                false
            }
            name.isBlank() -> {
                error = "Введите название товара"
                false
            }
            description.isBlank() -> {
                error = "Введите описание"
                false
            }
            scaleSize.toIntOrNull() == null || scaleSize.toInt() < 1 -> {
                error = "Количество делений должно быть больше 0"
                false
            }
            price.isBlank() || price.toDoubleOrNull() == null -> {
                error = "Введите корректную цену"
                false
            }
            else -> true
        }
    }

    fun addProduct() {
        if (!validateInputs()) return

        isLoading = true

        val productData = hashMapOf<String, Any>( // Явно указываем тип
            "name" to name.trim(),
            "description" to description.trim(),
            "scaleSize" to (scaleSize.toIntOrNull() ?: 10),
            "price" to (price.toDoubleOrNull() ?: 0.0),
            "cafeId" to (selectedCafe?.id ?: ""),
            "active" to true,
            "createdAt" to System.currentTimeMillis(),
            "createdBy" to (currentUser?.uid ?: ""),
            "category" to "",
            "imageUrl" to ""
        )

        // И изменим сигнатуру внутренней функции:
        fun addProductToFirestore(data: Map<String, Any>) {
            firestore.collection("products")
                .add(data)
                .addOnSuccessListener { docRef ->
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

        // Проверяем права на добавление товара
        when (userRole) {
            UserRole.SUPER_ADMIN.name -> {
                // Супер-админ может добавлять товары в любое кафе
                addProductToFirestore(productData)
            }
            UserRole.ADMIN.name -> {
                // Проверяем, принадлежит ли кафе админу
                currentUser?.let { user ->
                    firestore.collection("users")
                        .document(user.uid)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            val managedCafes = userDoc.get("managedCafes") as? List<String> ?: emptyList()
                            if (managedCafes.contains(selectedCafe?.id)) {
                                addProductToFirestore(productData)
                            } else {
                                error = "У вас нет прав на добавление товаров в это кафе"
                                isLoading = false
                            }
                        }
                        .addOnFailureListener { e ->
                            error = "Ошибка проверки прав: ${e.message}"
                            isLoading = false
                        }
                }
            }
            else -> {
                error = "Недостаточно прав для добавления товаров"
                isLoading = false
            }
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

            // Название товара
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название товара") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            // Описание
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isLoading
            )

            // Количество делений шкалы
            OutlinedTextField(
                value = scaleSize,
                onValueChange = { scaleSize = it.filter { char -> char.isDigit() } },
                label = { Text("Количество делений для награды") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading,
                supportingText = { Text("Сколько покупок нужно для получения награды") }
            )

            // Цена
            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text("Цена") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !isLoading
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

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