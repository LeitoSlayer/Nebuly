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
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.data.models.repository.PlanetRepository
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Mapeo de números a IDs de planetas en Firebase
data class PlanetaDetectable(
    val numero: String,
    val planetId: String,
    val nombrePlaneta: String,
    val escala: Float = 1.5f,
    val velocidadRotacion: Float = 0.5f
)

// Lista de planetas detectables (1-8 corresponden a los 8 planetas)
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
    private var cameraExecutor: ExecutorService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            MaterialTheme {
                ModelCameraScreen(
                    cacheDir = cacheDir,
                    filesDir = filesDir,
                    cameraExecutor = cameraExecutor!!,
                    onBackClick = {
                        // Limpieza antes de cerrar
                        try {
                            cameraExecutor?.shutdown()
                            cameraExecutor = null
                        } catch (e: Exception) {
                            Log.e("ModelCamera", "Error cerrando executor: ${e.message}")
                        }
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            cameraExecutor?.shutdown()
            cameraExecutor = null
        } catch (e: Exception) {
            Log.e("ModelCamera", "Error en onDestroy: ${e.message}")
        }
    }
}

@Composable
fun ModelCameraScreen(
    cacheDir: File,
    filesDir: File,
    cameraExecutor: ExecutorService,
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
            cameraExecutor = cameraExecutor,
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
    cameraExecutor: ExecutorService,
    onBackClick: () -> Unit
) {
    val repository = remember { PlanetRepository() }
    val scope = rememberCoroutineScope()

    var planetaActual by remember { mutableStateOf<PlanetaDetectable?>(null) }
    var mostrarModelo3D by remember { mutableStateOf(false) }
    var ultimaDeteccion by remember { mutableStateOf(0L) }
    var isLoadingModel by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isExiting by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var rotationY by remember { mutableStateOf(0f) }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine)
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Cargar modelo desde Firebase cuando cambia el planeta
    LaunchedEffect(planetaActual) {
        if (planetaActual != null && !isLoadingModel && !isExiting) {
            isLoadingModel = true
            errorMessage = null

            scope.launch {
                try {
                    // Limpiar modelo anterior primero
                    modelNode = null
                    mostrarModelo3D = false

                    // 1. Obtener datos del planeta desde Firestore
                    val result = repository.getPlanetData(planetaActual!!.planetId)

                    result.fold(
                        onSuccess = { planetData ->
                            // 2. Descargar modelo desde Firebase Storage
                            val downloadResult = repository.downloadPlanetModel(
                                planetData.modelUrl,
                                cacheDir
                            )

                            downloadResult.fold(
                                onSuccess = { localPath ->
                                    try {
                                        val sourceFile = File(localPath)
                                        if (!sourceFile.exists()) {
                                            errorMessage = "Archivo no encontrado"
                                            isLoadingModel = false
                                            planetaActual = null
                                            return@fold
                                        }

                                        // Copiar a filesDir
                                        val modelsDir = File(filesDir, "models")
                                        if (!modelsDir.exists()) {
                                            modelsDir.mkdirs()
                                        }

                                        val destFile = File(modelsDir, sourceFile.name)
                                        sourceFile.copyTo(destFile, overwrite = true)

                                        // Cargar el modelo
                                        val instance = modelLoader.createModelInstance(file = destFile)

                                        if (instance != null && !isExiting) {
                                            modelNode = ModelNode(
                                                modelInstance = instance,
                                                scaleToUnits = planetaActual!!.escala,
                                                centerOrigin = Position(0f, 0f, 0f)
                                            ).apply {
                                                position = Position(0f, 0f, 0f)
                                            }
                                            cameraNode.position = Position(0f, 0f, 4.5f)
                                            cameraNode.lookAt(Position(0f, 0f, 0f))
                                            mostrarModelo3D = true
                                            isLoadingModel = false
                                        } else {
                                            errorMessage = "Error al crear instancia del modelo"
                                            isLoadingModel = false
                                            planetaActual = null
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e.message}"
                                        isLoadingModel = false
                                        planetaActual = null
                                        Log.e("ModelCamera", "Error cargando modelo", e)
                                    }
                                },
                                onFailure = { error ->
                                    errorMessage = "Error descargando: ${error.message}"
                                    isLoadingModel = false
                                    planetaActual = null
                                }
                            )
                        },
                        onFailure = { error ->
                            errorMessage = "Error cargando datos: ${error.message}"
                            isLoadingModel = false
                            planetaActual = null
                        }
                    )
                } catch (e: Exception) {
                    errorMessage = "Error: ${e.message}"
                    isLoadingModel = false
                    planetaActual = null
                    Log.e("ModelCamera", "Error general", e)
                }
            }
        }
    }

    // Animación de rotación
    LaunchedEffect(mostrarModelo3D, modelNode, planetaActual) {
        if (mostrarModelo3D && modelNode != null && planetaActual != null && !isExiting) {
            try {
                while (mostrarModelo3D && !isExiting) {
                    delay(16)
                    rotationY += planetaActual!!.velocidadRotacion
                    if (rotationY >= 360f) rotationY = 0f
                    modelNode?.rotation = io.github.sceneview.math.Rotation(0f, rotationY, 0f)
                }
            } catch (e: Exception) {
                Log.e("ModelCamera", "Error en animación de rotación", e)
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
                    cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!isExiting) {
                            detectarNumeroPlaneta(
                                imageProxy = imageProxy,
                                planetas = planetasDetectables,
                                onPlanetaDetectado = { planeta ->
                                    val ahora = System.currentTimeMillis()
                                    if (ahora - ultimaDeteccion > 2000 && !isExiting) {
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

                    try {
                        cameraProvider?.unbindAll()
                        cameraProvider?.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("ModelCamera", "Error al iniciar cámara: ${e.message}")
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize().zIndex(0f)
        )

        // Modelo 3D
        AnimatedVisibility(
            visible = mostrarModelo3D && modelNode != null && !isExiting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (modelNode != null) {
                Scene(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .zIndex(1f),
                    engine = engine,
                    modelLoader = modelLoader,
                    cameraNode = cameraNode,
                    childNodes = listOf(modelNode!!),
                    isOpaque = false
                )
            }
        }

        // Loading del modelo
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

        // Error message
        if (errorMessage != null) {
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
                            text = errorMessage ?: "",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoadingModel = false
                                    mostrarModelo3D = false
                                    modelNode = null
                                }
                            ) {
                                Text("Reintentar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    errorMessage = null
                                    isLoadingModel = false
                                    mostrarModelo3D = false
                                    modelNode = null
                                    planetaActual = null
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

        // UI Superior con BackButton
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
                BackButton(
                    onClick = {
                        if (!isExiting) {
                            isExiting = true
                            scope.launch {
                                try {
                                    // Detener animaciones
                                    mostrarModelo3D = false
                                    isLoadingModel = false

                                    delay(100)

                                    // Desenlazar cámara
                                    cameraProvider?.unbindAll()

                                    // Limpiar recursos 3D
                                    modelNode = null
                                    planetaActual = null
                                    errorMessage = null

                                    delay(100)

                                    // Navegar
                                    onBackClick()
                                } catch (e: Exception) {
                                    Log.e("ModelCamera", "Error al salir: ${e.message}", e)
                                    onBackClick()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                )
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

            if (planetaActual == null && !isExiting) {
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
        if (mostrarModelo3D && planetaActual != null && !isLoadingModel && !isExiting) {
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
                        try {
                            mostrarModelo3D = false
                            rotationY = 0f

                            scope.launch {
                                delay(100)
                                modelNode = null
                                planetaActual = null
                                errorMessage = null
                            }
                        } catch (e: Exception) {
                            Log.e("ModelCamera", "Error al ocultar planeta: ${e.message}")
                            modelNode = null
                            planetaActual = null
                            mostrarModelo3D = false
                            errorMessage = null
                        }
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                isExiting = true
                cameraProvider?.unbindAll()
                cameraProvider = null
                modelNode = null
                Log.d("ModelCamera", "Recursos limpiados en DisposableEffect")
            } catch (e: Exception) {
                Log.e("ModelCamera", "Error en DisposableEffect: ${e.message}", e)
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