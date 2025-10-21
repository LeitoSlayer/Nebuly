package com.utadeo.nebuly.screens.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    onBackClick: () -> Unit,
    onQuizClick: () -> Unit = {}, // Para futuro cuestionario
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
                    Text(
                        text = errorMessage ?: "Error desconocido",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            planetLevel != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Título con número de nivel
                    TitleHeader(text = "Nivel ${planetLevel!!.levelNumber}")

                    Spacer(modifier = Modifier.height(24.dp))

                    // Imagen del planeta
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .border(
                                width = 4.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF4A148C),
                                        Color(0xFF6A1B9A)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        AsyncImage(
                            model = planetLevel!!.planetImageUrl,
                            contentDescription = planetLevel!!.planetName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nombre del planeta
                    Text(
                        text = planetLevel!!.planetName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Descripción con scroll
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1A237E).copy(alpha = 0.8f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp)
                        ) {
                            Text(
                                text = planetLevel!!.description,
                                fontSize = 16.sp,
                                color = Color.White,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Justify
                            )

                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón de cuestionario (deshabilitado por ahora)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2C).copy(alpha = 0.8f))
                            .clickable { /* onQuizClick() */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "❓",
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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