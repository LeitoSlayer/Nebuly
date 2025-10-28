package com.utadeo.nebuly.screens.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.components.MenuCard
import com.utadeo.nebuly.components.UserHeader

@Composable
fun MenuScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onStoreClick: () -> Unit = {},
    onLearningClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onInvestigarClick: () -> Unit = {}, // 🆕 Callback para investigar (visor 3D)
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

        // Contenido con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // UserHeader clickeable
            UserHeader(
                auth = auth,
                modifier = Modifier.padding(bottom = 20.dp),
                onClick = onAvatarClick
            )

            // Tarjeta Aprender - Navega a ruta de aprendizaje
            MenuCard(
                imageRes = R.drawable.menu_aprender,
                title = "Aprender",
                onClick = onLearningClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🆕 Tarjeta Investigar - Navega al visor 3D
            MenuCard(
                imageRes = R.drawable.menu_investigar,
                title = "Investigar",
                onClick = onInvestigarClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta Tienda
            MenuCard(
                imageRes = R.drawable.menu_tienda,
                title = "Tienda",
                onClick = onStoreClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta Logros - Navega a pantalla de logros
            MenuCard(
                imageRes = R.drawable.menu_logros,
                title = "Logros",
                onClick = onAchievementsClick
            )

            Spacer(modifier = Modifier.height(100.dp))
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