package com.utadeo.nebuly.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.data.models.User
import com.utadeo.nebuly.data.repository.AvatarRepository
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun UserHeader(
    auth: FirebaseAuth,
    modifier: Modifier = Modifier,
    showLevel: Boolean = true,
    avatarSize: Int = 60
) {
    val repository = remember { AvatarRepository() }
    var user by remember { mutableStateOf<User?>(null) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Cargar datos del usuario
    LaunchedEffect(auth.currentUser?.uid) {
        scope.launch {
            auth.currentUser?.uid?.let { userId ->
                try {
                    Log.d("UserHeader", "Cargando datos para usuario: $userId")

                    // Obtener usuario
                    val userResult = repository.getUser(userId)
                    userResult.onSuccess { userData ->
                        user = userData
                        Log.d("UserHeader", "Usuario cargado: ${userData.username}, Avatar: ${userData.currentAvatarId}")

                        // Obtener URL del avatar actual
                        val avatarsResult = repository.getAllAvatars()
                        avatarsResult.onSuccess { avatars ->
                            val currentAvatar = avatars.find { it.id == userData.currentAvatarId }
                            avatarUrl = currentAvatar?.imageUrl
                            Log.d("UserHeader", "Avatar URL: $avatarUrl")
                        }.onFailure { e ->
                            Log.e("UserHeader", "Error al cargar avatares", e)
                        }
                    }.onFailure { e ->
                        Log.e("UserHeader", "Error al cargar usuario", e)
                    }

                    isLoading = false
                } catch (e: Exception) {
                    Log.e("UserHeader", "Error general", e)
                    isLoading = false
                }
            }
        }
    }

    if (isLoading) {
        // Placeholder mientras carga
        Row(
            modifier = modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0x40FFFFFF),
                            Color(0x20FFFFFF)
                        )
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize.dp)
                    .clip(CircleShape)
                    .background(Color(0x40FFFFFF))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp)
                    .background(
                        Color(0x40FFFFFF),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    )
            )
        }
    } else {
        user?.let { userData ->
            Row(
                modifier = modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1A1A2E).copy(alpha = 0.9f),
                                Color(0xFF16213E).copy(alpha = 0.85f)
                            )
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4A90E2),
                                Color(0xFF7B68EE)
                            )
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(avatarSize.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF4A90E2),
                                    Color(0xFF2C5F8D)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFF4A90E2)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    if (!avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar de ${userData.username}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Avatar por defecto con inicial
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userData.username.firstOrNull()?.uppercase() ?: "U",
                                fontSize = (avatarSize / 2).sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Información del usuario
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = userData.username,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    if (showLevel) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "⭐",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Nivel ${userData.level}",
                                fontSize = 14.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Variante compacta (solo avatar y nombre, sin nivel)
@Composable
fun UserHeaderCompact(
    auth: FirebaseAuth,
    modifier: Modifier = Modifier
) {
    UserHeader(
        auth = auth,
        modifier = modifier,
        showLevel = false,
        avatarSize = 50
    )
}

// Variante grande (para páginas de perfil)
@Composable
fun UserHeaderLarge(
    auth: FirebaseAuth,
    modifier: Modifier = Modifier
) {
    UserHeader(
        auth = auth,
        modifier = modifier,
        showLevel = true,
        avatarSize = 80
    )
}