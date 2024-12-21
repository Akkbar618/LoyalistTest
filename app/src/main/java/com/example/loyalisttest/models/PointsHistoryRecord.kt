package com.example.loyalisttest.models

data class PointsHistoryRecord(
    val id: String = "",
    val userId: String = "",
    val adminId: String = "",
    val cafeId: String = "",
    val productId: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val progress: Int = 0,
    val isReward: Boolean = false
)