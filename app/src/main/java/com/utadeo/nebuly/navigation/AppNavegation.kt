// 📁 Ubicación: app/src/main/kotlin+java/com/utadeo/nebuly/navigation/AppNavigation.kt

package com.utadeo.nebuly.navigation

import android.content.Intent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
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
import com.utadeo.nebuly.screens.viewer.ModelViewerActivity // 🆕

sealed class Screen {
    object Welcome : Screen()
    object Login : Screen()
    object Register : Screen()
    data class AvatarSelection(val userId: String, val returnTo: Screen? = null) : Screen()
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

    // Pantalla de Logros
    object Achievements : Screen()

    // 🆕 Pantalla del Visor 3D
    object ModelViewer : Screen()
}

@Composable
fun AppNavigation(auth: FirebaseAuth) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var previousScreen by remember { mutableStateOf<Screen?>(null) }
    val context = LocalContext.current

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
                // 🆕 Abrir el visor 3D
                val intent = Intent(context, ModelViewerActivity::class.java)
                context.startActivity(intent)
            },
            onAvatarClick = {
                auth.currentUser?.uid?.let { userId ->
                    previousScreen = currentScreen
                    currentScreen = Screen.AvatarSelection(
                        userId = userId,
                        returnTo = Screen.Menu
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
                onStartQuiz = {
                    currentScreen = Screen.Question(
                        levelId = screen.levelId,
                        moduleId = screen.moduleId,
                        moduleName = screen.moduleName
                    )
                }
            )
        }

        // Pantalla de Cuestionario
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

        // Pantalla de Logros
        is Screen.Achievements -> AchievementsScreen(
            auth = auth,
            onBackClick = { currentScreen = Screen.Menu }
        )

        // 🆕 Pantalla del Visor 3D (caso simplificado)
        is Screen.ModelViewer -> {
            // Esta pantalla solo se usa como trigger para abrir la Activity
            // El Intent ya se ejecutó en el MenuScreen, así que volvemos al menú
            LaunchedEffect(Unit) {
                currentScreen = Screen.Menu
            }
        }
    }
}