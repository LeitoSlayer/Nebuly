package com.utadeo.nebuly.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )

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

    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(scale)
            //  Borde blanco brillante animado
            .border(
                BorderStroke(3.dp, shinyBorder),
                shape = RoundedCornerShape(35.dp)
            )
            .clip(RoundedCornerShape(35.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(35.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(35.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Imagen de fondo
            Image(
                painter = painterResource(R.drawable.botones_inicio_registro),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(35.dp)),
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
}
