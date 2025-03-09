package com.example.loyalisttest.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.loyalisttest.R
import com.example.loyalisttest.components.ProgressScale
import com.example.loyalisttest.models.Cafe
import com.example.loyalisttest.models.Product
import com.example.loyalisttest.models.UserPoints
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProductCard(
    product: Product,
    cafe: Cafe?,
    isAdmin: Boolean,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userPoints by remember { mutableStateOf<UserPoints?>(null) }
    var isLoadingProgress by remember { mutableStateOf(!isAdmin) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Load progress for regular users
    LaunchedEffect(product.id) {
        if (!isAdmin && currentUser != null) {
            firestore.collection("userPoints")
                .document("${currentUser.uid}_${product.cafeId}_${product.id}")
                .get()
                .addOnSuccessListener { doc ->
                    userPoints = doc.toObject(UserPoints::class.java)
                    isLoadingProgress = false
                }
                .addOnFailureListener {
                    isLoadingProgress = false
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
            // Cafe name
            cafe?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

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

            Text(
                text = stringResource(R.string.price_format, product.price),
                style = MaterialTheme.typography.titleSmall
            )

            if (!isAdmin) {
                Spacer(modifier = Modifier.height(12.dp))

                if (isLoadingProgress) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    Column {
                        ProgressScale(
                            currentProgress = userPoints?.currentProgress ?: 0,
                            maxProgress = product.scaleSize
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.points_count,
                                    userPoints?.currentProgress ?: 0,
                                    product.scaleSize
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = stringResource(
                                    R.string.rewards_count,
                                    userPoints?.rewardsReceived ?: 0
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.catalog_scan_qr))
                }
            }
        }
    }
}