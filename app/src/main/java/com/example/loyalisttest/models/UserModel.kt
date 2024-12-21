package com.example.loyalisttest.models

enum class UserRole(val displayName: String) {
    SUPER_ADMIN("Супер-админ"),
    ADMIN("Администратор"),
    USER("Пользователь")
}

data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = UserRole.USER.name,
    val managedCafes: List<String> = emptyList(), // Список кафе, которыми управляет админ
    val registrationDate: Long = System.currentTimeMillis()
)