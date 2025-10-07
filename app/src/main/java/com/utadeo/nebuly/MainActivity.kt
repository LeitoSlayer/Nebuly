package com.utadeo.nebuly

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(auth)
                }
            }
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = "Iniciar Sesión",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading
        )

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            enabled = !isLoading
        )

        // Mensaje de error
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Botón de Login
        Button(
            onClick = {
                if (validateInputs(email, password)) {
                    loginUser(auth, email, password, context) { loading, error ->
                        isLoading = loading
                        errorMessage = error
                    }
                } else {
                    errorMessage = "Por favor completa todos los campos correctamente"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("INICIAR SESIÓN", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón de Registro
        Button(
            onClick = {
                if (validateInputs(email, password)) {
                    registerUser(auth, email, password, context) { loading, error ->
                        isLoading = loading
                        errorMessage = error
                    }
                } else {
                    errorMessage = "Por favor completa todos los campos correctamente"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                Text("REGISTRARSE", fontSize = 16.sp)
            }
        }
    }
}

// Función para validar inputs
fun validateInputs(email: String, password: String): Boolean {
    return email.isNotEmpty() &&
            email.contains("@") &&
            password.isNotEmpty() &&
            password.length >= 6
}

// Función para iniciar sesión
fun loginUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    context: android.content.Context,
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
                    "Bienvenido ${user?.email}",
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
    context: android.content.Context,
    onResult: (Boolean, String) -> Unit
) {
    onResult(true, "") // Mostrar loading

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            onResult(false, "") // Ocultar loading

            if (task.isSuccessful) {
                Toast.makeText(
                    context,
                    "Usuario registrado exitosamente",
                    Toast.LENGTH_LONG
                ).show()

                // Auto-login después de registro
                loginUser(auth, email, password, context, onResult)
            } else {
                onResult(false, "Error: ${task.exception?.message}")
            }
        }
}