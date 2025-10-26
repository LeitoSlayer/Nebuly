package com.utadeo.nebuly.screens.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.components.TitleHeader
import com.utadeo.nebuly.data.models.Level
import com.utadeo.nebuly.data.repository.LearningRepository
import kotlinx.coroutines.launch

@Composable
fun NivelesScreen(
    auth: FirebaseAuth,
    moduleId: String,
    moduleName: String,
    onBackClick: () -> Unit,
    onLevelClick: (Level) -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { LearningRepository() }
    var levels by remember { mutableStateOf<List<Level>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Cargar niveles al inicio
    LaunchedEffect(moduleId) {
        scope.launch {
            auth.currentUser?.uid?.let { userId ->
                repository.getLevelsForModule(userId, moduleId).fold(
                    onSuccess = {
                        levels = it
                        isLoading = false
                    },
                    onFailure = {
                        errorMessage = it.message
                        isLoading = false
                    }
                )
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            TitleHeader(text = moduleName)

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
                else -> {
                    // Grid de niveles (2 columnas)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(levels) { level ->
                            LevelCard(
                                level = level,
                                onClick = {
                                    if (!level.isLocked) {
                                        onLevelClick(level)
                                    }
                                }
                            )
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

@Composable
private fun LevelCard(
    level: Level,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !level.isLocked) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Círculo con imagen del planeta
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    if (level.isLocked) Color(0xFF2C2C2C).copy(alpha = 0.8f)
                    else Color(0x37FFFFFF).copy(alpha = 0.3f)
                )
        ) {
            AsyncImage(
                model = level.planetImageUrl,
                contentDescription = level.planetName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Candado si está bloqueado
            if (level.isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔒",
                        fontSize = 40.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nombre del planeta
        Text(
            text = level.planetName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}