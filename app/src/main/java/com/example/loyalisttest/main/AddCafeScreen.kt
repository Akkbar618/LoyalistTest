package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.loyalisttest.models.CafeCategory
import com.example.loyalisttest.models.UserRole
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCafeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CafeCategory.COFFEE_SHOP) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var isSuperAdmin by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Проверяем права доступа при загрузке экрана
    LaunchedEffect(currentUser) {
        try {
            if (currentUser == null) {
                error = "Необходима авторизация"
                return@LaunchedEffect
            }

            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            isSuperAdmin = userDoc.getString("role") == UserRole.SUPER_ADMIN.name
            if (!isSuperAdmin) {
                error = "Недостаточно прав для создания кафе"
            }
        } catch (e: Exception) {
            error = "Ошибка проверки прав: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun validateInputs(): Boolean {
        return when {
            name.isBlank() -> {
                error = "Введите название кафе"
                false
            }
            description.isBlank() -> {
                error = "Введите описание"
                false
            }
            else -> true
        }
    }

    fun addCafe() {
        if (!validateInputs()) return
        if (!isSuperAdmin) {
            error = "Недостаточно прав для создания кафе"
            return
        }
        if (currentUser == null) {
            error = "Необходима авторизация"
            return
        }

        isLoading = true

        val cafeData = hashMapOf(
            "name" to name.trim(),
            "description" to description.trim(),
            "category" to selectedCategory.name,
            "active" to true,
            "createdAt" to System.currentTimeMillis(),
            "createdBy" to currentUser.uid
        )

        firestore.collection("cafes")
            .add(cafeData)
            .addOnSuccessListener { docRef ->
                docRef.update(mapOf("id" to docRef.id))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Кафе успешно добавлено", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        error = "Ошибка обновления ID кафе: ${e.message}"
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                error = "Ошибка при добавлении кафе: ${e.message}"
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить кафе") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!isSuperAdmin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text = "Недостаточно прав для создания кафе",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название кафе") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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

                ExposedDropdownMenuBox(
                    expanded = isCategoryDropdownExpanded,
                    onExpandedChange = { isCategoryDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Категория заведения") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                        enabled = !isLoading
                    )

                    ExposedDropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        CafeCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Button(
                    onClick = { addCafe() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Добавить кафе")
                    }
                }
            }
        }
    }
}