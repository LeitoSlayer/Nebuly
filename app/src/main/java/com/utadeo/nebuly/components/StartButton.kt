package com.utadeo.nebuly.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utadeo.nebuly.R

@Composable
fun StartButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val aoboshiOne = FontFamily(Font(R.font.aoboshi_one_regular, FontWeight.Normal))

    // Animación del brillo
    val infiniteTransition = rememberInfiniteTransition(label = "shine")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shine_offset"
    )

    // Gradiente blanco brillante en movimiento
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
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = if (isLoading) 0.5f else 1f
        )

        // Texto del botón
        Text(
            text = text,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = aoboshiOne,
            color = Color.White
        )
    }
}