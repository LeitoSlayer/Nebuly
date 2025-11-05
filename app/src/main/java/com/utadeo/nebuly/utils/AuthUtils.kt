package com.utadeo.nebuly.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


fun validateLoginInputs(email: String, password: String): Boolean {
    return email.isNotEmpty() &&
            email.contains("@") &&
            password.isNotEmpty() &&
            password.length >= 6
}


fun validateRegisterInputs(email: String, password: String, username: String): Boolean {
    return email.isNotEmpty() &&
            email.contains("@") &&
            password.isNotEmpty() &&
            password.length >= 6 &&
            username.isNotEmpty() &&
            username.length >= 3
}


fun loginUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    context: Context,
    onResult: (Boolean, String) -> Unit
) {
    onResult(true, "")

    auth.signInWithEmailAndPassword(email.trim(), password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {

                onResult(false, "")
                val user = auth.currentUser
                Toast.makeText(
                    context,
                    "Bienvenido ${user?.displayName ?: user?.email}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                val errorMsg = when (task.exception) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        "Contraseña incorrecta"
                    }
                    is FirebaseAuthInvalidUserException -> {
                        val errorCode = (task.exception as FirebaseAuthInvalidUserException).errorCode
                        when (errorCode) {
                            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo"
                            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada"
                            else -> "Usuario no encontrado"
                        }
                    }
                    else -> {
                        when {
                            task.exception?.message?.contains("badly formatted") == true ->
                                "El formato del correo es inválido"
                            task.exception?.message?.contains("network") == true ->
                                "Error de conexión. Verifica tu internet"
                            task.exception?.message?.contains("too many requests") == true ->
                                "Demasiados intentos fallidos. Intenta más tarde"
                            else ->
                                "Error al iniciar sesión. Intenta ingresar de nuevo"
                        }
                    }
                }

                onResult(false, errorMsg)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
        .addOnFailureListener { exception ->

            val errorMsg = "Error al iniciar sesión. Intenta ingresar de nuevo"
            onResult(false, errorMsg)
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
}


fun registerUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    username: String,
    context: Context,
    onResult: (Boolean, String, String?) -> Unit
) {
    onResult(true, "", null)

    auth.createUserWithEmailAndPassword(email.trim(), password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user?.uid

                if (userId == null) {
                    onResult(false, "Error: No se pudo obtener el usuario", null)
                    return@addOnCompleteListener
                }


                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {

                            createUserInFirestore(
                                userId = userId,
                                username = username,
                                email = email,
                                onSuccess = {
                                    onResult(false, "", userId)
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
                            onResult(false, "Error al actualizar perfil", null)
                        }
                    }
            } else {
                val errorMsg = when {
                    task.exception?.message?.contains("email address is already") == true ->
                        "Este correo ya está registrado"
                    task.exception?.message?.contains("weak password") == true ->
                        "La contraseña debe tener al menos 6 caracteres"
                    task.exception?.message?.contains("badly formatted") == true ->
                        "El formato del correo es inválido"
                    else ->
                        "Error. Intenta ingresar de nuevo"
                }
                onResult(false, errorMsg, null)
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
}


private fun createUserInFirestore(
    userId: String,
    username: String,
    email: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()


    val userData = hashMapOf(
        "userId" to userId,
        "username" to username,
        "email" to email,
        "level" to 1,
        "currentAvatarId" to "avatar_default",
        "unlockedAvatars" to listOf("avatar_default"),
        "createdAt" to com.google.firebase.Timestamp.now()
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            firestore.collection("users")
                .document(userId)
                .set(userData)
                .await()

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