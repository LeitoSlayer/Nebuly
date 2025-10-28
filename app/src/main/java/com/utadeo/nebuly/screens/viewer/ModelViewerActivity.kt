package com.utadeo.nebuly.screens.viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.romainguy.kotlin.math.*
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.math.Position
import kotlin.math.*

class ModelViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ModelViewerScreen()
            }
        }
    }
}

data class InfoTierra(
    val id: String,
    val titulo: String,
    val latitud: Float,
    val longitud: Float,
    val descripcion: String,
    val emoji: String
)

val informacionTierra = listOf(
    InfoTierra(
        id = "tamano",
        titulo = "Tamaño",
        latitud = 0f,
        longitud = 180f,
        descripcion = "Mido 12,742 km de ancho. ¡Soy el planeta rocoso más grande de nuestro sistema!",
        emoji = "🔍"
    ),
    InfoTierra(
        id = "distancia",
        titulo = "Distancia del Sol",
        latitud = 35f,
        longitud = -128.6f,
        descripcion = "Estoy a 150 millones de km del Sol. ¡Es la distancia perfecta para no tener demasiado calor ni demasiado frío!",
        emoji = "🌞"
    ),
    InfoTierra(
        id = "orbita",
        titulo = "Viaje alrededor del Sol",
        latitud = -25f,
        longitud = -77.2f,
        descripcion = "Tardo 365 días en dar una vuelta completa alrededor del sol.",
        emoji = "💫"
    ),
    InfoTierra(
        id = "rotacion",
        titulo = "Días",
        latitud = 50f,
        longitud = -25.8f,
        descripcion = "Giro sobre mí mismo en 24 horas.",
        emoji = "⏳"
    ),
    InfoTierra(
        id = "temperatura",
        titulo = "Temperatura",
        latitud = -40f,
        longitud = 25.6f,
        descripcion = "Tengo una temperatura perfecta para la vida, con una media de 15 °C; dependiendo de los lugares unos pueden ser muy fríos y otros muy calientes.",
        emoji = "🌡️"
    ),
    InfoTierra(
        id = "luna",
        titulo = "Lunas",
        latitud = 15f,
        longitud = 77f,
        descripcion = "Tengo una luna preciosa y grande.",
        emoji = "🌛"
    ),
    InfoTierra(
        id = "superpoder",
        titulo = "Superpoder",
        latitud = -55f,
        longitud = 128.4f,
        descripcion = "¡Soy el único planeta con agua líquida en la superficie y con vida! Tengo millones de especies de animales, plantas y otros seres.",
        emoji = "✨"
    )
)

