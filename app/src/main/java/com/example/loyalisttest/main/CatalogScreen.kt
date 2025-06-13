package com.example.loyalisttest.main

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.loyalisttest.R
import com.example.loyalisttest.models.*
import com.example.loyalisttest.navigation.NavigationRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(navController: NavHostController) {
    var isSuperAdmin by remember { mutableStateOf(false) }
    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var cafes by remember { mutableStateOf<Map<String, Cafe>>(emptyMap()) }
    var error by remember { mutableStateOf<String?>(null) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            firestore.collection("users")
                .document(user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        error = e.message
                        Log.e("Firestore", "Listen failed: ${e.message}")
                        return@addSnapshotListener
                    }

                    val userRole = snapshot?.getString("role") ?: "USER"

                    // Update states based on role
                    isSuperAdmin = userRole == "SUPER_ADMIN"
                    isAdmin = userRole == "ADMIN"

                    Log.d("Firestore", "User role updated: $userRole, isAdmin: $isAdmin, isSuperAdmin: $isSuperAdmin")
                }

            firestore.collection("cafes")
                .whereEqualTo("active", true)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        error = e.message
                        return@addSnapshotListener
                    }

                    // Create key-value pairs explicitly
                    cafes = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Cafe::class.java)?.let { cafe ->
                            Pair(cafe.id, cafe.copy(id = doc.id)) // Create Pair
                        }
                    }?.toMap() ?: emptyMap()
                }

            val productQuery = when {
                isSuperAdmin -> {
                    // Super admin sees all products
                    firestore.collection("products").whereEqualTo("active", true)
                }
                isAdmin -> {
                    // Admin sees only products for cafes they manage
                    val managedCafes = firestore.collection("users").document(user.uid).get().await().get("managedCafes") as? List<String> ?: emptyList()

                    if (managedCafes.isNotEmpty()) {
                        firestore.collection("products")
                            .whereEqualTo("active", true)
                            .whereIn("cafeId", managedCafes)
                    } else {
                        // If admin has no managed cafes, don't load products
                        null
                    }
                }
                else -> {
                    // Regular user sees all active products
                    firestore.collection("products")
                        .whereEqualTo("active", true)
                }
            }

            productQuery?.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    error = e.message
                    isLoading = false
                    return@addSnapshotListener
                }

                products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.catalog_title)) },
                actions = {
                    if (isSuperAdmin) {
                        // Add cafe button
                        IconButton(onClick = { navController.navigate(NavigationRoutes.AddCafe.route) }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.catalog_add_cafe)
                            )
                        }
                    }
                    if (isSuperAdmin || isAdmin) {
                        // Add product button
                        IconButton(onClick = { navController.navigate(NavigationRoutes.AddProduct.route) }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.catalog_add_product)
                            )
                        }
                    }
                }
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = stringResource(R.string.catalog_loading_error, error ?: ""),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                products.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.catalog_no_products),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    if (isLandscape) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = products,
                                key = { it.id }
                            ) { product ->
                                ProductCard(
                                    product = product,
                                    cafe = cafes[product.cafeId],
                                    isAdmin = isAdmin,
                                    onScanClick = {
                                        navController.navigate(NavigationRoutes.QrScanner.createRoute(product.id))
                                    }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = products,
                                key = { it.id }
                            ) { product ->
                                ProductCard(
                                    product = product,
                                    cafe = cafes[product.cafeId],
                                    isAdmin = isAdmin,
                                    onScanClick = {
                                        navController.navigate(NavigationRoutes.QrScanner.createRoute(product.id))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}