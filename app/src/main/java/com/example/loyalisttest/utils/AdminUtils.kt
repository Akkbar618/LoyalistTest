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

            // Создаем или обновляем пользователя с ролью админа
            val adminData = mapOf(
                "email" to email,
                "userId" to userId,
                "role" to "ADMIN",
                "updatedAt" to System.currentTimeMillis(),
                "name" to (userDoc.getString("name") ?: "Admin"),
                "totalPoints" to (userDoc.getLong("totalPoints") ?: 0),
                "visitCount" to (userDoc.getLong("visitCount") ?: 0),
                "registrationDate" to (userDoc.getLong("registrationDate")
                    ?: System.currentTimeMillis())
            )

            // Обновляем документ пользователя
            firestore.collection("users")
                .document(userId)
                .set(adminData)
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