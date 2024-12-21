package com.example.loyalisttest.models

data class Cafe(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = CafeCategory.OTHER.name,
    val active: Boolean = true,
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class UserPoints(
    val userId: String = "",
    val cafeId: String = "",
    val productId: String = "",
    val currentProgress: Int = 0,
    val totalScans: Int = 0,
    val rewardsReceived: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class Product(
    val id: String = "",
    val cafeId: String = "",
    val name: String = "",
    val description: String = "",
    val scaleSize: Int = 10,
    val price: Double = 0.0,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val category: String = "",
    val imageUrl: String = ""
)