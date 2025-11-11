package com.utadeo.nebuly.screens.viewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.utadeo.nebuly.data.models.repository.PlanetRepository
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

data class PlanetaDetectable(
    val numero: String,
    val planetId: String,
    val nombrePlaneta: String,
    val escala: Float = 1.5f,
    val velocidadRotacion: Float = 0.5f,
    val colorPrimario: Color = Color(0xFF4CAF50),
    val colorSecundario: Color = Color(0xFF81C784)
)

val planetasDetectables = listOf(

    PlanetaDetectable("1", "mercury", "Mercurio", 1.2f, 0.8f, Color(0xFF9E9E9E), Color(0xFFBDBDBD)),
    PlanetaDetectable("2", "venus", "Venus", 1.2f, 0.8f, Color(0xFFFF9800), Color(0xFFFFB74D)),
    PlanetaDetectable("3", "earth", "Tierra", 1.2f, 0.8f, Color(0xFF2196F3), Color(0xFF64B5F6)),
    PlanetaDetectable("4", "mars", "Marte", 1.2f, 0.8f, Color(0xFFF44336), Color(0xFFE57373)),
    PlanetaDetectable("5", "jupiter", "Júpiter", 1.2f, 0.8f, Color(0xFFFF5722), Color(0xFFFF8A65)),
    PlanetaDetectable("6", "saturn", "Saturno", 1.2f, 0.8f, Color(0xFFFFC107), Color(0xFFFFD54F)),
    PlanetaDetectable("7", "uranus", "Urano", 1.2f, 0.8f, Color(0xFF00BCD4), Color(0xFF4DD0E1)),
    PlanetaDetectable("8", "neptune", "Neptuno", 1.2f, 0.8f, Color(0xFF3F51B5), Color(0xFF7986CB))
)

class ModelCameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ModelCameraScreen(
                    cacheDir = cacheDir,
                    filesDir = filesDir,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun ModelCameraScreen(
    cacheDir: File,
    filesDir: File,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraARView(
            cacheDir = cacheDir,
            filesDir = filesDir,
            onBackClick = onBackClick
        )
    } else {
        PermissionDeniedScreen()
    }
}

