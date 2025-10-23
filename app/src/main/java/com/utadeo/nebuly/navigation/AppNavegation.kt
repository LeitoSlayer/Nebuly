package com.utadeo.nebuly.navigation

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.data.models.Avatar
import com.utadeo.nebuly.data.models.LearningModule
import com.utadeo.nebuly.data.models.Level
import com.utadeo.nebuly.screens.ComienzoScreen
import com.utadeo.nebuly.screens.avatar.AvatarSelectionScreen
import com.utadeo.nebuly.screens.LoginScreen
import com.utadeo.nebuly.screens.RegisterScreen
import com.utadeo.nebuly.screens.WelcomeScreen
import com.utadeo.nebuly.screens.menu.MenuScreen
import com.utadeo.nebuly.screens.store.StoreScreen
import com.utadeo.nebuly.screens.store.AvatarDetailScreen
import com.utadeo.nebuly.screens.learning.RutaAprendizajeScreen
import com.utadeo.nebuly.screens.learning.NivelesScreen
import com.utadeo.nebuly.screens.learning.PlanetDetailScreen
import com.utadeo.nebuly.screens.learning.QuestionScreen

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Register : Screen()
    data class AvatarSelection(val userId: String, val returnTo: Screen? = null) : Screen() // 🆕 returnTo
    object Comienzo : Screen()
    object Menu : Screen()
    object Store : Screen()
    data class AvatarDetail(val avatar: Avatar, val userCoins: Int) : Screen()

    // Pantallas de aprendizaje
    object RutaAprendizaje : Screen()
    data class Niveles(val moduleId: String, val moduleName: String) : Screen()
    data class PlanetDetail(
        val levelId: String,
        val moduleId: String,
        val moduleName: String
    ) : Screen()
    data class Question(
        val levelId: String,
        val moduleId: String,
        val moduleName: String
    ) : Screen()
}

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var previousScreen by remember { mutableStateOf<Screen?>(null) } // 🆕 Para recordar de dónde viene

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
                currentScreen = Screen.AvatarSelection(userId, returnTo = null)
            }
        )

        is Screen.AvatarSelection -> {
            val screen = currentScreen as Screen.AvatarSelection
            AvatarSelectionScreen(
                userId = screen.userId,
                onBackClick = {
                    // 🆕 Si tiene returnTo, volver ahí, sino al registro
                    currentScreen = screen.returnTo ?: Screen.Register
                }
            )
        }

        is Screen.Comienzo -> ComienzoScreen(
            onBackClick = { currentScreen = Screen.Login },
            onContinueClick = { currentScreen = Screen.Menu }
        )

        is Screen.Menu -> MenuScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Comienzo },
            onStoreClick = { currentScreen = Screen.Store },
            onLearningClick = { currentScreen = Screen.RutaAprendizaje },
            onAvatarClick = { // 🆕 Callback para ir a selección de avatares
                auth.currentUser?.uid?.let { userId ->
                    previousScreen = currentScreen // Guardar pantalla actual
                    currentScreen = Screen.AvatarSelection(
                        userId = userId,
                        returnTo = Screen.RutaAprendizaje // 🆕 Para volver aquí
                    )
                }
            }
        )

        is Screen.Store -> StoreScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Menu },
            onAvatarClick = { avatar ->
                auth.currentUser?.uid?.let { userId ->
                    currentScreen = Screen.AvatarDetail(avatar, 0)
                }
            }
        )

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

        // Pantalla de Ruta de Aprendizaje
        is Screen.RutaAprendizaje -> RutaAprendizajeScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Menu },
            onModuleClick = { module ->
                currentScreen = Screen.Niveles(module.id, module.name)
            },
            onAvatarClick = { // 🆕 Callback para ir a selección de avatares
                auth.currentUser?.uid?.let { userId ->
                    previousScreen = currentScreen // Guardar pantalla actual
                    currentScreen = Screen.AvatarSelection(
                        userId = userId,
                        returnTo = Screen.RutaAprendizaje // 🆕 Para volver aquí
                    )
                }
            }
        )

        // Pantalla de Niveles
        is Screen.Niveles -> {
            val screen = currentScreen as Screen.Niveles
            NivelesScreen(
                auth = auth,
                moduleId = screen.moduleId,
                moduleName = screen.moduleName,
                onBackClick = { currentScreen = Screen.RutaAprendizaje },
                onLevelClick = { level ->
                    currentScreen = Screen.PlanetDetail(
                        levelId = level.id,
                        moduleId = screen.moduleId,
                        moduleName = screen.moduleName
                    )
                }
            )
        }

        // Pantalla de Detalle del Planeta
        is Screen.PlanetDetail -> {
            val screen = currentScreen as Screen.PlanetDetail
            PlanetDetailScreen(
                auth = auth,
                levelId = screen.levelId,
                moduleId = screen.moduleId,
                moduleName = screen.moduleName,
                onBackClick = {
                    currentScreen = Screen.Niveles(screen.moduleId, screen.moduleName)
                },
                onStartQuiz = {  // 🆕 Navegar al cuestionario
                    currentScreen = Screen.Question(
                        levelId = screen.levelId,
                        moduleId = screen.moduleId,
                        moduleName = screen.moduleName
                    )
                }
            )
        }

        // 🆕 Pantalla de Cuestionario
        is Screen.Question -> {
            val screen = currentScreen as Screen.Question
            QuestionScreen(
                auth = auth,
                levelId = screen.levelId,
                moduleId = screen.moduleId,
                moduleName = screen.moduleName,
                onBackClick = {
                    currentScreen = Screen.PlanetDetail(
                        levelId = screen.levelId,
                        moduleId = screen.moduleId,
                        moduleName = screen.moduleName
                    )
                },
                onQuizComplete = {
                    currentScreen = Screen.Niveles(screen.moduleId, screen.moduleName)
                }
            )
        }
    }
}