package com.example.loyalisttest.utils

import com.example.loyalisttest.models.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object RoleManager {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("role", newRole.name)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(userId: String): Result<UserRole> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val roleString = snapshot.getString("role") ?: UserRole.USER.name
            val role = UserRole.values().find { it.name == roleString } ?: UserRole.USER

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}