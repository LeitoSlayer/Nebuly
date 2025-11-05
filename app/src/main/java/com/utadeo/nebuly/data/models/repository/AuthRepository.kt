package com.utadeo.nebuly.data.models.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    suspend fun registerUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                Result.success(user)
            } ?: Result.failure(Exception("Error al crear el usuario"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                Result.success(user)
            } ?: Result.failure(Exception("Error al iniciar sesión"))
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("password is invalid") == true ->
                    "Contraseña incorrecta"
                e.message?.contains("no user record") == true ->
                    "El usuario no existe"
                e.message?.contains("badly formatted") == true ->
                    "Correo electrónico inválido"
                e.message?.contains("network error") == true ->
                    "Error de conexión. Verifica tu internet"
                else -> e.message ?: "Error desconocido al iniciar sesión"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("no user record") == true ->
                    "No existe una cuenta con este correo"
                e.message?.contains("badly formatted") == true ->
                    "Correo electrónico inválido"
                else -> e.message ?: "Error al enviar correo de recuperación"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return firebaseAuth.currentUser?.email
    }
}