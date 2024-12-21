package com.example.loyalisttest.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AdminUtils {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun createAdmin(email: String, userId: String): Result<Unit> {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                return Result.failure(Exception("Пользователь не найден"))
            }

            val adminData = mapOf(
                "email" to email,
                "userId" to userId,
                "role" to "ADMIN",
                "updatedAt" to System.currentTimeMillis(),
                "name" to (userDoc.getString("name") ?: "Admin"),
                "managedCafes" to listOf<String>(),
                "registrationDate" to (userDoc.getLong("registrationDate")
                    ?: System.currentTimeMillis())
            )

            firestore.collection("users")
                .document(userId)
                .set(adminData)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

    suspend fun isSuperAdmin(userId: String): Boolean {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            userDoc.getString("role") == "SUPER_ADMIN"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addCafeToAdmin(adminId: String, cafeId: String): Result<Unit> {
        return try {
            val userDoc = firestore.collection("users")
                .document(adminId)
                .get()
                .await()

            if (!userDoc.exists()) {
                return Result.failure(Exception("Администратор не найден"))
            }

            if (userDoc.getString("role") != "ADMIN") {
                return Result.failure(Exception("Пользователь не является администратором"))
            }

            firestore.collection("users")
                .document(adminId)
                .update("managedCafes", FieldValue.arrayUnion(cafeId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeCafeFromAdmin(adminId: String, cafeId: String): Result<Unit> {
        return try {
            val userDoc = firestore.collection("users")
                .document(adminId)
                .get()
                .await()

            if (!userDoc.exists()) {
                return Result.failure(Exception("Администратор не найден"))
            }

            firestore.collection("users")
                .document(adminId)
                .update("managedCafes", FieldValue.arrayRemove(cafeId))
                .await()

            val updatedDoc = firestore.collection("users")
                .document(adminId)
                .get()
                .await()

            val managedCafes = updatedDoc.get("managedCafes") as? List<String> ?: emptyList()
            if (managedCafes.isEmpty()) {
                firestore.collection("users")
                    .document(adminId)
                    .update("role", "USER")
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}