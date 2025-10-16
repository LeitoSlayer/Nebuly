package com.utadeo.nebuly.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun ComienzoScreen(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val stardosStencil = FontFamily(
        Font(R.font.stardos_stencil_regular, FontWeight.Normal),
        Font(R.font.stardos_stencil_bold, FontWeight.Bold)
    )

    val aoboshiOne = FontFamily(
        Font(R.font.aoboshi_one_regular, FontWeight.Normal)
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // imagen de fondo
        Image(
            painter = painterResource(R.drawable.fondo_inicio_sesion),
            contentDescription = "Fondo de comienzo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Logo en la parte superior
        Image(
            painter = painterResource(R.drawable.logo_nebuly_app),
            contentDescription = "Logo app",
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .padding(top = 10.dp, end = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            // Contenido  centrado
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Imagen del astronauta
                Image(
                    painter = painterResource(R.drawable.astronauta_cohete),
                    contentDescription = "Astronauta",
                    modifier = Modifier
                        .size(220.dp)
                        .padding(bottom = 30.dp)
                )

                // Título
                Text(
                    text = "Bienvenido",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontFamily = stardosStencil,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Texto descriptivo - dividido en líneas
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 40.dp)
                ) {
                    Text(
                        text = "El viaje hacia las estrellas",
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "comienza con un paso, tu",
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "curiosidad.",
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
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