package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.loyalisttest.models.*
import com.example.loyalisttest.navigation.NavigationRoutes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(navController: NavHostController) {
    var isAdmin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()

    // Проверяем права админа и загружаем продукты
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // Проверяем роль пользователя
            firestore.collection("users")
                .document(user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        error = e.message
                        return@addSnapshotListener
                    }

                    isAdmin = snapshot?.getString("role") == "ADMIN"
                }

            // Загружаем продукты в реальном времени
            firestore.collection("products")
                .whereEqualTo("active", true)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        error = e.message
                        isLoading = false
                        return@addSnapshotListener
                    }

                    products = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            doc.toObject(Product::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()

                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Каталог") },
                actions = {
                    if (isAdmin) {
                        // Кнопка добавления кафе
                        IconButton(
                            onClick = { navController.navigate(NavigationRoutes.AddCafe.route) }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Добавить кафе",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        // Кнопка добавления товара
                        IconButton(
                            onClick = { navController.navigate(NavigationRoutes.AddProduct.route) }
                        ) {
                            Icon(Icons.Default.Add, "Добавить товар")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Text(
                        text = error!!,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                products.isEmpty() -> {
                    Text(
                        text = "Нет доступных товаров",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
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
                                isAdmin = isAdmin,
                                onScanClick = {
                                    navController.navigate(
                                        NavigationRoutes.QrScanner.createRoute(product.id)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    isAdmin: Boolean,
    onScanClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.price} ₽",
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = "+${product.points} баллов",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сканировать QR код")
                }
            }
        }
    }
}