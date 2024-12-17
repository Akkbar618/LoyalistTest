package com.example.loyalisttest.models

data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.USER,
    val points: Int = 0,
    val registrationDate: Long = 0,
    val visitCount: Int = 0
)

enum class UserRole {
    ADMIN,
    USER
}

data class PointsHistoryRecord(
    val id: String = "",
    val userId: String = "",
    val adminId: String = "",
    val pointsAdded: Int = 0,
    val timestamp: Long = 0,
    val description: String = ""
)