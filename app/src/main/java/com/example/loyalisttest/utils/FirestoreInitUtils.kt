package com.example.loyalisttest.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.loyalisttest.models.*

object FirestoreInitUtils {
    private val firestore = FirebaseFirestore.getInstance()
    private const val TAG = "FirestoreInitUtils"

    /**
     * Инициализирует коллекции Firestore для нового пользователя
     */
    suspend fun initializeCollections(userId: String, email: String, name: String, isFirstUser: Boolean): Result<Unit> {
        return try {
            Log.d(TAG, "Starting initialization for user: $userId, isFirstUser: $isFirstUser")

            // Проверяем, действительно ли это первый пользователь
            val actuallyFirst = isFirstUser && isFirstUser()
            Log.d(TAG, "Verified first user status: $actuallyFirst")

            // Определяем роль пользователя
            val userRole = if (actuallyFirst) UserRole.SUPER_ADMIN else UserRole.USER
            Log.d(TAG, "Assigned role: ${userRole.name}")

            // Создаем базовый документ пользователя
            val userData = hashMapOf(
                "userId" to userId,
                "email" to email,
                "name" to name,
                "role" to userRole.name,
                "registrationDate" to System.currentTimeMillis(),
                "totalPoints" to 0,
                "visitCount" to 0,
                "managedCafes" to listOf<String>()
            )

            // Создаем документ пользователя
            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()
            Log.d(TAG, "Created user document")

            // Если это первый пользователь (SUPER_ADMIN), создаем тестовые данные
            if (actuallyFirst) {
                Log.d(TAG, "Creating initial data for first user")

                // Создаем тестовое кафе
                val cafeData = hashMapOf(
                    "name" to "Тестовое кафе",
                    "description" to "Описание тестового кафе",
                    "category" to CafeCategory.COFFEE_SHOP.name,
                    "active" to true,
                    "createdAt" to System.currentTimeMillis(),
                    "createdBy" to userId,
                    "id" to "" // Будет обновлено после создания
                )

                // Создаем документ кафе
                val cafeRef = firestore.collection("cafes").document()
                val cafeId = cafeRef.id
                cafeData["id"] = cafeId
                cafeRef.set(cafeData).await()
                Log.d(TAG, "Created cafe with ID: $cafeId")

                // Создаем тестовый продукт
                val productData = hashMapOf(
                    "name" to "Тестовый продукт",
                    "description" to "Описание тестового продукта",
                    "cafeId" to cafeId,
                    "scaleSize" to 10,
                    "price" to 100.0,
                    "active" to true,
                    "createdAt" to System.currentTimeMillis(),
                    "createdBy" to userId,
                    "category" to "",
                    "imageUrl" to "",
                    "id" to "" // Будет обновлено после создания
                )

                // Создаем документ продукта
                val productRef = firestore.collection("products").document()
                val productId = productRef.id
                productData["id"] = productId
                productRef.set(productData).await()
                Log.d(TAG, "Created product with ID: $productId")

                // Инициализируем точки пользователя
                val userPointsData = hashMapOf(
                    "userId" to userId,
                    "cafeId" to cafeId,
                    "productId" to productId,
                    "currentProgress" to 0,
                    "totalScans" to 0,
                    "rewardsReceived" to 0,
                    "lastUpdated" to System.currentTimeMillis()
                )

                // Создаем документ точек пользователя с составным ID
                val userPointsId = "${userId}_${cafeId}_${productId}"
                firestore.collection("userPoints")
                    .document(userPointsId)
                    .set(userPointsData)
                    .await()
                Log.d(TAG, "Created userPoints document with ID: $userPointsId")

                // Обновляем managedCafes для супер-админа
                firestore.collection("users")
                    .document(userId)
                    .update("managedCafes", listOf(cafeId))
                    .await()
                Log.d(TAG, "Updated managedCafes for super admin")
            }

            Log.d(TAG, "Successfully completed initialization")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during initialization", e)
            Result.failure(e)
        }
    }

    /**
     * Проверяет, является ли пользователь первым в системе
     */
    suspend fun isFirstUser(): Boolean {
        return try {
            val usersSnapshot = firestore.collection("users")
                .limit(1)
                .get()
                .await()
            val isEmpty = usersSnapshot.isEmpty
            Log.d(TAG, "Checked if first user: $isEmpty")
            isEmpty
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if first user", e)
            true // В случае ошибки предполагаем, что это первый пользователь
        }
    }

    /**
     * Проверяет роль пользователя
     */
    suspend fun checkUserRole(userId: String): UserRole {
        return try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val roleString = userDoc.getString("role")
            val role = UserRole.values().find { it.name == roleString } ?: UserRole.USER
            Log.d(TAG, "Retrieved user role: ${role.name}")
            role
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user role", e)
            UserRole.USER
        }
    }

    /**
     * Проверяет существование всех необходимых коллекций
     */
    suspend fun checkCollectionsExist(): Boolean {
        return try {
            var allExist = true
            val collections = listOf("users", "cafes", "products", "userPoints")

            for (collection in collections) {
                val snapshot = firestore.collection(collection)
                    .limit(1)
                    .get()
                    .await()
                if (snapshot.isEmpty) {
                    allExist = false
                    Log.w(TAG, "Collection $collection is empty")
                    break
                }
            }

            Log.d(TAG, "Checked collections existence: $allExist")
            allExist
        } catch (e: Exception) {
            Log.e(TAG, "Error checking collections", e)
            false
        }
    }
}