@Composable
fun CameraARView(
    cacheDir: File,
    filesDir: File,
    onBackClick: () -> Unit
) {
    val repository = remember { PlanetRepository() }
    val scope = rememberCoroutineScope()

    val isDisposed = remember { AtomicBoolean(false) }
    val isDisposing = remember { AtomicBoolean(false) }
    val canAnimate = remember { AtomicBoolean(false) }

    var planetaActual by remember { mutableStateOf<PlanetaDetectable?>(null) }
    var mostrarModelo3D by remember { mutableStateOf(false) }
    var isDetectionEnabled by remember { mutableStateOf(true) }
    var isLoadingModel by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var childNodes by remember { mutableStateOf<List<ModelNode>>(emptyList()) }
    var rotationY by remember { mutableStateOf(0f) }
    var animationJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine)
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    BackHandler {
        if (!isDisposed.get() && !isDisposing.getAndSet(true)) {
            Log.d("ModelCamera", "Back físico presionado")
            scope.launch {
                try {
                    canAnimate.set(false)
                    animationJob?.cancel()
                    animationJob = null
                    delay(150)

                    mostrarModelo3D = false
                    delay(200)

                    childNodes = emptyList()
                    delay(350)

                    cameraProvider?.unbindAll()
                    cameraProvider = null
                    delay(100)

                    isDisposed.set(true)
                    delay(100)

                    onBackClick()
                } catch (e: Exception) {
                    Log.e("ModelCamera", "Error en back: ${e.message}", e)
                    isDisposed.set(true)
                    onBackClick()
                }
            }
        }
    }

    fun cleanupModel() {
        Log.d("ModelCamera", "cleanupModel: Iniciando limpieza")
        canAnimate.set(false)
        animationJob?.cancel()
        animationJob = null
        mostrarModelo3D = false

        scope.launch {
            delay(100)
            childNodes = emptyList()
            Log.d("ModelCamera", "Nodos limpiados")
        }
    }

    // Cargar modelo desde Firebase
    LaunchedEffect(planetaActual, isDisposed.get()) {
        if (planetaActual != null && !isLoadingModel && !isDisposed.get()) {
            isLoadingModel = true
            isDetectionEnabled = false
            errorMessage = null
            Log.d("ModelCamera", "Cargando planeta: ${planetaActual?.nombrePlaneta}")

            try {
                cleanupModel()
                delay(200)

                val result = repository.getPlanetData(planetaActual!!.planetId)

                result.fold(
                    onSuccess = { planetData ->
                        if (isDisposed.get()) {
                            isLoadingModel = false
                            return@LaunchedEffect
                        }

                        val downloadResult = repository.downloadPlanetModel(
                            planetData.modelUrl,
                            cacheDir
                        )

                        downloadResult.fold(
                            onSuccess = { localPath ->
                                if (isDisposed.get()) {
                                    isLoadingModel = false
                                    return@fold
                                }

                                try {
                                    val sourceFile = File(localPath)
                                    if (!sourceFile.exists()) {
                                        errorMessage = "Archivo no encontrado"
                                        isLoadingModel = false
                                        planetaActual = null
                                        isDetectionEnabled = true
                                        return@fold
                                    }

                                    val modelsDir = File(filesDir, "models")
                                    if (!modelsDir.exists()) {
                                        modelsDir.mkdirs()
                                    }

                                    val destFile = File(modelsDir, sourceFile.name)
                                    sourceFile.copyTo(destFile, overwrite = true)

                                    if (isDisposed.get()) {
                                        isLoadingModel = false
                                        return@fold
                                    }

                                    val instance = modelLoader.createModelInstance(file = destFile)

                                    if (instance != null && !isDisposed.get()) {
                                        val newNode = ModelNode(
                                            modelInstance = instance,
                                            scaleToUnits = planetaActual!!.escala,
                                            centerOrigin = Position(0f, 0f, 0f)
                                        ).apply {
                                            position = Position(0f, 0f, 0f)
                                        }

                                        childNodes = listOf(newNode)
                                        cameraNode.position = Position(0f, 0f, 4.5f)
                                        cameraNode.lookAt(Position(0f, 0f, 0f))

                                        delay(100)
                                        mostrarModelo3D = true
                                        canAnimate.set(true)
                                        Log.d("ModelCamera", "Modelo cargado - Detección PAUSADA")
                                    }
                                    isLoadingModel = false
                                } catch (e: Exception) {
                                    if (!isDisposed.get()) {
                                        errorMessage = "Error: ${e.message}"
                                        planetaActual = null
                                        isDetectionEnabled = true
                                    }
                                    isLoadingModel = false
                                    Log.e("ModelCamera", "Error cargando modelo", e)
                                }
                            },
                            onFailure = { error ->
                                if (!isDisposed.get()) {
                                    errorMessage = "Error descargando: ${error.message}"
                                    planetaActual = null
                                    isDetectionEnabled = true
                                }
                                isLoadingModel = false
                            }
                        )
                    },
                    onFailure = { error ->
                        if (!isDisposed.get()) {
                            errorMessage = "Error cargando datos: ${error.message}"
                            planetaActual = null
                            isDetectionEnabled = true
                        }
                        isLoadingModel = false
                    }
                )
            } catch (e: Exception) {
                if (!isDisposed.get()) {
                    errorMessage = "Error: ${e.message}"
                    planetaActual = null
                    isDetectionEnabled = true
                }
                isLoadingModel = false
                Log.e("ModelCamera", "Error general", e)
            }
        }
    }

    // Animación de rotación
    LaunchedEffect(canAnimate.get(), childNodes) {
        animationJob?.cancel()
        animationJob = null

        if (canAnimate.get() && childNodes.isNotEmpty()) {
            Log.d("ModelCamera", "Iniciando animación de rotación")
            animationJob = launch {
                try {
                    while (isActive && canAnimate.get() && !isDisposed.get() && !isDisposing.get()) {
                        if (!canAnimate.get() || isDisposed.get() || isDisposing.get()) break

                        delay(16)

                        if (!canAnimate.get() || isDisposed.get() || isDisposing.get()) break

                        val currentNodes = childNodes
                        if (currentNodes.isEmpty()) break

                        val node = currentNodes.firstOrNull() ?: break

                        rotationY = (rotationY + (planetaActual?.velocidadRotacion ?: 0.5f)) % 360f

                        try {
                            if (canAnimate.get() && !isDisposed.get() && !isDisposing.get()) {
                                node.rotation = io.github.sceneview.math.Rotation(0f, rotationY, 0f)
                            } else {
                                break
                            }
                        } catch (_: Exception) {
                            break
                        }
                    }
                    Log.d("ModelCamera", "Animación detenida limpiamente")
                } catch (e: Exception) {
                    Log.d("ModelCamera", "Animación finalizada: ${e.message}")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Cámara
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    if (isDisposed.get() || isDisposing.get()) return@addListener

                    try {
                        cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (!isDisposed.get() && !isDisposing.get() && isDetectionEnabled) {
                                detectarNumeroPlaneta(
                                    imageProxy = imageProxy,
                                    planetas = planetasDetectables,
                                    onPlanetaDetectado = { planeta ->
                                        if (isDetectionEnabled && !isDisposed.get() && !isDisposing.get()) {
                                            planetaActual = planeta
                                            Log.d("ModelCamera", "Planeta detectado, detección pausada")
                                        }
                                    }
                                )
                            } else {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider?.unbindAll()
                        cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("ModelCamera", "Error iniciar cámara: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize().zIndex(0f)
        )

        // Modelo 3D
        if (mostrarModelo3D && childNodes.isNotEmpty() && !isDisposed.get() && !isDisposing.get() && !isLoadingModel) {
            Scene(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .zIndex(1f),
                engine = engine,
                modelLoader = modelLoader,
                cameraNode = cameraNode,
                childNodes = childNodes,
                isOpaque = false
            )
        }

        // Loading
        AnimatedVisibility(
            visible = isLoadingModel,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(32.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = planetaActual?.colorPrimario?.copy(alpha = 0.95f) ?: Color(0xFF1E1E1E)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(60.dp),
                                color = Color.White,
                                strokeWidth = 5.dp
                            )
                        }

                        Text(
                            text = "Cargando ${planetaActual?.nombrePlaneta}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Preparando experiencia 3D...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Error
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .zIndex(3f)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Error al Cargar",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        errorMessage?.let {
                            Text(
                                text = it,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoadingModel = false
                                    isDetectionEnabled = true
                                    cleanupModel()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Text("Reintentar", color = Color(0xFFD32F2F))
                            }
                            OutlinedButton(
                                onClick = {
                                    errorMessage = null
                                    planetaActual = null
                                    isDetectionEnabled = true
                                    cleanupModel()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            }
        }

        // UI Superior - Card de detección
        AnimatedVisibility(
            visible = planetaActual != null && !isLoadingModel,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(20.dp)
                .zIndex(2f)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = planetaActual?.colorPrimario ?: Color(0xFF4CAF50)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = planetaActual?.numero ?: "",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "¡Número Detectado!",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = planetaActual?.nombrePlaneta ?: "",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Instrucciones - Cuando no hay planeta
        AnimatedVisibility(
            visible = planetaActual == null && !isDisposed.get() && !isDisposing.get(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(20.dp)
                .zIndex(2f)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E).copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(24.dp)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔍",
                        fontSize = 40.sp
                    )
                    Text(
                        text = "Busca Números del 1 al 8",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Lista de planetas
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        planetasDetectables.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { planeta ->
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(
                                            containerColor = planeta.colorPrimario.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(planeta.colorPrimario),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = planeta.numero,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = planeta.nombrePlaneta,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botón para buscar otro planeta
        AnimatedVisibility(
            visible = mostrarModelo3D && planetaActual != null && !isLoadingModel && !isDisposed.get() && !isDisposing.get(),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
                .zIndex(2f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        planetaActual = null
                        errorMessage = null
                        rotationY = 0f
                        isDetectionEnabled = true
                        cleanupModel()
                        Log.d("ModelCamera", "Detección reactivada")
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(64.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = planetaActual?.colorPrimario ?: Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 24.sp
                        )
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Buscar Otro Planeta",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Detecta un nuevo número",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }

    // DISPOSE
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ModelCamera", "=== DISPOSE: Iniciando secuencia SEGURA ===")
            isDisposing.set(true)

            try {
                canAnimate.set(false)
                animationJob?.cancel()
                animationJob = null
                Log.d("ModelCamera", "✓ Animación cancelada")

                Thread.sleep(200)

                mostrarModelo3D = false
                Log.d("ModelCamera", "✓ Scene ocultada")

                Thread.sleep(300)

                childNodes = emptyList()
                Log.d("ModelCamera", "✓ Nodos limpiados")

                Thread.sleep(500)

                try {
                    cameraProvider?.unbindAll()
                    cameraProvider = null
                    Log.d("ModelCamera", "✓ Cámara detenida")
                } catch (e: Exception) {
                    Log.e("ModelCamera", "Error deteniendo cámara: ${e.message}")
                }

                try {
                    cameraExecutor.shutdown()
                    if (!cameraExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                        cameraExecutor.shutdownNow()
                    }
                    Log.d("ModelCamera", "✓ Executor cerrado")
                } catch (e: Exception) {
                    Log.e("ModelCamera", "Error cerrando executor: ${e.message}")
                }

                isDisposed.set(true)

                Log.d("ModelCamera", "=== DISPOSE COMPLETADO ===")
            } catch (e: Exception) {
                Log.e("ModelCamera", "Error CRÍTICO en dispose: ${e.message}", e)
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun detectarNumeroPlaneta(
    imageProxy: ImageProxy,
    planetas: List<PlanetaDetectable>,
    onPlanetaDetectado: (PlanetaDetectable) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                for (block in visionText.textBlocks) {
                    val text = block.text
                    for (planeta in planetas) {
                        if (text.contains(planeta.numero)) {
                            onPlanetaDetectado(planeta)
                            return@addOnSuccessListener
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ModelCamera", "Error en detección: ${e.message}")
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Composable
fun PermissionDeniedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E),
                        Color(0xFF000051)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 56.sp)
                }

                Text(
                    "Permiso de Cámara",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )

                Text(
                    "Esta aplicación necesita acceso a tu cámara para detectar números y mostrar planetas en realidad aumentada.",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF424242),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Por favor, habilita el permiso en la configuración de tu dispositivo.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}