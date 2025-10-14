package com.utadeo.nebuly.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.utadeo.nebuly.data.models.User
import kotlinx.coroutines.tasks.await
import android.util.Log

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Obtiene los datos del usuario
     */
    suspend fun getUser(userId: String): Result<User> {
        return try {
            Log.d("UserRepository", "Obteniendo usuario: $userId")

            val doc = usersCollection.document(userId).get().await()

            if (!doc.exists()) {
                Log.e("UserRepository", "Documento de usuario no existe")
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val user = doc.toObject(User::class.java)

            if (user != null) {
                Log.d("UserRepository", "Usuario obtenido - Coins: ${user.coins}, Avatares: ${user.unlockedAvatars.size}")
                Result.success(user)
            } else {
                Log.e("UserRepository", "No se pudo convertir el documento a User")
                Result.failure(Exception("Error al procesar datos del usuario"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al obtener usuario", e)
            Result.failure(e)
        }
    }

    /**
     * Compra un avatar deduciendo monedas y agregándolo a avatares desbloqueados
     */
    suspend fun purchaseAvatar(userId: String, avatarId: String, cost: Int): Result<Boolean> {
        return try {
            Log.d("UserRepository", "Iniciando compra - User: $userId, Avatar: $avatarId, Costo: $cost")

            // Primero verificamos que el usuario tenga suficientes monedas
            val userResult = getUser(userId)
            if (userResult.isFailure) {
                return Result.failure(userResult.exceptionOrNull() ?: Exception("Error al obtener usuario"))
            }

            val user = userResult.getOrNull()!!

            // Verificar si ya tiene el avatar
            if (user.unlockedAvatars.contains(avatarId)) {
                Log.w("UserRepository", "Avatar ya desbloqueado")
                return Result.failure(Exception("Ya tienes este avatar"))
            }

            // Verificar si tiene suficientes monedas
            if (user.coins < cost) {
                Log.w("UserRepository", "Monedas insuficientes - Tiene: ${user.coins}, Necesita: $cost")
                return Result.failure(Exception("Monedas insuficientes"))
            }

            // Realizar la transacción
            val userRef = usersCollection.document(userId)

            firestore.runTransaction { transaction ->
                // Deducir monedas
                transaction.update(userRef, "coins", user.coins - cost)

                // Agregar avatar a la lista de desbloqueados
                transaction.update(userRef, "unlockedAvatars", FieldValue.arrayUnion(avatarId))
            }.await()

            Log.d("UserRepository", "Compra exitosa")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al comprar avatar", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el avatar actual del usuario
     */
    suspend fun setCurrentAvatar(userId: String, avatarId: String): Result<Boolean> {
        return try {
            Log.d("UserRepository", "Actualizando avatar actual: $userId -> $avatarId")

            usersCollection.document(userId)
                .update("currentAvatarId", avatarId)
                .await()

            Log.d("UserRepository", "Avatar actual actualizado")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al actualizar avatar actual", e)
            Result.failure(e)
        }
    }

    /**
     * Agrega monedas al usuario (para testing o recompensas)
     */
    suspend fun addCoins(userId: String, amount: Int): Result<Boolean> {
        return try {
            Log.d("UserRepository", "Agregando $amount monedas al usuario: $userId")

            usersCollection.document(userId)
                .update("coins", FieldValue.increment(amount.toLong()))
                .await()

            Log.d("UserRepository", "Monedas agregadas exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al agregar monedas", e)
            Result.failure(e)
        }
    }
}