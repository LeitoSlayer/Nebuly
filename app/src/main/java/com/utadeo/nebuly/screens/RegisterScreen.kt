package com.utadeo.nebuly.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.ActionButton
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.ui.theme.AppDimens
import com.utadeo.nebuly.utils.registerUser
import com.utadeo.nebuly.utils.validateRegisterInputs

@Composable
fun RegisterScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAvatarSelection: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var currentAvatarUrl by remember { mutableStateOf<String?>(null) }
    var currentUserId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // FocusRequesters para navegación entre campos
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }

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

            Spacer(modifier = Modifier.height(AppDimens.spacingLarge()))

            // Box con imagen de perfil + botón de editar
            Box(
                modifier = Modifier
                    .size(AppDimens.avatarSize())
                    .padding(bottom = AppDimens.spacingMedium()),
                contentAlignment = Alignment.Center
            ) {
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
                        modifier = Modifier.fillMaxSize()
                    )
                }

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
                    .padding(vertical = AppDimens.spacingMedium()),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Column(
                    modifier = Modifier.padding(AppDimens.spacingMedium())
                ) {
                    // ✅ Campo de Nombre de Usuario
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = ""
                        },
                        label = { Text("Nombre de usuario", color = Color.White) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { emailFocusRequester.requestFocus() }
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
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { passwordFocusRequester.requestFocus() }
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester)
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
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                // Ejecutar registro automáticamente
                                if (validateRegisterInputs(email, password, username)) {
                                    registerUser(auth, email, password, username, context) { loading, error, userId ->
                                        isLoading = loading
                                        errorMessage = error
                                        if (userId != null && error.isEmpty()) {
                                            currentUserId = userId
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
                            .focusRequester(passwordFocusRequester)
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

            // Botón de Registro
            ActionButton(
                text = "REGISTRARSE",
                isLoading = isLoading,
                onClick = {
                    if (validateRegisterInputs(email, password, username)) {
                        registerUser(auth, email, password, username, context) { loading, error, userId ->
                            isLoading = loading
                            errorMessage = error
                            if (userId != null && error.isEmpty()) {
                                currentUserId = userId
                            }
                        }
                    } else {
                        errorMessage = "Completar todos los campos correctamente"
                    }
                },
                modifier = Modifier.height(AppDimens.buttonHeight())
            )

            Spacer(modifier = Modifier.height(AppDimens.spacingLarge()))
        }
    }
}