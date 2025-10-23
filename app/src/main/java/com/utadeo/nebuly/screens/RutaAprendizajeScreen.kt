package com.utadeo.nebuly.screens.learning

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.utadeo.nebuly.components.UserHeader
import com.utadeo.nebuly.data.models.LearningModule
import com.utadeo.nebuly.data.repository.LearningRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable

@Composable
fun RutaAprendizajeScreen(
    auth: FirebaseAuth,
    onBackClick: () -> Unit,
    onModuleClick: (LearningModule) -> Unit,
    onAvatarClick: () -> Unit, // 🆕 Callback para ir a selección de avatares
    modifier: Modifier = Modifier
) {
    val repository = remember { LearningRepository() }
    var modules by remember { mutableStateOf<List<LearningModule>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Cargar módulos al inicio
    LaunchedEffect(Unit) {
        scope.launch {
            auth.currentUser?.uid?.let { userId ->
                repository.getModulesForUser(userId).fold(
                    onSuccess = {
                        modules = it
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
            // UserHeader clickeable
            UserHeader(
                auth = auth,
                modifier = Modifier.padding(horizontal = 24.dp),
                onClick = onAvatarClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            TitleHeader(text = "Ruta de aprendizaje")

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
                    // Lista de módulos
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(modules) { module ->
                            ModuleCard(
                                module = module,
                                onClick = {
                                    if (!module.isLocked) {
                                        onModuleClick(module)
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
private fun ModuleCard(
    module: LearningModule,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (module.isLocked) Color(0xFF2C2C2C).copy(alpha = 0.8f)
                else Color(0xFF1A237E).copy(alpha = 0.9f)
            )
            .clickable(enabled = !module.isLocked) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del módulo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                AsyncImage(
                    model = module.imageUrl,
                    contentDescription = module.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Candado si está bloqueado
                if (module.isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 32.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del módulo
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            )
            {
                Text(
                    text = module.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (!module.isLocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = module.description,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 2
                    )
                }
            }
        }
    }
}