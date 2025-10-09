package com.utadeo.nebuly.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Función para validar inputs de inicio de sesión
fun validateLoginInputs(email: String, password: String): Boolean {
    return email.isNotEmpty() &&
            email.contains("@") &&
            password.isNotEmpty() &&
            password.length >= 6
}

// Función para validar inputs de registro
fun validateRegisterInputs(email: String, password: String, username: String): Boolean {
    return email.isNotEmpty() &&
            email.contains("@") &&
            password.isNotEmpty() &&
            password.length >= 6 &&
            username.isNotEmpty() &&
            username.length >= 3
}

// Función para iniciar sesión
fun loginUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    context: Context,
    onResult: (Boolean, String) -> Unit
) {
    onResult(true, "") // Mostrar loading

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            onResult(false, "") // Ocultar loading

            if (task.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(
                    context,
                    "Bienvenido ${user?.displayName ?: user?.email}",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                onResult(false, "Error: ${task.exception?.message}")
            }
        }
}

// ✅ MODIFICADO: Función para registrar usuario CON Firestore
fun registerUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    username: String,
    context: Context,
    onResult: (Boolean, String, String?) -> Unit // ✅ Cambiado: agregado String? para userId
) {
    onResult(true, "", null) // Mostrar loading

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                if (userId == null) {
                    onResult(false, "Error: No se pudo obtener el ID del usuario", null)
                    return@addOnCompleteListener
                }

                // Actualizar el perfil del usuario con el nombre de usuario
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            // ✅ NUEVO: Crear documento del usuario en Firestore
                            createUserInFirestore(
                                userId = userId,
                                username = username,
                                email = email,
                                onSuccess = {
                                    onResult(false, "", userId) // ✅ Devolver userId
                                    Toast.makeText(
                                        context,
                                        "Usuario $username registrado exitosamente",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                onError = { error ->
                                    onResult(false, "Error al crear usuario en Firestore: $error", null)
                                }
                            )
                        } else {
                            onResult(false, "Error al actualizar perfil: ${profileTask.exception?.message}", null)
                        }
                    }
            } else {
                onResult(false, "Error: ${task.exception?.message}", null)
            }
        }
}

// ✅ NUEVA FUNCIÓN: Crear documento del usuario en Firestore
private fun createUserInFirestore(
    userId: String,
    username: String,
    email: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()

    // Datos del usuario con avatar por defecto desbloqueado
    val userData = hashMapOf(
        "userId" to userId,
        "username" to username,
        "email" to email,
        "level" to 1,
        "currentAvatarId" to "avatar_default",
        "unlockedAvatars" to listOf("avatar_default"), // Solo el avatar por defecto desbloqueado
        "createdAt" to com.google.firebase.Timestamp.now()
    )

    // Usar coroutine para operación asíncrona
    CoroutineScope(Dispatchers.IO).launch {
        try {
            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

            // Volver al hilo principal para mostrar el resultado
            CoroutineScope(Dispatchers.Main).launch {
                onSuccess()
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                onError(e.message ?: "Error desconocido")
            }
        }
    }
}

// ✅ NUEVA FUNCIÓN AUXILIAR: Actualizar nivel del usuario (útil para desbloquear avatares)
fun updateUserLevel(
    userId: String,
    newLevel: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            firestore.collection("users")
                .document(userId)
                .update("level", newLevel)
                .await()

            CoroutineScope(Dispatchers.Main).launch {
                onSuccess()
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                onError(e.message ?: "Error al actualizar nivel")
            }
        }
    }
}