@Composable
fun ModelViewerScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoSeleccionada by remember { mutableStateOf<InfoTierra?>(null) }
    var infosDescubiertas by remember { mutableStateOf(setOf<String>()) }
    var puntosProyectados by remember { mutableStateOf<Map<String, PuntoProyectado>>(emptyMap()) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine)

    LaunchedEffect(Unit) {
        try {
            val instance = modelLoader.createModelInstance(
                assetFileLocation = "models/earth.glb"
            )

            if (instance != null) {
                modelNode = ModelNode(
                    modelInstance = instance,
                    scaleToUnits = 2f,
                    centerOrigin = Position(0f, 0f, 0f)
                ).apply {
                    position = Position(x = 0f, y = 0f, z = 0f)
                }

                cameraNode.position = Position(x = 0f, y = 0f, z = 6f)
                cameraNode.lookAt(Position(0f, 0f, 0f))
            } else {
                errorMessage = "No se pudo crear la instancia del modelo"
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (modelNode != null) {
            Scene(
                modifier = Modifier.fillMaxSize(),
                engine = engine,
                modelLoader = modelLoader,
                cameraNode = cameraNode,
                childNodes = listOf(modelNode!!),
                cameraManipulator = io.github.sceneview.rememberCameraManipulator(),
                onFrame = {
                    val nuevasProyecciones = mutableMapOf<String, PuntoProyectado>()

                    informacionTierra.forEach { info ->
                        val proyeccion = calcularProyeccionPunto(
                            info = info,
                            modelNode = modelNode!!,
                            cameraNode = cameraNode,
                            radioEsfera = 1f
                        )

                        if (proyeccion != null) {
                            nuevasProyecciones[info.id] = proyeccion
                        }
                    }

                    puntosProyectados = nuevasProyecciones
                }
            )

            PuntosInteractivosOverlay(
                puntosProyectados = puntosProyectados,
                informacionTierra = informacionTierra,
                infosDescubiertas = infosDescubiertas,
                onInfoClick = { info ->
                    infoSeleccionada = info
                    infosDescubiertas = infosDescubiertas + info.id
                }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = Color.White
                    )
                    Text(
                        text = "Cargando planeta...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Error",
                        color = Color.Red,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Error desconocido",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (!isLoading && errorMessage == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Explora la Tierra",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rota para descubrir información",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Text(
                                text = "${infosDescubiertas.size}/${informacionTierra.size} descubiertos",
                                color = Color(0xFF4CAF50),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (infoSeleccionada != null) {
        DialogoInformacion(
            info = infoSeleccionada!!,
            onDismiss = { infoSeleccionada = null }
        )
    }
}

data class PuntoProyectado(
    val screenX: Float,
    val screenY: Float,
    val esVisible: Boolean
)

fun calcularProyeccionPunto(
    info: InfoTierra,
    modelNode: ModelNode,
    cameraNode: io.github.sceneview.node.CameraNode,
    radioEsfera: Float
): PuntoProyectado? {
    try {
        val latRad = Math.toRadians(info.latitud.toDouble()).toFloat()
        val lonRad = Math.toRadians(info.longitud.toDouble()).toFloat()

        val x = radioEsfera * cos(latRad) * sin(lonRad)
        val y = radioEsfera * sin(latRad)
        val z = radioEsfera * cos(latRad) * cos(lonRad)

        val modelMatrix = modelNode.worldTransform
        val puntoLocal = Float4(x, y, z, 1f)
        val puntoMundial = modelMatrix * puntoLocal

        val camPos = cameraNode.worldPosition
        val camPosVec = Float3(camPos.x, camPos.y, camPos.z)
        val puntoPosVec = Float3(puntoMundial.x, puntoMundial.y, puntoMundial.z)

        val centroEsfera = Float3(
            modelNode.worldPosition.x,
            modelNode.worldPosition.y,
            modelNode.worldPosition.z
        )
        val normalSuperficie = normalize(puntoPosVec - centroEsfera)
        val direccionHaciaCamara = normalize(camPosVec - puntoPosVec)
        val dotProduct = dot(normalSuperficie, direccionHaciaCamara)

        if (dotProduct <= 0.1f) return null

        val vectorCamaraPunto = puntoPosVec - camPosVec
        val forward = normalize(centroEsfera - camPosVec)
        val worldUp = Float3(0f, 1f, 0f)
        val right = normalize(cross(worldUp, forward))
        val up = cross(forward, right)

        val distancia = dot(vectorCamaraPunto, forward)
        if (distancia <= 0.1f) return null

        val xCam = dot(vectorCamaraPunto, right)
        val yCam = dot(vectorCamaraPunto, up)

        val fov = 0.8f
        val aspect = 1.0f

        val screenX = (xCam / (distancia * fov * aspect)) * 0.5f + 0.5f
        val screenY = (-yCam / (distancia * fov)) * 0.5f + 0.5f

        if (screenX >= -0.1f && screenX <= 1.1f && screenY >= -0.1f && screenY <= 1.1f) {
            return PuntoProyectado(screenX, screenY, true)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}

@Composable
fun PuntosInteractivosOverlay(
    puntosProyectados: Map<String, PuntoProyectado>,
    informacionTierra: List<InfoTierra>,
    infosDescubiertas: Set<String>,
    onInfoClick: (InfoTierra) -> Unit
) {
    var anchoPantalla by remember { mutableStateOf(0f) }
    var altoPantalla by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                anchoPantalla = coordinates.size.width.toFloat()
                altoPantalla = coordinates.size.height.toFloat()
            }
    ) {
        if (anchoPantalla > 0 && altoPantalla > 0) {
            val centroX = anchoPantalla / 2f
            val centroY = altoPantalla / 2f

            var puntoMasCercano: Pair<InfoTierra, PuntoProyectado>? = null
            var distanciaMinima = Float.MAX_VALUE

            informacionTierra.forEach { info ->
                val proyeccion = puntosProyectados[info.id]

                if (proyeccion != null && proyeccion.esVisible) {
                    val x = proyeccion.screenX * anchoPantalla
                    val y = proyeccion.screenY * altoPantalla

                    val distancia = kotlin.math.sqrt(
                        (x - centroX) * (x - centroX) +
                                (y - centroY) * (y - centroY)
                    )

                    if (distancia < distanciaMinima) {
                        distanciaMinima = distancia
                        puntoMasCercano = Pair(info, proyeccion)
                    }
                }
            }

            puntoMasCercano?.let { (info, proyeccion) ->
                val x = (proyeccion.screenX * anchoPantalla).toInt()
                val y = (proyeccion.screenY * altoPantalla).toInt()
                val esDescubierto = infosDescubiertas.contains(info.id)

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x - 28, y - 28) }
                ) {
                    MarcadorInfo(
                        info = info,
                        esDescubierto = esDescubierto,
                        onClick = { onInfoClick(info) }
                    )
                }
            }
        }
    }
}

@Composable
fun MarcadorInfo(
    info: InfoTierra,
    esDescubierto: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = scaleIn(initialScale = 0.3f) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (esDescubierto)
                        Color(0xFF6A1B9A).copy(alpha = 0.95f)
                    else
                        Color(0xFF9C27B0).copy(alpha = 0.95f),
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )

            Text(
                text = info.emoji,
                style = MaterialTheme.typography.titleLarge
            )

            if (!esDescubierto) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF00E676), CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DialogoInformacion(
    info: InfoTierra,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = info.emoji,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = info.titulo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = info.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "¡Atrás!",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}