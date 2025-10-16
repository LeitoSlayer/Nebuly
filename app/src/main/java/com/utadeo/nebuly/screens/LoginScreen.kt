package com.utadeo.nebuly.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.ActionButton
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.ui.theme.AppDimens
import com.utadeo.nebuly.utils.loginUser
import com.utadeo.nebuly.utils.validateLoginInputs

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToComienzo: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // FocusRequesters para navegación entre campos
    val passwordFocusRequester = remember { FocusRequester() }

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

        // Contenido scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = AppDimens.paddingHorizontal()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Botón para volver atrás
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppDimens.paddingVertical()),
                horizontalArrangement = Arrangement.Start
            ) {
                BackButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(AppDimens.backButtonSize())
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.spacingExtraLarge()))

            // Imagen de perfil
            Image(
                painter = painterResource(R.drawable.ic_persona),
                contentDescription = "Icono usuario",
                modifier = Modifier
                    .size(AppDimens.avatarSize())
                    .padding(bottom = AppDimens.spacingMedium())
            )

            // Campos de texto
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppDimens.spacingMedium()),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier.padding(AppDimens.spacingMedium())
                ) {
                    // ✅ Campo de Email con teclado de email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        label = { Text("Correo electrónico", color = Color.White) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next // ✅ Botón "Siguiente"
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = {
                                passwordFocusRequester.requestFocus() // ✅ Ir al siguiente campo
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppDimens.spacingMedium()),
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

                    // ✅ Campo de Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = { Text("Contraseña", color = Color.White) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done // ✅ Botón "Listo"
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus() // ✅ Cerrar teclado
                                // Ejecutar login automáticamente
                                if (validateLoginInputs(email, password)) {
                                    loginUser(auth, email, password, context) { loading, error ->
                                        isLoading = loading
                                        errorMessage = error
                                        if (error.isEmpty() && !loading) {
                                            onNavigateToComienzo()
                                        }
                                    }
                                } else {
                                    errorMessage = "Completar todos los campos correctamente"
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester) // ✅ Para recibir foco
                            .padding(bottom = AppDimens.spacingSmall()),
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
                    modifier = Modifier.padding(bottom = AppDimens.spacingMedium())
                )
            }

            // Botón de Iniciar Sesión
            ActionButton(
                text = "INICIAR SESIÓN",
                isLoading = isLoading,
                onClick = {
                    if (validateLoginInputs(email, password)) {
                        loginUser(auth, email, password, context) { loading, error ->
                            isLoading = loading
                            errorMessage = error
                            if (error.isEmpty() && !loading) {
                                onNavigateToComienzo()
                            }
                        }
                    } else {
                        errorMessage = "Completar todos los campos correctamente"
                    }
                },
                modifier = Modifier.height(AppDimens.buttonHeight())
            )

            Spacer(modifier = Modifier.height(AppDimens.spacingMedium()))

            // Link para registrarse
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "¿No tienes cuenta? Regístrate",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            // Espacio extra al final para pantallas pequeñas
            Spacer(modifier = Modifier.height(AppDimens.spacingLarge()))
        }
    }
}