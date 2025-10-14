package com.utadeo.nebuly.navigation

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.screens.ComienzoScreen
import com.utadeo.nebuly.screens.avatar.AvatarSelectionScreen
import com.utadeo.nebuly.screens.LoginScreen
import com.utadeo.nebuly.screens.RegisterScreen
import com.utadeo.nebuly.screens.WelcomeScreen

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Register : Screen()
    data class AvatarSelection(val userId: String) : Screen()
    object Comienzo : Screen()
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
            onNavigateToComienzo = { currentScreen = Screen.Comienzo }
        )

        is Screen.Register -> RegisterScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Welcome },
            onNavigateToLogin = { currentScreen = Screen.Login },
            onNavigateToAvatarSelection = { userId -> //
                currentScreen = Screen.AvatarSelection(userId)
            }
        )

        is Screen.AvatarSelection -> {
            val userId = (currentScreen as Screen.AvatarSelection).userId
            AvatarSelectionScreen(
                userId = userId,
                onBackClick = { currentScreen = Screen.Register }
            )
        }

        is Screen.Comienzo -> ComienzoScreen(
            onBackClick = { currentScreen = Screen.Login },
            onContinueClick = {
                println("Navegar a pantalla principal desde Comienzo")
            }
        )
    }
}