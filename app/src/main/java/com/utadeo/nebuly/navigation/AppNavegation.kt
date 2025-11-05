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
import com.utadeo.nebuly.screens.learning.RutaAprendizajeScreen
import com.utadeo.nebuly.screens.learning.NivelesScreen
import com.utadeo.nebuly.screens.learning.PlanetDetailScreen
import com.utadeo.nebuly.screens.learning.QuestionScreen
import com.utadeo.nebuly.screens.achievements.AchievementsScreen
import com.utadeo.nebuly.screens.viewer.PlanetSelectionScreen

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Register : Screen()
    data class AvatarSelection(val userId: String, val returnTo: Screen? = null) : Screen()
    object Comienzo : Screen()
    object Menu : Screen()
    object Store : Screen()
    data class AvatarDetail(val avatar: Avatar, val userCoins: Int) : Screen()

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

    object Achievements : Screen()
    object PlanetSelection : Screen()
}

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    val initialScreen = if (auth.currentUser != null) {
        Screen.Menu
    } else {
        Screen.Welcome
    }

    var currentScreen by remember { mutableStateOf<Screen>(initialScreen) }
    var previousScreen by remember { mutableStateOf<Screen?>(null) }

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
            onAchievementsClick = { currentScreen = Screen.Achievements },
            onInvestigarClick = {
                currentScreen = Screen.PlanetSelection
            },
            onAvatarClick = {
                auth.currentUser?.uid?.let { userId ->
                    previousScreen = currentScreen
                    currentScreen = Screen.AvatarSelection(
                        userId = userId,
                        returnTo = Screen.Menu
                    )
                }
            },
            onLogoutClick = {
                currentScreen = Screen.Welcome
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

        is Screen.RutaAprendizaje -> RutaAprendizajeScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Menu },
            onModuleClick = { module ->
                currentScreen = Screen.Niveles(module.id, module.name)
            },
            onAvatarClick = {
                auth.currentUser?.uid?.let { userId ->
                    previousScreen = currentScreen
                    currentScreen = Screen.AvatarSelection(
                        userId = userId,
                        returnTo = Screen.RutaAprendizaje
                    )
                }
            }
        )

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
                onStartQuiz = {
                    currentScreen = Screen.Question(
                        levelId = screen.levelId,
                        moduleId = screen.moduleId,
                        moduleName = screen.moduleName
                    )
                }
            )
        }

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

        is Screen.Achievements -> AchievementsScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Menu }
        )

        is Screen.PlanetSelection -> PlanetSelectionScreen(
            onBackClick = { currentScreen = Screen.Menu }
        )
    }
}