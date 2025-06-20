package com.example.loyalisttest.main

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.example.loyalisttest.R
import com.example.loyalisttest.language.LanguageSwitcher
import com.example.loyalisttest.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showSignOutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()

    var name by remember(currentUser) { mutableStateOf(currentUser?.displayName ?: "") }
    var email by remember(currentUser) { mutableStateOf(currentUser?.email ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Карточка с информацией о пользователе
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
                        contentDescription = stringResource(R.string.settings_user_section),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.settings_user_section),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.settings_edit_name)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    enabled = !isSaving
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.settings_edit_email)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    enabled = !isSaving
                )

                if (updateMessage != null) {
                    Text(
                        text = updateMessage!!,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        if (name.isBlank() || email.isBlank() || currentUser == null) {
                            updateMessage = context.getString(R.string.fill_all_fields)
                            return@Button
                        }
                        coroutineScope.launch {
                            isSaving = true
                            try {
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name.trim())
                                    .build()
                                currentUser.updateProfile(profileUpdates).await()
                                currentUser.updateEmail(email.trim()).await()
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(currentUser.uid)
                                    .update(mapOf(
                                        "name" to name.trim(),
                                        "email" to email.trim()
                                    )).await()
                                updateMessage = context.getString(R.string.settings_profile_updated)
                            } catch (e: Exception) {
                                updateMessage = context.getString(
                                    R.string.settings_error_updating_profile,
                                    e.message ?: ""
                                )
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.settings_save_profile))
                    }
                }
            }
        }

        // Карточка с настройками языка
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LanguageSwitcher(
                    onLanguageChanged = {
                        // Restart activity with smooth transition
                        activity?.let {
                            val intent = it.intent
                            it.finish()
                            it.startActivity(intent)
                            it.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                )
            }
        }

        // Card with theme switch
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.settings_dark_theme),
                    style = MaterialTheme.typography.titleMedium
                )

                val isDark by ThemeManager.darkTheme
                Switch(
                    checked = isDark,
                    onCheckedChange = {
                        ThemeManager.toggleTheme(context)
                    }
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
                contentDescription = stringResource(R.string.settings_sign_out),
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(R.string.settings_sign_out))
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.settings_sign_out_confirmation)) },
            text = { Text(stringResource(R.string.settings_sign_out_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                    }
                ) {
                    Text(
                        stringResource(R.string.settings_sign_out_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }
}