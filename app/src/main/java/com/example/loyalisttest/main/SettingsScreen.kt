package com.example.loyalisttest.main.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showSignOutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Заголовок
        Text(
            text = "Настройки",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Информация о пользователе
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = currentUser?.displayName ?: "Пользователь",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = currentUser?.email ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Кнопка выхода
        OutlinedButton(
            onClick = { showSignOutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Sign Out",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Выйти из аккаунта")
        }
    }

    // Диалог подтверждения выхода
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Выйти из аккаунта?") },
            text = { Text("Вы уверены, что хотите выйти из аккаунта?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        // Навигация будет обработана в MainScreen
                    }
                ) {
                    Text("Выйти", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}