package com.example.loyalisttest.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loyalisttest.R
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityChange: (() -> Unit)? = null,
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Black,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Gray,
        focusedLeadingIconColor = Color.Black,
        unfocusedLeadingIconColor = Color.Gray,
        focusedTrailingIconColor = Color.Black,
        unfocusedTrailingIconColor = Color.Gray,
        cursorColor = Color.Black,
        selectionColors = TextSelectionColors(
            handleColor = colorResource(id = R.color.light_black), // Цвет стрелки селектора
            backgroundColor = colorResource(id = R.color.light_black).copy(alpha = 0.4f) // Цвет выделения текста (полупрозрачный)
        )
    )

    // Определяем тип автозаполнения на основе содержимого label
    val autofillTypes = when {
        isPassword -> listOf(AutofillType.Password)
        label.contains("email", ignoreCase = true) -> listOf(AutofillType.EmailAddress)
        label.contains("имя", ignoreCase = true) ||
                label.contains("name", ignoreCase = true) -> listOf(AutofillType.PersonFullName)
        else -> listOf(AutofillType.Username)
    }

    // Настройка автозаполнения
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(
        autofillTypes = autofillTypes,
        onFill = { onValueChange(it) }
    )

    LocalAutofillTree.current += autofillNode

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        colors = textFieldColors,
        visualTransformation = if (isPassword && !isPasswordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = when {
                isPassword -> KeyboardType.Password
                label.contains("email", ignoreCase = true) -> KeyboardType.Email
                else -> KeyboardType.Text
            },
            imeAction = ImeAction.Next,
            autoCorrect = !isPassword
        ),
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onVisibilityChange?.invoke() }) {
                    Icon(
                        painter = painterResource(
                            if (isPasswordVisible) R.drawable.icon_visible_on
                            else R.drawable.icon_visible_off
                        ),
                        contentDescription = if (isPasswordVisible) "Скрыть пароль" else "Показать пароль"
                    )
                }
            }
        } else null,
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                // Обновление размеров и координат узла автозаполнения
                autofillNode.boundingBox = coordinates.boundsInWindow()
            }
            .onFocusChanged { focusState ->
                // Запуск автозаполнения при получении фокуса
                if (focusState.isFocused) {
                    autofill?.requestAutofillForNode(autofillNode)
                } else {
                    autofill?.cancelAutofillForNode(autofillNode)
                }
            }
    )
}

@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            disabledContainerColor = Color.Gray
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(text = text, fontSize = 16.sp)
        }
    }
}

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .width(40.dp)
            .height(40.dp)
            .background(
                color = Color.Black,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "Назад",
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun OutlinedAuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorResource(R.color.my_gray)
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}