package com.utadeo.nebuly.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.utils.validateLoginInputs
import com.utadeo.nebuly.utils.loginUser

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Fuente Aoboshi One para los botones
    val aoboshiOne = FontFamily(
        Font(R.font.aoboshi_one_regular, FontWeight.Normal)
    )

    // Box principal con imagen de fondo
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(R.drawable.fondo_inicio_sesion),
            contentDescription = "Fondo de inicio de sesión",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Botón para volver atrás
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 60.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(65.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.boton_volver),
                        contentDescription = "Volver",
                        modifier = Modifier.size(90.dp)
                    )
                }
            }

            // Logo
            Image(
                painter = painterResource(R.drawable.logo_nebuly_app),
                contentDescription = "Logo Nebuly",
                modifier = Modifier
                    .size(300.dp)
                    .padding(bottom = 1.dp)
            )

            // Campos de texto
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Campo de Email con bordes más redondeados
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        label = {
                            Text(
                                "Correo electrónico",
                                color = Color.White
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.extraLarge, // Bordes más redondeados
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedLabelColor = Color.White,
                            cursorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.White
                        )
                    )

                    // Campo de Contraseña con bordes más redondeados
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = {
                            Text(
                                "Contraseña",
                                color = Color.White
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.extraLarge, // Bordes más redondeados
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            focusedLabelColor = Color.White,
                            cursorColor = Color.White,
                            unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.White
                        )
                    )
                }
            }

            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Botón CONTINUAR
            Button(
                onClick = {
                    if (validateLoginInputs(email, password)) {
                        loginUser(auth, email, password, context) { loading, error ->
                            isLoading = loading
                            errorMessage = error
                        }
                    } else {
                        errorMessage = "Completar todos los campos correctamente"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                enabled = !isLoading,
                shape = MaterialTheme.shapes.extraLarge, // Bordes redondeados
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.5f), // Blanco transparente
                    contentColor = Color.Black // Texto blanco
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        "CONTINUAR",
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = aoboshiOne
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}