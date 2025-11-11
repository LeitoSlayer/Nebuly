package com.utadeo.nebuly.screens.menu

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.components.MenuCard
import com.utadeo.nebuly.components.UserHeader
import com.utadeo.nebuly.data.models.repository.AuthRepository
import kotlinx.coroutines.delay

@Composable
fun MenuScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onStoreClick: () -> Unit = {},
    onLearningClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onInvestigarClick: () -> Unit = {},
    onAvatarClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val authRepository = remember { AuthRepository() }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(Unit) {
        delay(3000) // 3 segundos de pantalla de carga
        isLoading = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            // Pantalla de carga
            LoadingScreen()
        } else {
            // Contenido del menú
            MenuContent(
                auth = auth,
                authRepository = authRepository,
                onBackClick = onBackClick,
                onStoreClick = onStoreClick,
                onLearningClick = onLearningClick,
                onAchievementsClick = onAchievementsClick,
                onInvestigarClick = onInvestigarClick,
                onAvatarClick = onAvatarClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    val context = LocalContext.current

    // ImageLoader para GIFs
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    // Animación de los puntos
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500) // Cambia cada 500ms
            dotCount = (dotCount + 1) % 4 // Cicla entre 0, 1, 2, 3
        }
    }

    val dots = ".".repeat(dotCount)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // GIF de fondo con placeholder
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(R.drawable.carga_menu)
                .crossfade(true) // Transición suave
                .build(),
            contentDescription = "Cargando",
            imageLoader = imageLoader,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.fondo_sistemasolar_ruta), // Imagen temporal

        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Cargando",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(end = 4.dp)
                )

                Text(
                    text = dots,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .width(60.dp) // Espacio fijo para los puntos
                        .padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MenuContent(
    auth: FirebaseAuth,
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    onStoreClick: () -> Unit,
    onLearningClick: () -> Unit,
    onAchievementsClick: () -> Unit,
    onInvestigarClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_inicio_sesion),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            UserHeader(
                auth = auth,
                modifier = Modifier.padding(bottom = 20.dp),
                onClick = onAvatarClick
            )

            MenuCard(
                imageRes = R.drawable.menu_aprender,
                title = "Aprender",
                onClick = onLearningClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            MenuCard(
                imageRes = R.drawable.menu_investigar,
                title = "Investigar",
                onClick = onInvestigarClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            MenuCard(
                imageRes = R.drawable.menu_tienda,
                title = "Tienda",
                onClick = onStoreClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            MenuCard(
                imageRes = R.drawable.menu_logros,
                title = "Logros",
                onClick = onAchievementsClick
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp, start = 20.dp),
            contentAlignment = Alignment.TopStart
        ) {
            BackButton(onClick = onBackClick)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.5.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = {
                    authRepository.logout()
                    onLogoutClick()
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.boton_salida),
                    contentDescription = "Cerrar sesión",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(55.dp)
                )
            }
        }
    }
}