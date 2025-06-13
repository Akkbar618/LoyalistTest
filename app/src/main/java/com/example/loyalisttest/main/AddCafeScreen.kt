package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.example.loyalisttest.R
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
    val coroutineScope = rememberCoroutineScope()
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Check access rights when loading screen
    LaunchedEffect(currentUser) {
        try {
            if (currentUser == null) {
                error = context.getString(R.string.error_auth_required)
                return@LaunchedEffect
            }

            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            isSuperAdmin = userDoc.getString("role") == UserRole.SUPER_ADMIN.name
            if (!isSuperAdmin) {
                error = context.getString(R.string.error_insufficient_rights_cafe)
            }
        } catch (e: Exception) {
            error = context.getString(R.string.error_checking_rights, e.message ?: "")
        } finally {
            isLoading = false
        }
    }

    fun validateInputs(): Boolean {
        return when {
            name.isBlank() -> {
                error = context.getString(R.string.error_enter_cafe_name)
                false
            }
            description.isBlank() -> {
                error = context.getString(R.string.error_enter_description)
                false
            }
            else -> true
        }
    }

    fun addCafe() {
        if (!validateInputs()) return
        if (!isSuperAdmin) {
            error = context.getString(R.string.error_insufficient_rights_cafe)
            return
        }
        if (currentUser == null) {
            error = context.getString(R.string.error_auth_required)
            return
        }

        coroutineScope.launch {
            isLoading = true
            val cafeData = hashMapOf(
                "name" to name.trim(),
                "description" to description.trim(),
                "category" to selectedCategory.name,
                "active" to true,
                "createdAt" to System.currentTimeMillis(),
                "createdBy" to currentUser.uid
            )

            try {
                val docRef = firestore.collection("cafes").add(cafeData).await()
                docRef.update("id", docRef.id).await()
                Toast.makeText(context, context.getString(R.string.success_add_cafe), Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } catch (e: Exception) {
                error = context.getString(R.string.error_adding_cafe, e.message ?: "")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_cafe)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
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
                    text = stringResource(R.string.error_insufficient_rights_cafe),
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .widthIn(max = if (isLandscape) 600.dp else Dp.Unspecified),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = if (isLandscape) Alignment.CenterHorizontally else Alignment.Start
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.cafe_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.cafe_description)) },
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
                        label = { Text(stringResource(R.string.cafe_category)) },
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
                        Text(stringResource(R.string.add_cafe))
                    }
                }
            }
        }
    }
}