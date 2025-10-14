package com.utadeo.nebuly.screens.store

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.utadeo.nebuly.components.CoinDisplay
import com.utadeo.nebuly.data.models.Avatar
import com.utadeo.nebuly.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun AvatarDetailScreen(
    auth: FirebaseAuth,
    avatar: Avatar,
    userCoins: Int,
    onBackClick: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userRepository = remember { UserRepository() }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var isPurchasing by remember { mutableStateOf(false) }
    var purchaseError by remember { mutableStateOf<String?>(null) }
    var currentCoins by remember { mutableStateOf(userCoins) }
    val scope = rememberCoroutineScope()

    val canAfford = currentCoins >= avatar.requiredCoins

    Box(
        modifier = modifier.fillMaxSize()
    ) {
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
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Monedas del usuario
            CoinDisplay(
                auth = auth,
                onCoinsLoaded = { coins ->
                    currentCoins = coins
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Avatar grande
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF7B68EE).copy(alpha = 0.4f),
                                Color(0xFF4A90E2).copy(alpha = 0.2f)
                            )
                        )
                    )
                    .border(
                        width = 5.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4A90E2),
                                Color(0xFF7B68EE)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = avatar.imageUrl,
                    contentDescription = "Avatar ${avatar.id}",
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Información del precio
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Desliza hacia la derecha y dale",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "clic en el ícono de tu preferencia",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Precio del avatar
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1A1A2E).copy(alpha = 0.9f),
                                Color(0xFF16213E).copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = if (canAfford) Color(0xFFFFD700) else Color(0xFFDC143C),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable(enabled = canAfford && !isPurchasing) {
                        showConfirmDialog = true
                    }
                    .padding(horizontal = 40.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "💰",
                            fontSize = 32.sp
                        )
                        Text(
                            text = "${avatar.requiredCoins}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) Color(0xFFFFD700) else Color(0xFFDC143C)
                        )
                    }

                    Text(
                        text = "Nebulones",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de compra
            if (isPurchasing) {
                CircularProgressIndicator(
                    color = Color(0xFF4A90E2),
                    modifier = Modifier.size(48.dp)
                )
            } else if (!canAfford) {
                Text(
                    text = "⚠️ No tienes suficientes Nebulones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFDC143C),
                    textAlign = TextAlign.Center
                )
            }

            // Mensaje de error si existe
            purchaseError?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = Color(0xFFDC143C),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // Diálogo de confirmación
        AnimatedVisibility(
            visible = showConfirmDialog,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) { /* Bloquear clics de fondo */ },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .padding(32.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A2E),
                                    Color(0xFF16213E)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4A90E2),
                                    Color(0xFF7B68EE)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "¿Estás seguro que quieres\ncomprar este artículo?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        // Botones
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Botón No
                            Button(
                                onClick = {
                                    showConfirmDialog = false
                                    purchaseError = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .border(
                                        width = 2.dp,
                                        color = Color(0xFFDC143C),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Text(
                                    text = "No",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC143C)
                                )
                            }

                            // Botón Sí
                            Button(
                                onClick = {
                                    scope.launch {
                                        isPurchasing = true
                                        showConfirmDialog = false
                                        purchaseError = null

                                        auth.currentUser?.uid?.let { userId ->
                                            val result = userRepository.purchaseAvatar(
                                                userId = userId,
                                                avatarId = avatar.id,
                                                cost = avatar.requiredCoins
                                            )

                                            result.onSuccess {
                                                Log.d("AvatarDetailScreen", "Compra exitosa")
                                                // Pequeño delay para mostrar feedback
                                                delay(500)
                                                onPurchaseSuccess()
                                            }.onFailure { e ->
                                                Log.e("AvatarDetailScreen", "Error en compra", e)
                                                purchaseError = e.message ?: "Error al comprar"
                                                isPurchasing = false
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF4A90E2),
                                                Color(0xFF7B68EE)
                                            )
                                        ),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                Text(
                                    text = "Sí",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
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