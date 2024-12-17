package com.example.loyalisttest.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AdminUtils {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createAdmin(email: String, userId: String): Result<Unit> {
        return try {
            // Проверяем, существует ли пользователь
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                return Result.failure(Exception("Пользователь не найден"))
            }

            // Обновляем роль пользователя на ADMIN
            firestore.collection("users")
                .document(userId)
                .update(
                    mapOf(
                        "role" to "ADMIN",
                        "updatedAt" to System.currentTimeMillis(),
                        "isAdmin" to true
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Проверка является ли пользователь администратором
    suspend fun isUserAdmin(userId: String): Boolean {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            userDoc.getString("role") == "ADMIN"
        } catch (e: Exception) {
            false
        }
    }
}