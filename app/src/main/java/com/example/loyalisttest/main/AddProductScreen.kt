package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.loyalisttest.R
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
    val coroutineScope = rememberCoroutineScope()

    // Load cafes based on user role
    LaunchedEffect(currentUser) {
        try {
            isLoading = true
            if (currentUser != null) {
                // Get user role
                val userDoc = firestore.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                userRole = userDoc.getString("role")

                // Load cafes based on role
                val cafesQuery = when (userRole) {
                    UserRole.SUPER_ADMIN.name -> {
                        // Super admin sees all active cafes
                        firestore.collection("cafes")
                            .whereEqualTo("active", true)
                    }
                    UserRole.ADMIN.name -> {
                        // Admin sees only their cafes
                        val managedCafes = userDoc.get("managedCafes") as? List<String> ?: emptyList()
                        if (managedCafes.isEmpty()) {
                            error = context.getString(R.string.error_no_managed_cafes)
                            return@LaunchedEffect
                        }
                        firestore.collection("cafes")
                            .whereEqualTo("active", true)
                            .whereIn("id", managedCafes)
                    }
                    else -> {
                        error = context.getString(R.string.error_insufficient_rights_product)
                        return@LaunchedEffect
                    }
                }

                val snapshot = cafesQuery.get().await()
                cafes = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Cafe::class.java)?.copy(id = doc.id)
                }

                if (cafes.isEmpty()) {
                    error = context.getString(R.string.error_no_cafes)
                }
            }
        } catch (e: Exception) {
            error = context.getString(R.string.error_loading_cafes, e.message ?: "")
        } finally {
            isLoading = false
        }
    }

    fun validateInputs(): Boolean {
        return when {
            selectedCafe == null -> {
                error = context.getString(R.string.error_select_cafe)
                false
            }
            name.isBlank() -> {
                error = context.getString(R.string.error_enter_product_name)
                false
            }
            description.isBlank() -> {
                error = context.getString(R.string.error_enter_description)
                false
            }
            scaleSize.toIntOrNull() == null || scaleSize.toInt() < 1 -> {
                error = context.getString(R.string.error_scale_size)
                false
            }
            price.isBlank() || price.toDoubleOrNull() == null -> {
                error = context.getString(R.string.error_enter_valid_price)
                false
            }
            else -> true
        }
    }

    fun addProduct() {
        if (!validateInputs()) return

        coroutineScope.launch {
            isLoading = true

            val productData = hashMapOf<String, Any>(
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

            suspend fun addProductToFirestore(data: Map<String, Any>) {
                val docRef = firestore.collection("products").add(data).await()
                docRef.update("id", docRef.id).await()
                Toast.makeText(context, context.getString(R.string.success_add_product), Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }

            try {
                when (userRole) {
                    UserRole.SUPER_ADMIN.name -> {
                        addProductToFirestore(productData)
                    }
                    UserRole.ADMIN.name -> {
                        currentUser?.let { user ->
                            val userDoc = firestore.collection("users").document(user.uid).get().await()
                            val managedCafes = userDoc.get("managedCafes") as? List<String> ?: emptyList()
                            if (managedCafes.contains(selectedCafe?.id)) {
                                addProductToFirestore(productData)
                            } else {
                                error = context.getString(R.string.error_no_rights_for_cafe)
                            }
                        } ?: run {
                            error = context.getString(R.string.error_auth_required)
                        }
                    }
                    else -> {
                        error = context.getString(R.string.error_insufficient_rights_product)
                    }
                }
            } catch (e: Exception) {
                error = context.getString(R.string.error_adding_product, e.message ?: "")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_product)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
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
            // Cafe selection
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCafe?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.select_cafe)) },
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

            // Product name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.product_name)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.product_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = !isLoading
            )

            // Scale size
            OutlinedTextField(
                value = scaleSize,
                onValueChange = { scaleSize = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.product_scale)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading,
                supportingText = { Text(stringResource(R.string.product_scale_hint)) }
            )

            // Price
            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { char -> char.isDigit() || char == '.' } },
                label = { Text(stringResource(R.string.product_price)) },
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
                    Text(stringResource(R.string.add_product))
                }
            }
        }
    }
}