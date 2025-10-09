package com.utadeo.nebuly.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.utils.validateRegisterInputs
import com.utadeo.nebuly.utils.registerUser
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.components.ActionButton

@Composable
fun RegisterScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAvatarSelection: (String) -> Unit = {} // ✅ NUEVO parámetro
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // ✅ NUEVO: Estados para el avatar
    var currentAvatarUrl by remember { mutableStateOf<String?>(null) }
    var currentUserId by remember { mutableStateOf<String?>(null) }

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
            contentDescription = "Fondo de registro",
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
                BackButton(onClick = onBackClick)
            }

            // ✅ MODIFICADO: Box con imagen de perfil + botón de editar
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Imagen de perfil (usa AsyncImage si hay URL, sino usa el drawable por defecto)
                if (currentAvatarUrl != null) {
                    AsyncImage(
                        model = currentAvatarUrl,
                        contentDescription = "Avatar del usuario",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.ic_persona),
                        contentDescription = "Icono usuario",
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                // ✅ NUEVO: Botón de editar avatar (ícono de lápiz)
                // Solo aparece si el usuario ya está registrado
                if (currentUserId != null) {
                    IconButton(
                        onClick = {
                            currentUserId?.let { userId ->
                                onNavigateToAvatarSelection(userId)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(48.dp)
                            .background(Color(0xFFE8B4F0), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar avatar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

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
                    // Campo de Nombre de Usuario con bordes redondeados
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = ""
                        },
                        label = {
                            Text(
                                "Nombre de usuario",
                                color = Color.White
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.extraLarge,
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

                    // Campo de Email con bordes redondeados
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
                            .padding(bottom = 16.dp),
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.extraLarge,
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

                    // Campo de Contraseña con bordes redondeados
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
                            .padding(bottom = 8.dp),
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.extraLarge,
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
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Botón de Registro
            ActionButton(
                text = "REGISTRARSE",
                isLoading = isLoading,
                onClick = {
                    if (validateRegisterInputs(email, password, username)) {
                        // ✅ MODIFICADO: Callback actualizado para recibir userId
                        registerUser(auth, email, password, username, context) { loading, error, userId ->
                            isLoading = loading
                            errorMessage = error

                            // ✅ NUEVO: Guardar el userId cuando el registro sea exitoso
                            if (userId != null && error.isEmpty()) {
                                currentUserId = userId
                                // Aquí podrías cargar el avatar por defecto si quisieras
                                // currentAvatarUrl = "url_del_avatar_por_defecto"
                            }
                        }
                    } else {
                        errorMessage = "Completar todos los campos correctamente"
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}