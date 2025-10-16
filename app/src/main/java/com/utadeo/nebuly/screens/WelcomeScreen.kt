package com.utadeo.nebuly.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.StartButton
import com.utadeo.nebuly.ui.theme.AppDimens
import androidx.compose.foundation.layout.BoxWithConstraints

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val stardosStencil = FontFamily(
        Font(R.font.stardos_stencil_regular, FontWeight.Normal),
        Font(R.font.stardos_stencil_bold, FontWeight.Bold)
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(R.drawable.fondo_inicio),
            contentDescription = "Fondo de pantalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = AppDimens.paddingHorizontal()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Espacio en la parte superior (adaptativo)
            Spacer(modifier = Modifier.height(AppDimens.spacingExtraLarge() + 50.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AppDimens.spacingLarge()),
                contentAlignment = Alignment.Center
            ) {
                // Aquí usamos el prefijo "this@BoxWithConstraints"
                val fontSize = when {
                    this@BoxWithConstraints.maxWidth < 300.dp -> 60.sp
                    this@BoxWithConstraints.maxWidth < 400.dp -> 75.sp
                    else -> 60.sp
                }

                Text(
                    text = "NEBULY",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = stardosStencil,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }


            // Espaciado adaptativo entre título y botones
            Spacer(modifier = Modifier.height(AppDimens.spacingExtraLarge() + 80.dp))

            // Botón de Iniciar Sesión
            StartButton(
                text = "INICIAR SESIÓN",
                onClick = onLoginClick,
                modifier = Modifier.height(AppDimens.buttonHeight())
            )

            // Botón de Registrarse
            StartButton(
                text = "REGISTRARSE",
                onClick = onRegisterClick,
                modifier = Modifier.height(AppDimens.buttonHeight())
            )

            // Espacio extra al final
            Spacer(modifier = Modifier.height(AppDimens.spacingLarge()))
        }
    }
}