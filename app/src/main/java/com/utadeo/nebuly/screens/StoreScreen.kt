package com.utadeo.nebuly.screens.store

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.components.CoinDisplay
import com.utadeo.nebuly.data.models.Avatar
import com.utadeo.nebuly.data.repository.AvatarRepository
import com.utadeo.nebuly.data.repository.UserRepository
import com.utadeo.nebuly.ui.theme.AppDimens
import kotlinx.coroutines.launch
import android.util.Log

@OptIn(ExperimentalPagerApi::class)
@Composable
fun StoreScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onAvatarClick: (Avatar) -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarRepository = remember { AvatarRepository() }
    val userRepository = remember { UserRepository() }
    var avatars by remember { mutableStateOf<List<Avatar>>(emptyList()) }
    var userCoins by remember { mutableStateOf(0) }
    var unlockedAvatars by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val scrollState = rememberScrollState()

    // Cargar avatares y datos del usuario
    LaunchedEffect(auth.currentUser?.uid) {
        scope.launch {
            auth.currentUser?.uid?.let { userId ->
                try {
                    Log.d("StoreScreen", "Cargando datos para usuario: $userId")

                    val userResult = userRepository.getUser(userId)
                    userResult.onSuccess { user ->
                        userCoins = user.coins
                        unlockedAvatars = user.unlockedAvatars
                        Log.d("StoreScreen", "Usuario cargado - Coins: ${user.coins}")
                    }

                    val avatarsResult = avatarRepository.getAllAvatars()
                    avatarsResult.onSuccess { loadedAvatars ->
                        avatars = loadedAvatars.map { avatar ->
                            avatar.copy(isLocked = !unlockedAvatars.contains(avatar.id))
                        }
                        Log.d("StoreScreen", "Avatares cargados: ${avatars.size}")
                    }

                    isLoading = false
                } catch (e: Exception) {
                    Log.e("StoreScreen", "Error al cargar datos", e)
                    isLoading = false
                }
            }
        }
    }

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
                .padding(horizontal = AppDimens.paddingHorizontal()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(AppDimens.topSpacing()))

            // Header: Título y monedas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.store_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                CoinDisplay(
                    auth = auth,
                    onCoinsLoaded = { coins ->
                        userCoins = coins
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4A90E2))
                }
            } else if (avatars.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.store_no_avatars),
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Carrusel de avatares
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    HorizontalPager(
                        count = avatars.size,
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(horizontal = 40.dp)
                    ) { page ->
                        val avatar = avatars[page]
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarCard(
                                avatar = avatar,
                                userCoins = userCoins,
                                onClick = { onAvatarClick(avatar) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Indicador de página
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(avatars.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF4A90E2) else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }

        // Botón de retroceso
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = AppDimens.paddingVertical(), start = AppDimens.paddingVertical()),
            contentAlignment = Alignment.TopStart
        ) {
            BackButton(
                onClick = onBackClick,
                modifier = Modifier.size(AppDimens.backButtonSize())
            )
        }
    }
}

@Composable
fun AvatarCard(
    avatar: Avatar,
    userCoins: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canAfford = userCoins >= avatar.requiredCoins
    val isLocked = avatar.isLocked

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E).copy(alpha = 0.95f),
                        Color(0xFF16213E).copy(alpha = 0.9f)
                    )
                )
            )
            .border(
                width = 3.dp,
                brush = Brush.verticalGradient(
                    colors = if (isLocked) {
                        listOf(
                            Color(0xFF4A90E2),
                            Color(0xFF7B68EE)
                        )
                    } else {
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFA500)
                        )
                    }
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = isLocked && canAfford) { onClick() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            modifier = Modifier.fillMaxSize()
        ) {
            // Contenedor del avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF7B68EE).copy(alpha = 0.3f),
                                Color(0xFF4A90E2).copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.radialGradient(
                            colors = if (isLocked) {
                                listOf(
                                    Color(0xFF4A90E2),
                                    Color(0xFF7B68EE)
                                )
                            } else {
                                listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFFFA500)
                                )
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = avatar.imageUrl,
                    contentDescription = stringResource(id = R.string.store_avatar_desc, avatar.id),
                    modifier = Modifier
                        .fillMaxSize(0.85f)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Overlay si está bloqueado
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.store_lock_icon),
                            fontSize = 48.sp
                        )
                    }
                }
            }

            // Estado del avatar
            if (isLocked) {
                // Precio
                Row(
                    modifier = Modifier
                        .background(
                            color = if (canAfford) Color(0xFF4A90E2).copy(alpha = 0.3f)
                            else Color(0xFFDC143C).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = if (canAfford) Color(0xFFFFD700) else Color(0xFFDC143C),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.store_coin_icon),
                        fontSize = 24.sp
                    )
                    Text(
                        text = "${avatar.requiredCoins}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color(0xFFFFD700) else Color(0xFFDC143C)
                    )
                }

                // Mensaje de estado
                Text(
                    text = if (canAfford)
                        stringResource(id = R.string.store_tap_to_buy)
                    else
                        stringResource(id = R.string.store_insufficient_coins),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            } else {
                // Desbloqueado
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF4CAF50).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 2.dp,
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.store_check_icon),
                            fontSize = 20.sp,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = stringResource(id = R.string.store_unlocked),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}