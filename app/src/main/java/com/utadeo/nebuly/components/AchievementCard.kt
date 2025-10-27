package com.utadeo.nebuly.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.utadeo.nebuly.data.models.Achievement

@Composable
fun AchievementCard(
    achievement: Achievement,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = if (isUnlocked) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1A237E).copy(alpha = 0.9f),
                            Color(0xFF0D47A1).copy(alpha = 0.85f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2C2C2C).copy(alpha = 0.7f),
                            Color(0xFF1A1A1A).copy(alpha = 0.65f)
                        )
                    )
                }
            )
            .border(
                width = 2.dp,
                color = if (isUnlocked) Color(0xFF4A90E2) else Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            // Contenedor de imagen del logro con candado superpuesto
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Color(0xFF7B68EE).copy(alpha = 0.3f)
                        else Color(0xFF3C3C3C).copy(alpha = 0.5f)
                    )
                    .border(
                        width = 3.dp,
                        color = if (isUnlocked) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Imagen del planeta desde URL
                if (achievement.imageUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = achievement.imageUrl,
                        contentDescription = achievement.planetName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .then(
                                if (!isUnlocked) Modifier.blur(8.dp) else Modifier
                            ),
                        contentScale = ContentScale.Crop,
                        colorFilter = if (!isUnlocked) {
                            ColorFilter.tint(Color.Gray.copy(alpha = 0.6f))
                        } else null,
                        loading = {
                            // Mientras carga, mostrar emoji
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getPlanetEmoji(achievement.planetName),
                                    fontSize = 48.sp
                                )
                            }
                        },
                        error = {
                            // Si falla, mostrar emoji
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getPlanetEmoji(achievement.planetName),
                                    fontSize = 48.sp,
                                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    )
                } else {
                    // Sin URL, mostrar emoji
                    Text(
                        text = getPlanetEmoji(achievement.planetName),
                        fontSize = 48.sp,
                        color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                }

                // Candado superpuesto para logros bloqueados
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 42.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del logro
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = achievement.planetName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isUnlocked) achievement.description else "???",
                    fontSize = 14.sp,
                    color = if (isUnlocked) Color.White.copy(alpha = 0.8f)
                    else Color.White.copy(alpha = 0.4f),
                    lineHeight = 18.sp
                )

                if (isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✅",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Desbloqueado",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Obtiene el emoji del planeta según su nombre
 */
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
        "sistema solar" -> "🌌"
        else -> "🪐"
    }
}