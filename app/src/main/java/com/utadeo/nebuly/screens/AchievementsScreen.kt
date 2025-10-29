package com.utadeo.nebuly.screens.achievements

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.AchievementCard
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.ui.viewmodel.AchievementsViewModel
import android.util.Log

@Composable
fun AchievementsScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = remember { AchievementsViewModel() }
    val achievements by viewModel.achievements
    val unlockedAchievements by viewModel.unlockedAchievements
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    //  Log para debug
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        Log.e("AchievementsScreen", "=========================================")
        Log.e("AchievementsScreen", "Usuario ID: $userId")
        Log.e("AchievementsScreen", "=========================================")

        if (userId != null) {
            viewModel.loadAchievements(userId)
        } else {
            Log.e("AchievementsScreen", "❌ NO HAY USUARIO AUTENTICADO")
        }
    }

    //  Log de estados
    LaunchedEffect(isLoading, errorMessage, achievements.size) {
        Log.e("AchievementsScreen", "isLoading: $isLoading")
        Log.e("AchievementsScreen", "errorMessage: $errorMessage")
        Log.e("AchievementsScreen", "achievements count: ${achievements.size}")
        Log.e("AchievementsScreen", "unlockedAchievements count: ${unlockedAchievements.size}")
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo espacial
        Image(
            painter = painterResource(id = R.drawable.fondo_inicio_sesion),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Título
            Text(
                text = "Logros",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contador de logros
            Text(
                text = "${unlockedAchievements.size} de ${achievements.size} desbloqueados",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando logros...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "❌ Error al cargar logros",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage ?: "Error desconocido",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                achievements.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "📋",
                                fontSize = 64.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay logros disponibles",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    // Lista de logros
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(achievements) { achievement ->
                            AchievementCard(
                                achievement = achievement,
                                isUnlocked = viewModel.isAchievementUnlocked(achievement.id)
                            )
                        }
                    }
                }
            }
        }

        // Botón de retroceso
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, start = 20.dp),
            contentAlignment = Alignment.TopStart
        ) {
            BackButton(onClick = onBackClick)
        }
    }
}