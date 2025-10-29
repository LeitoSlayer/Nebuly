package com.utadeo.nebuly.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.ActionButton
import com.utadeo.nebuly.components.BackButton
import kotlin.random.Random

@Composable
fun ComienzoScreen(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val stardosStencil = FontFamily(
        Font(R.font.stardos_stencil_regular, FontWeight.Normal),
        Font(R.font.stardos_stencil_bold, FontWeight.Bold)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "float_animation")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.fondo_inicio_sesion),
            contentDescription = "Fondo de comienzo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Estrellas animadas de fondo
        AnimatedStars()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            // Contenido centrado
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Imagen del astronauta con animación de flotación
                Image(
                    painter = painterResource(R.drawable.astronauta_cohete),
                    contentDescription = "Astronauta",
                    modifier = Modifier
                        .size(220.dp)
                        .offset(y = floatingOffset.dp)
                        .padding(bottom = 30.dp)
                )

                // Título blanco brillante
                Text(
                    text = "Bienvenido",
                    fontSize = 40.sp,
                    fontFamily = stardosStencil,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Texto descriptivo con fondo semi-transparente
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.4f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "El viaje hacia las estrellas",
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "comienza con un paso, tu",
                        color = Color.White,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "curiosidad.",
                        color = Color(0xFFFFD700),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Botón para continuar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 200.dp)
            ) {
                ActionButton(
                    text = "Continuar",
                    isLoading = false,
                    onClick = onContinueClick,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
        }

        // Botón de volver atrás
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
fun ShimmerText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = text.length.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        text.forEachIndexed { index, char ->
            val distance = kotlin.math.abs(shimmerProgress - index)
            val brightness = when {
                distance < 0.5f -> 1f
                distance < 1.5f -> 0.7f
                else -> 0.4f
            }

            Text(
                text = char.toString(),
                fontSize = fontSize,
                fontFamily = fontFamily,
                fontWeight = fontWeight,
                color = Color.White.copy(alpha = brightness)
            )
        }
    }
}

@Composable
fun AnimatedStars() {
    // Generar estrellas aleatorias
    val stars = remember {
        List(20) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 4f + 2f,
                delay = Random.nextInt(2000)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        stars.forEach { star ->
            AnimatedStar(star = star)
        }
    }
}

data class Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val delay: Int
)

@Composable
fun AnimatedStar(star: Star) {
    val infiniteTransition = rememberInfiniteTransition(label = "star_twinkle")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500 + star.delay,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(star.delay)
        ),
        label = "alpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500 + star.delay,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(star.delay)
        ),
        label = "scale"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(
                    x = maxWidth * star.x,
                    y = maxHeight * star.y
                )
                .size(star.size.dp)
                .scale(scale)
                .alpha(alpha)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
        )
    }
}