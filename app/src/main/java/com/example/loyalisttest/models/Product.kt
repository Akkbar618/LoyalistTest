package com.example.loyalisttest.models

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val points: Int,
    val qrCode: String? = null
)