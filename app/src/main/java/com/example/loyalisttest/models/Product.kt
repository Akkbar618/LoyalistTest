package com.example.loyalisttest.models

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val points: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val isActive: Boolean = true
)