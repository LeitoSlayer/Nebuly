package com.utadeo.nebuly.navigation

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.screens.WelcomeScreen
import com.utadeo.nebuly.screens.LoginScreen
import com.utadeo.nebuly.screens.RegisterScreen
import com.utadeo.nebuly.screens.avatar.AvatarSelectionScreen

// ✅ Estados para controlar la navegación
sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Register : Screen()
    data class AvatarSelection(val userId: String) : Screen() // ✅ NUEVO: Pantalla de avatares con userId
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
            onNavigateToLogin = { currentScreen = Screen.Login },
            onNavigateToAvatarSelection = { userId -> // ✅ NUEVO callback
                currentScreen = Screen.AvatarSelection(userId)
            }
        )

        // ✅ NUEVA PANTALLA: Selección de avatares
        is Screen.AvatarSelection -> {
            val userId = (currentScreen as Screen.AvatarSelection).userId
            AvatarSelectionScreen(
                userId = userId,
                onBackClick = { currentScreen = Screen.Register }
            )
        }
    }
}