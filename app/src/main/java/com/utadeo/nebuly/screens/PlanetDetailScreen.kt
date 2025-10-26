package com.utadeo.nebuly.screens.learning

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
    moduleId: String,
    moduleName: String,
    onBackClick: () -> Unit,
    onStartQuiz: () -> Unit,
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

                    // Imagen del planeta con borde animado y glow
                    PlanetImageWithGlow(
                        imageUrl = planet.planetImageUrl,
                        planetName = planet.planetName
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Nombre del planeta
                    Text(
                        text = planet.planetName,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Descripción con imagen de fondo
                    DescriptionBox(description = planet.description)

                    Spacer(modifier = Modifier.height(24.dp))


                    Spacer(modifier = Modifier.height(8.dp))

                    // Info adicional
                    Text(
                        text = "5 preguntas × 50 nebulones",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón para iniciar cuestionario mejorado
                    StartQuizButton(onClick = onStartQuiz)
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

@Composable
private fun DescriptionBox(description: String) {
    // Animación del borde
    val infiniteTransition = rememberInfiniteTransition(label = "desc_shine")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shine_offset"
    )

    val shinyBorder = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.6f),
            Color.Transparent,
            Color.White.copy(alpha = 0.6f)
        ),
        start = androidx.compose.ui.geometry.Offset(offset, 0f),
        end = androidx.compose.ui.geometry.Offset(offset + 300f, 300f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .border(
                width = 2.dp,
                brush = shinyBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(R.drawable.fondo_presentacion_planetas),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.9f
        )

        // Capa semi-transparente para mejorar legibilidad
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Texto
        Text(
            text = description,
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Justify,
            lineHeight = 24.sp,
            modifier = Modifier.padding(20.dp),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlanetImageWithGlow(
    imageUrl: String,
    planetName: String
) {
    // Animación del glow
    val infiniteTransition = rememberInfiniteTransition(label = "planet_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    // Animación del borde
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_shine"
    )

    val shinyBorder = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.9f),
            Color.Transparent,
            Color.White.copy(alpha = 0.9f)
        ),
        start = androidx.compose.ui.geometry.Offset(offset, 0f),
        end = androidx.compose.ui.geometry.Offset(offset + 200f, 200f)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(220.dp)
    ) {
        // Efecto glow
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(glowScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6C63FF).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
                .blur(25.dp)
        )

        // Imagen del planeta con borde animado
        Box(
            modifier = Modifier
                .size(200.dp)
                .border(
                    width = 3.dp,
                    brush = shinyBorder,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = planetName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Composable
private fun StartQuizButton(onClick: () -> Unit) {
    // Animación del borde
    val infiniteTransition = rememberInfiniteTransition(label = "button_shine")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shine_offset"
    )

    val shinyBorder = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.9f),
            Color.Transparent,
            Color.White.copy(alpha = 0.9f)
        ),
        start = androidx.compose.ui.geometry.Offset(offset, 0f),
        end = androidx.compose.ui.geometry.Offset(offset + 300f, 300f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(56.dp)
            .border(
                width = 3.dp,
                brush = shinyBorder,
                shape = RoundedCornerShape(35.dp)
            )
            .clip(RoundedCornerShape(35.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(R.drawable.botones_inicio_registro),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Texto del botón
        Text(
            text = "Iniciar Cuestionario",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}