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
import androidx.compose.ui.res.stringResource

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
    val passwordFocusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.fondo_inicio_sesion),
            contentDescription = "Fondo de inicio de sesión",
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

            // Logo de la app
            Image(
                painter = painterResource(R.drawable.logo_nebuly_app),
                contentDescription = "Logo Nebuly",
                modifier = Modifier
                    .size(280.dp)
                    .padding(bottom = AppDimens.spacingLarge())
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = AppDimens.spacingMedium()),
                colors = CardDefaults.cardColors(
                    // Morado espacial claro
                    containerColor = Color(0x743C2C81).copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Campo de Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = ""
                        },
                        label = {
                            Text(
                                stringResource(R.string.campo_correo),
                                color = Color(0xFFFFFFFF)
                            )
                        },
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
                            .padding(bottom = 16.dp),
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = Color(0xFF2D1B4E),
                            focusedTextColor = Color(0xFF2D1B4E),
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedLabelColor = Color(0xFF2D1B4E).copy(alpha = 0.7f),
                            focusedLabelColor = Color(0xFF7B68EE),
                            cursorColor = Color(0xFF7B68EE),
                            unfocusedIndicatorColor = Color(0xFF7B68EE).copy(alpha = 0.5f),
                            focusedIndicatorColor = Color(0xFF7B68EE)
                        )
                    )

                    // Campo de Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = {
                            Text(
                                stringResource(R.string.campo_contraseña),
                                color = Color(0xFFFFFFFF)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
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
                            .focusRequester(passwordFocusRequester),
                        enabled = !isLoading,
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = TextFieldDefaults.colors(
                            unfocusedTextColor = Color(0xFFFFFFFF),
                            focusedTextColor = Color(0xFFFFFFFF),
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedLabelColor = Color(0xFF2D1B4E).copy(alpha = 0.7f),
                            focusedLabelColor = Color(0xFF7B68EE),
                            cursorColor = Color(0xFF7B68EE),
                            unfocusedIndicatorColor = Color(0xFF7B68EE).copy(alpha = 0.5f),
                            focusedIndicatorColor = Color(0xFF7B68EE)
                        )
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(vertical = AppDimens.spacingMedium())
                )
            }

            ActionButton(
                text = stringResource(R.string.iniciar_sesion),
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

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = stringResource(R.string.sin_cuenta),
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.spacingLarge()))
        }
    }
}