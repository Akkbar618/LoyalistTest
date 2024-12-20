package com.example.loyalisttest.models

data class Cafe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val isActive: Boolean = true
)
data class UserPoints(
    val cafeId: String = "",
    val userId: String = "",
    val currentPoints: Int = 0,
    val totalEarnedPoints: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
data class Product(
    val id: String = "",
    val cafeId: String = "",
    val name: String = "",
    val description: String = "",
    val points: Int = 0,
    val price: Double = 0.0,
    val active: Boolean = true,  // Изменено с isActive на active
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val category: String = "",
    val imageUrl: String = ""
)