package com.utadeo.nebuly.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

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

// Función para registrar usuario
fun registerUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    username: String,
    context: Context,
    onResult: (Boolean, String) -> Unit
) {
    onResult(true, "") // Mostrar loading

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Actualizar el perfil del usuario con el nombre de usuario
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                user?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener { profileTask ->
                        onResult(false, "") // Ocultar loading

                        if (profileTask.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Usuario $username registrado exitosamente",
                                Toast.LENGTH_LONG
                            ).show()

                            // Auto-login después de registro
                            loginUser(auth, email, password, context, onResult)
                        } else {
                            onResult(false, "Error al actualizar perfil: ${profileTask.exception?.message}")
                        }
                    }
            } else {
                onResult(false, "Error: ${task.exception?.message}")
            }
        }
}