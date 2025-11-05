package com.utadeo.nebuly.screens.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onInvestigarClick: () -> Unit = {},
    onAvatarClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {

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
                .padding(top = 20.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = {
                    auth.signOut()
                    onLogoutClick()
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.boton_salida),
                    contentDescription = "Cerrar sesión",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}