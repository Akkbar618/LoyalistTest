//package com.example.loyalisttest.components
//
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import com.example.loyalisttest.models.Cafe
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CafeSelector(
//    selectedCafe: Cafe?,
//    cafes: List<Cafe>,
//    onCafeSelected: (Cafe) -> Unit,
//    enabled: Boolean = true,
//    modifier: Modifier = Modifier
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { if (enabled) expanded = it },
//        modifier = modifier
//    ) {
//        OutlinedTextField(
//            value = selectedCafe?.name ?: "Выберите кафе",
//            onValueChange = { },
//            readOnly = true,
//            enabled = enabled,
//            modifier = Modifier
//                .fillMaxWidth()
//                .menuAnchor()
//        )
//
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            cafes.forEach { cafe ->
//                DropdownMenuItem(
//                    text = { Text(cafe.name) },
//                    onClick = {
//                        onCafeSelected(cafe)
//                        expanded = false
//                    }
//                )
//            }
//        }
//    }
//}