package com.example.loyalisttest.models

data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val cafeId: String? = null, // Для админов - ID их кафе
    val registrationDate: Long = System.currentTimeMillis()
)

enum class UserRole {
    ADMIN,
    USER
}