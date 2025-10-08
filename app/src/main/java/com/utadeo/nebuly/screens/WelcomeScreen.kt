package com.utadeo.nebuly.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val stardosStencil = FontFamily(
        Font(R.font.stardos_stencil_regular, FontWeight.Normal),
        Font(R.font.stardos_stencil_bold, FontWeight.Bold)
    )

    // Fuente Aoboshi One para los botones
    val aoboshiOne = FontFamily(
        Font(R.font.aoboshi_one_regular, FontWeight.Normal)
    )


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

        // Capa semitransparente para mejorar la legibilidad
        Box(
            modifier = Modifier
                .fillMaxSize()

        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Espacio en la parte superior
            Spacer(modifier = Modifier.height(150.dp))

            // Título NEBULY
            Text(
                text = "NEBULY",
                fontSize = 90.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = stardosStencil,
                color = Color.White,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Spacer(modifier = Modifier.height(160.dp))

            // Botón de Iniciar Sesión
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(bottom = 7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.6f),
                    contentColor = Color.Black
                ),

            ) {
                Text(
                    "INICIAR SESIÓN",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = aoboshiOne
                )
            }

            // Botón de Registrarse
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(bottom = 7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.6f),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    "REGISTRARSE",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = aoboshiOne
                )
            }
        }
    }
}