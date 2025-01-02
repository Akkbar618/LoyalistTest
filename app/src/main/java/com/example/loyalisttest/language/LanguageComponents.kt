//package com.example.loyalisttest.language
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun rememberCurrentLanguage(): AppLanguage {
//    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
//    return currentLanguage
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LanguageSwitcher(
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    val currentLanguage = rememberCurrentLanguage()
//    var isDropdownExpanded by remember { mutableStateOf(false) }
//
//    ExposedDropdownMenuBox(
//        expanded = isDropdownExpanded,
//        onExpandedChange = { isDropdownExpanded = it },
//        modifier = modifier
//    ) {
//        OutlinedTextField(
//            value = currentLanguage.displayName,
//            onValueChange = { },
//            readOnly = true,
//            label = { Text("Язык приложения") },
//            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
//            modifier = Modifier
//                .fillMaxWidth()
//                .menuAnchor()
//        )
//
//        ExposedDropdownMenu(
//            expanded = isDropdownExpanded,
//            onDismissRequest = { isDropdownExpanded = false }
//        ) {
//            AppLanguage.values().forEach { language ->
//                DropdownMenuItem(
//                    text = { Text(language.displayName) },
//                    onClick = {
//                        LanguageManager.setLanguage(language, context)
//                        isDropdownExpanded = false
//                    }
//                )
//            }
//        }
//    }
//}