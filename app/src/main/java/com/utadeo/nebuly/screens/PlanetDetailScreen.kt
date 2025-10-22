package com.utadeo.nebuly.screens.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.components.TitleHeader
import com.utadeo.nebuly.data.models.PlanetLevel
import com.utadeo.nebuly.data.repository.LearningRepository
import kotlinx.coroutines.launch

@Composable
fun PlanetDetailScreen(
    auth: FirebaseAuth,
    levelId: String,
    moduleId: String,  // 🆕 Recibimos el módulo
    moduleName: String,  // 🆕 Recibimos el nombre del módulo
    onBackClick: () -> Unit,
    onStartQuiz: () -> Unit,  // 🆕 Navegar al cuestionario
    modifier: Modifier = Modifier
) {
    val repository = remember { LearningRepository() }
    var planetLevel by remember { mutableStateOf<PlanetLevel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Cargar información del planeta
    LaunchedEffect(levelId) {
        scope.launch {
            repository.getPlanetLevel(levelId).fold(
                onSuccess = {
                    planetLevel = it
                    isLoading = false
                },
                onFailure = {
                    errorMessage = it.message
                    isLoading = false
                }
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo espacial
        Image(
            painter = painterResource(id = R.drawable.fondo_inicio_sesion),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "❌ Error al cargar nivel",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
            planetLevel != null -> {
                val planet = planetLevel!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, bottom = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título
                    TitleHeader(text = "Nivel ${planet.levelNumber}")

                    Spacer(modifier = Modifier.height(32.dp))

                    // Imagen del planeta
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        AsyncImage(
                            model = planet.planetImageUrl,
                            contentDescription = planet.planetName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nombre del planeta
                    Text(
                        text = planet.planetName,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Descripción
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1A237E).copy(alpha = 0.8f))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = planet.description,
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Justify,
                            lineHeight = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Recompensa TOTAL (250 nebulones por las 5 preguntas)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50).copy(alpha = 0.8f),
                                        Color(0xFF8BC34A).copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🏆 Recompensa total:",
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "250 Nebulones 🪙",
                                fontSize = 20.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Info adicional
                    Text(
                        text = "5 preguntas × 50 nebulones c/u",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón para iniciar cuestionario (CAMBIADO)
                    Button(
                        onClick = { onStartQuiz() },  // 🆕 Navegar al quiz
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .height(56.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4A90E2),
                                        Color(0xFF7B68EE)
                                    )
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "🚀 Iniciar Cuestionario",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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