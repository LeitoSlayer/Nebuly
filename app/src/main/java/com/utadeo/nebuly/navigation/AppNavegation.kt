package com.utadeo.nebuly.navigation

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.screens.WelcomeScreen
import com.utadeo.nebuly.screens.LoginScreen
import com.utadeo.nebuly.screens.RegisterScreen

// Estados para controlar la navegación
sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Register : Screen()
}

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }

    when (currentScreen) {
        is Screen.Welcome -> WelcomeScreen(
            onLoginClick = { currentScreen = Screen.Login },
            onRegisterClick = { currentScreen = Screen.Register }
        )
        is Screen.Login -> LoginScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Welcome },
            onNavigateToRegister = { currentScreen = Screen.Register }
        )
        is Screen.Register -> RegisterScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Welcome },
            onNavigateToLogin = { currentScreen = Screen.Login }
        )
    }
}