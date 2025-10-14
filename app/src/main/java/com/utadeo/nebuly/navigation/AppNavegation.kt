package com.utadeo.nebuly.navigation

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.data.models.Avatar
import com.utadeo.nebuly.screens.ComienzoScreen
import com.utadeo.nebuly.screens.avatar.AvatarSelectionScreen
import com.utadeo.nebuly.screens.LoginScreen
import com.utadeo.nebuly.screens.RegisterScreen
import com.utadeo.nebuly.screens.WelcomeScreen
import com.utadeo.nebuly.screens.menu.MenuScreen
import com.utadeo.nebuly.screens.store.StoreScreen
import com.utadeo.nebuly.screens.store.AvatarDetailScreen

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Register : Screen()
    data class AvatarSelection(val userId: String) : Screen()
    object Comienzo : Screen()
    object Menu : Screen()
    object Store : Screen()
    data class AvatarDetail(val avatar: Avatar, val userCoins: Int) : Screen() // 🆕 Detalle de avatar
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
            onNavigateToAvatarSelection = { userId ->
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
            onContinueClick = { currentScreen = Screen.Menu }
        )

        is Screen.Menu -> MenuScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Comienzo },
            onStoreClick = { currentScreen = Screen.Store }
        )

        //Pantalla de tienda
        is Screen.Store -> StoreScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Menu },
            onAvatarClick = { avatar ->
                auth.currentUser?.uid?.let { userId ->
                    // Aquí pasamos las monedas que se obtienen en StoreScreen
                    // Por simplicidad, las obtendremos de nuevo en AvatarDetailScreen
                    currentScreen = Screen.AvatarDetail(avatar, 0)
                }
            }
        )

        // Pantalla de detalle de avatar
        is Screen.AvatarDetail -> {
            val screen = currentScreen as Screen.AvatarDetail
            AvatarDetailScreen(
                auth = auth,
                avatar = screen.avatar,
                userCoins = screen.userCoins,
                onBackClick = { currentScreen = Screen.Store },
                onPurchaseSuccess = {
                    currentScreen = Screen.Store
                }
            )
        }
    }
}