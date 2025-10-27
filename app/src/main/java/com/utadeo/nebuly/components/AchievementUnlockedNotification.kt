package com.utadeo.nebuly.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.utadeo.nebuly.data.models.Achievement
import kotlinx.coroutines.delay

/**
 * Notificación animada de logro desbloqueado
 */
@Composable
fun AchievementUnlockedNotification(
    achievement: Achievement,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Animación de entrada
    LaunchedEffect(Unit) {
        visible = true
        delay(3000) // Mostrar por 3 segundos
        visible = false
        delay(300) // Esperar a que termine la animación de salida
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .scale(scale)
                .padding(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA500)
                        )
                    )
                )
                .border(
                    width = 3.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "🏆 ¡LOGRO DESBLOQUEADO! 🏆",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Imagen del planeta
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f))
                    .border(
                        width = 4.dp,
                        color = Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.imageUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = achievement.imageUrl,
                        contentDescription = achievement.planetName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Text(
                                text = getPlanetEmoji(achievement.planetName),
                                fontSize = 64.sp
                            )
                        },
                        error = {
                            Text(
                                text = getPlanetEmoji(achievement.planetName),
                                fontSize = 64.sp
                            )
                        }
                    )
                } else {
                    Text(
                        text = getPlanetEmoji(achievement.planetName),
                        fontSize = 64.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre del planeta
            Text(
                text = achievement.planetName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = achievement.description,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

private fun getPlanetEmoji(planetName: String): String {
    return when (planetName.lowercase()) {
        "mercurio" -> "☿️"
        "venus" -> "♀️"
        "tierra" -> "🌍"
        "marte" -> "♂️"
        "júpiter", "jupiter" -> "♃"
        "saturno" -> "♄"
        "urano" -> "♅"
        "neptuno" -> "♆"
        else -> "🪐"
    }
}