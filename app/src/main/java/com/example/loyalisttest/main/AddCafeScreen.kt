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
import com.example.loyalisttest.models.Cafe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCafeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

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

        isLoading = true

        val cafeData = mapOf(
            "name" to name.trim(),
            "description" to description.trim(),
            "active" to true,
            "category" to "coffee",
            "createdAt" to System.currentTimeMillis(),
            "createdBy" to (currentUser?.uid ?: "")
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