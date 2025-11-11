package com.utadeo.nebuly.screens.viewer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

data class PlanetaDetectable(
    val numero: String,
    val planetId: String,
    val nombrePlaneta: String,
    val escala: Float = 1.5f,
    val velocidadRotacion: Float = 0.5f
)

val planetasDetectables = listOf(
    PlanetaDetectable("1", "mercury", "Mercurio", 1.2f, 0.8f),
    PlanetaDetectable("2", "venus", "Venus", 1.2f, 0.8f),
    PlanetaDetectable("3", "earth", "Tierra", 1.2f, 0.8f),
    PlanetaDetectable("4", "mars", "Marte", 1.2f, 0.8f),
    PlanetaDetectable("5", "jupiter", "Júpiter", 1.2f, 0.8f),
    PlanetaDetectable("6", "saturn", "Saturno", 1.2f, 0.8f),
    PlanetaDetectable("7", "uranus", "Urano", 1.2f, 0.8f),
    PlanetaDetectable("8", "neptune", "Neptuno", 1.2f, 0.8f)
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

    // CRÍTICO: Usar AtomicBoolean para flags thread-safe
    val isDisposed = remember { AtomicBoolean(false) }
    val isDisposing = remember { AtomicBoolean(false) }
    val canAnimate = remember { AtomicBoolean(false) }

    var planetaActual by remember { mutableStateOf<PlanetaDetectable?>(null) }
    var mostrarModelo3D by remember { mutableStateOf(false) }
    var ultimaDeteccion by remember { mutableStateOf(0L) }
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

    // Función para limpiar modelo
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
                                        canAnimate.set(true) // Habilitar animación AHORA
                                        Log.d("ModelCamera", "Modelo cargado y mostrado")
                                    }
                                    isLoadingModel = false
                                } catch (e: Exception) {
                                    if (!isDisposed.get()) {
                                        errorMessage = "Error: ${e.message}"
                                        planetaActual = null
                                    }
                                    isLoadingModel = false
                                    Log.e("ModelCamera", "Error cargando modelo", e)
                                }
                            },
                            onFailure = { error ->
                                if (!isDisposed.get()) {
                                    errorMessage = "Error descargando: ${error.message}"
                                    planetaActual = null
                                }
                                isLoadingModel = false
                            }
                        )
                    },
                    onFailure = { error ->
                        if (!isDisposed.get()) {
                            errorMessage = "Error cargando datos: ${error.message}"
                            planetaActual = null
                        }
                        isLoadingModel = false
                    }
                )
            } catch (e: Exception) {
                if (!isDisposed.get()) {
                    errorMessage = "Error: ${e.message}"
                    planetaActual = null
                }
                isLoadingModel = false
                Log.e("ModelCamera", "Error general", e)
            }
        }
    }

    // Animación de rotación - COMPLETAMENTE REDISEÑADA
    LaunchedEffect(canAnimate.get(), childNodes) {
        animationJob?.cancel()
        animationJob = null

        if (canAnimate.get() && childNodes.isNotEmpty()) {
            Log.d("ModelCamera", "Iniciando animación de rotación")
            animationJob = launch {
                try {
                    while (isActive && canAnimate.get() && !isDisposed.get() && !isDisposing.get()) {
                        // Verificación antes de cada operación
                        if (!canAnimate.get() || isDisposed.get() || isDisposing.get()) {
                            break
                        }

                        delay(16)

                        // Verificación después del delay
                        if (!canAnimate.get() || isDisposed.get() || isDisposing.get()) {
                            break
                        }

                        // Capturar nodo localmente
                        val currentNodes = childNodes
                        if (currentNodes.isEmpty()) break

                        val node = currentNodes.firstOrNull()
                        if (node == null) break

                        // Incrementar rotación
                        rotationY = (rotationY + (planetaActual?.velocidadRotacion ?: 0.5f)) % 360f

                        // Aplicar rotación con try-catch INDIVIDUAL
                        try {
                            if (canAnimate.get() && !isDisposed.get() && !isDisposing.get()) {
                                node.rotation = io.github.sceneview.math.Rotation(0f, rotationY, 0f)
                            } else {
                                break
                            }
                        } catch (_: Exception) {
                            // Cualquier error = salir inmediatamente
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
                            if (!isDisposed.get() && !isDisposing.get()) {
                                detectarNumeroPlaneta(
                                    imageProxy = imageProxy,
                                    planetas = planetasDetectables,
                                    onPlanetaDetectado = { planeta ->
                                        val ahora = System.currentTimeMillis()
                                        if (ahora - ultimaDeteccion > 2000 && !isDisposed.get() && !isDisposing.get()) {
                                            planetaActual = planeta
                                            ultimaDeteccion = ahora
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
        if (isLoadingModel) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .zIndex(3f),
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
                        text = "Cargando ${planetaActual?.nombrePlaneta}...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Error
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .zIndex(3f)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ Error",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoadingModel = false
                                    cleanupModel()
                                }
                            ) {
                                Text("Reintentar")
                            }
                            Button(
                                onClick = {
                                    errorMessage = null
                                    planetaActual = null
                                    cleanupModel()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray
                                )
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            }
        }

        // UI Superior - Botón de regreso simplificado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
                .zIndex(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        if (!isDisposed.get() && !isDisposing.getAndSet(true)) {
                            Log.d("ModelCamera", "Botón atrás presionado")
                            scope.launch {
                                try {
                                    // 1. PRIMERO: Detener animación
                                    canAnimate.set(false)
                                    animationJob?.cancel()
                                    animationJob = null
                                    Log.d("ModelCamera", "✓ Animación cancelada")

                                    delay(150)

                                    // 2. Ocultar Scene
                                    mostrarModelo3D = false
                                    Log.d("ModelCamera", "✓ Scene ocultada")

                                    delay(200)

                                    // 3. Limpiar nodos
                                    childNodes = emptyList()
                                    Log.d("ModelCamera", "✓ Nodos limpiados")

                                    delay(350)

                                    // 4. Detener cámara
                                    cameraProvider?.unbindAll()
                                    cameraProvider = null
                                    Log.d("ModelCamera", "✓ Cámara detenida")

                                    delay(100)

                                    // 5. Marcar disposed
                                    isDisposed.set(true)

                                    delay(100)

                                    // 6. Salir
                                    Log.d("ModelCamera", "✓ Cerrando activity")
                                    onBackClick()
                                } catch (e: Exception) {
                                    Log.e("ModelCamera", "Error en back: ${e.message}", e)
                                    isDisposed.set(true)
                                    onBackClick()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = planetaActual != null && !isLoadingModel,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "¡Número ${planetaActual?.numero} Detectado!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Planeta ${planetaActual?.nombrePlaneta} apareció",
                                color = Color.White.copy(alpha = 0.95f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            if (planetaActual == null && !isDisposed.get() && !isDisposing.get()) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.75f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔍 Detecta planetas con números",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        planetasDetectables.forEach { planeta ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(planeta.nombrePlaneta, color = Color.White, fontSize = 14.sp)
                                Text(
                                    "→ Número ${planeta.numero}",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Botón para ocultar
        if (mostrarModelo3D && planetaActual != null && !isLoadingModel && !isDisposed.get() && !isDisposing.get()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .fillMaxWidth()
                    .zIndex(2f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        planetaActual = null
                        errorMessage = null
                        rotationY = 0f
                        cleanupModel()
                    },
                    modifier = Modifier.fillMaxWidth(0.85f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "🔄 Detectar Otro Planeta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Apunta a otro número para cambiar de planeta",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // DISPOSE MEJORADO
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ModelCamera", "=== DISPOSE: Iniciando secuencia SEGURA ===")
            isDisposing.set(true)

            try {
                // 1. CRÍTICO: Detener animación PRIMERO
                canAnimate.set(false)
                animationJob?.cancel()
                animationJob = null
                Log.d("ModelCamera", "✓ Animación cancelada")

                // 2. Esperar a que termine cualquier frame
                Thread.sleep(200)

                // 3. Ocultar Scene
                mostrarModelo3D = false
                Log.d("ModelCamera", "✓ Scene ocultada")

                // 4. Esperar más tiempo
                Thread.sleep(300)

                // 5. Limpiar nodos
                childNodes = emptyList()
                Log.d("ModelCamera", "✓ Nodos limpiados")

                // 6. CRÍTICO: Esperar a Engine
                Thread.sleep(500)

                // 7. Detener cámara
                try {
                    cameraProvider?.unbindAll()
                    cameraProvider = null
                    Log.d("ModelCamera", "✓ Cámara detenida")
                } catch (e: Exception) {
                    Log.e("ModelCamera", "Error deteniendo cámara: ${e.message}")
                }

                // 8. Cerrar executor
                try {
                    cameraExecutor.shutdown()
                    if (!cameraExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                        cameraExecutor.shutdownNow()
                    }
                    Log.d("ModelCamera", "✓ Executor cerrado")
                } catch (e: Exception) {
                    Log.e("ModelCamera", "Error cerrando executor: ${e.message}")
                }

                // 9. Marcar como disposed
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
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📷", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Permiso de Cámara Necesario",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Esta función necesita acceso a la cámara para detectar números y mostrar planetas en 3D.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}