package com.utadeo.nebuly.navigation

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.screens.ComienzoScreen // ✅ Nueva importación
import com.utadeo.nebuly.screens.LoginScreen
import com.utadeo.nebuly.screens.RegisterScreen
import com.utadeo.nebuly.screens.WelcomeScreen

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Register : Screen()
    object Comienzo : Screen() // ✅ NUEVA pantalla
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
            onNavigateToRegister = { currentScreen = Screen.Register },
            onNavigateToComienzo = { currentScreen = Screen.Comienzo } // ✅ Nuevo callback
        )

        is Screen.Register -> RegisterScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Welcome },
            onNavigateToLogin = { currentScreen = Screen.Login }
        )

        // ✅ NUEVA PANTALLA: Comienzo
        is Screen.Comienzo -> ComienzoScreen(
            onBackClick = { currentScreen = Screen.Login },
            onContinueClick = {
                // Aquí puedes navegar a la siguiente pantalla principal
                // Por ejemplo: currentScreen = Screen.Home
                println("Navegar a pantalla principal desde Comienzo")
            }
        )
    }
}