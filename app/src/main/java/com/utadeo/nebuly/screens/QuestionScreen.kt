package com.utadeo.nebuly.screens.learning

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.utadeo.nebuly.R
import com.utadeo.nebuly.components.BackButton
import com.utadeo.nebuly.components.AchievementUnlockedNotification
import com.utadeo.nebuly.data.models.Question
import com.utadeo.nebuly.data.models.Achievement
import com.utadeo.nebuly.data.repository.LearningRepository
import com.utadeo.nebuly.data.repository.QuestionRepository
import com.utadeo.nebuly.data.repository.AchievementsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log

// IDs de recursos de planetas
private val PLANET_IMAGES = listOf(
    R.drawable.planeta_mini1,
    R.drawable.planeta_mini2,
    R.drawable.planeta_mini3,
    R.drawable.planeta_mini4,
    R.drawable.planeta_mini5
)

private val PLANET_NAMES = listOf("Mercurio", "Venus", "Tierra", "Marte", "Júpiter")

@Composable
fun QuestionScreen(
    auth: FirebaseAuth,
    levelId: String,
    moduleId: String,
    moduleName: String,
    onBackClick: () -> Unit,
    onQuizComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val questionRepository = remember { QuestionRepository() }
    val learningRepository = remember { LearningRepository() }
    val achievementsRepository = remember { AchievementsRepository() }
    val scope = rememberCoroutineScope()

    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var totalCoinsEarned by remember { mutableStateOf(0) }
    var correctAnswers by remember { mutableStateOf(0) }
    var showFeedback by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // 🆕 Lista para trackear el estado de cada pregunta
    var questionStates by remember { mutableStateOf<List<QuestionState>>(emptyList()) }

    var isFirstTime by remember { mutableStateOf(true) }
    var quizCompleted by remember { mutableStateOf(false) }

    var showAchievementNotification by remember { mutableStateOf(false) }
    var unlockedAchievement by remember { mutableStateOf<Achievement?>(null) }

    // Verificar si el siguiente nivel ya está desbloqueado
    LaunchedEffect(levelId) {
        scope.launch {
            auth.currentUser?.uid?.let { userId ->
                learningRepository.isNextLevelUnlocked(userId, levelId).fold(
                    onSuccess = { isUnlocked ->
                        isFirstTime = !isUnlocked
                    },
                    onFailure = {
                        isFirstTime = true
                    }
                )
            }
        }
    }

    // Cargar preguntas
    LaunchedEffect(levelId) {
        scope.launch {
            questionRepository.getQuestionsForLevel(levelId).fold(
                onSuccess = {
                    questions = it
                    // Inicializar estados de preguntas
                    questionStates = List(it.size) { QuestionState.PENDING }
                    isLoading = false
                },
                onFailure = {
                    errorMessage = it.message
                    isLoading = false
                }
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo espacial
        Image(
            painter = painterResource(id = R.drawable.fondo_preguntas),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Error",
                            color = Color(0xFFFF6B6B),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            quizCompleted -> {
                QuizCompletionScreen(
                    correctAnswers = correctAnswers,
                    totalQuestions = questions.size,
                    coinsEarned = if (isFirstTime) totalCoinsEarned else 0,
                    isFirstTime = isFirstTime,
                    passed = correctAnswers == questions.size,
                    onContinue = onQuizComplete
                )
            }
            questions.isNotEmpty() -> {
                val currentQuestion = questions[currentQuestionIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    // 🆕 Nuevo indicador de progreso con planetas
                    PlanetProgressIndicator(
                        totalQuestions = questions.size,
                        currentQuestionIndex = currentQuestionIndex,
                        questionStates = questionStates,
                        correctAnswers = correctAnswers
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de progreso
                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Pregunta
                    QuestionCard(question = currentQuestion)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Opciones
                    currentQuestion.options.forEachIndexed { index, option ->
                        AnswerOption(
                            text = option,
                            index = index,
                            isSelected = selectedAnswer == index,
                            isCorrect = if (showFeedback) index == currentQuestion.correctAnswer else null,
                            isWrong = if (showFeedback && selectedAnswer == index)
                                index != currentQuestion.correctAnswer else false,
                            enabled = !showFeedback && !isProcessing,
                            onClick = {
                                if (!showFeedback && !isProcessing) {
                                    selectedAnswer = index
                                    isAnswerCorrect = index == currentQuestion.correctAnswer
                                    showFeedback = true

                                    // 🆕 Actualizar estado de la pregunta
                                    val newStates = questionStates.toMutableList()
                                    newStates[currentQuestionIndex] = if (isAnswerCorrect == true) {
                                        QuestionState.CORRECT
                                    } else {
                                        QuestionState.INCORRECT
                                    }
                                    questionStates = newStates

                                    if (isAnswerCorrect == true) {
                                        correctAnswers++
                                        if (isFirstTime) {
                                            totalCoinsEarned += currentQuestion.reward
                                        }
                                    }

                                    // Avanzar automáticamente después de 2 segundos
                                    scope.launch {
                                        delay(2000)

                                        if (currentQuestionIndex < questions.size - 1) {
                                            currentQuestionIndex++
                                            selectedAnswer = null
                                            isAnswerCorrect = null
                                            showFeedback = false
                                        } else {
                                            quizCompleted = true

                                            if (isFirstTime && correctAnswers == questions.size) {
                                                isProcessing = true
                                                auth.currentUser?.uid?.let { userId ->
                                                    learningRepository.completeLevel(
                                                        userId = userId,
                                                        levelId = levelId,
                                                        coinsReward = totalCoinsEarned
                                                    ).fold(
                                                        onSuccess = {
                                                            scope.launch {
                                                                unlockNextLevel(
                                                                    userId = userId,
                                                                    currentLevelId = levelId,
                                                                    learningRepository = learningRepository
                                                                )

                                                                unlockAchievementForLevel(
                                                                    userId = userId,
                                                                    levelId = levelId,
                                                                    achievementsRepository = achievementsRepository,
                                                                    onAchievementUnlocked = { achievement ->
                                                                        unlockedAchievement = achievement
                                                                        showAchievementNotification = true
                                                                    }
                                                                )
                                                            }
                                                        },
                                                        onFailure = {
                                                            errorMessage = it.message
                                                            isProcessing = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Feedback
                    AnimatedVisibility(
                        visible = showFeedback,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        FeedbackCard(
                            isCorrect = isAnswerCorrect ?: false,
                            coinsEarned = if (isFirstTime) currentQuestion.reward else 0,
                            isFirstTime = isFirstTime
                        )
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

        // Notificación de logro desbloqueado
        if (showAchievementNotification && unlockedAchievement != null) {
            AchievementUnlockedNotification(
                achievement = unlockedAchievement!!,
                onDismiss = {
                    showAchievementNotification = false
                    unlockedAchievement = null
                }
            )
        }
    }
}

// 🆕 Enum para estados de preguntas
enum class QuestionState {
    PENDING,
    CORRECT,
    INCORRECT
}

// 🆕 Nuevo componente de indicador de planetas
@Composable
private fun PlanetProgressIndicator(
    totalQuestions: Int,
    currentQuestionIndex: Int,
    questionStates: List<QuestionState>,
    correctAnswers: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fila de planetas
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            repeat(totalQuestions.coerceAtMost(5)) { index ->
                PlanetIndicator(
                    planetImageUrl = PLANET_IMAGES.getOrNull(index) ?: PLANET_IMAGES[0],
                    planetName = PLANET_NAMES.getOrNull(index) ?: "Planeta ${index + 1}",
                    state = questionStates.getOrNull(index) ?: QuestionState.PENDING,
                    isCurrent = index == currentQuestionIndex,
                    questionNumber = index + 1
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))


    }
}

@Composable
private fun PlanetIndicator(
    planetImageUrl: Int,
    planetName: String,
    state: QuestionState,
    isCurrent: Boolean,
    questionNumber: Int
) {
    // Animación para el planeta actual
    val infiniteTransition = rememberInfiniteTransition(label = "planet_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCurrent) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Color del borde según el estado
    val borderColor = when (state) {
        QuestionState.CORRECT -> Color(0xFF4CAF50)
        QuestionState.INCORRECT -> Color(0xFFFF6B6B)
        QuestionState.PENDING -> if (isCurrent) Color.White else Color.White.copy(alpha = 0.3f)
    }

    val borderWidth = when {
        isCurrent -> 3.dp
        state != QuestionState.PENDING -> 2.dp
        else -> 1.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp)  // Cambia a width
    ) {
        // Box externo para el scale
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(if (isCurrent) scale else 1f),  // Scale solo aquí
            contentAlignment = Alignment.Center
        ) {
            // Box interno con el contenido
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .background(
                        when (state) {
                            QuestionState.PENDING -> Color.Black.copy(alpha = 0.5f)
                            else -> Color.Black.copy(alpha = 0.3f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Imagen del planeta
                Image(
                    painter = painterResource(id = planetImageUrl),
                    contentDescription = planetName,
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer {
                            alpha = when (state) {
                                QuestionState.PENDING -> if (isCurrent) 1f else 0.4f
                                else -> 1f
                            }
                        },
                    contentScale = ContentScale.Fit
                )

                // Overlay para estados
                when (state) {
                    QuestionState.CORRECT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF4CAF50).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    QuestionState.INCORRECT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFF6B6B).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✗",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    QuestionState.PENDING -> {
                        if (!isCurrent) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.6f))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Número de pregunta
        Text(
            text = "$questionNumber",
            fontSize = 12.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun QuizCompletionScreen(
    correctAnswers: Int,
    totalQuestions: Int,
    coinsEarned: Int,
    isFirstTime: Boolean,
    passed: Boolean,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (passed) {
                            listOf(Color(0xFF2E7D32), Color(0xFF4CAF50))
                        } else {
                            listOf(Color(0xFFB71C1C), Color(0xFFE53935))
                        }
                    )
                )
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (passed) "¡Felicidades!" else "Intenta de nuevo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Respuestas correctas:",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Text(
                text = "$correctAnswers/$totalQuestions",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (passed) {
                if (isFirstTime) {
                    Text(
                        text = "Nebulones ganados:",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "+$coinsEarned",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "¡Siguiente nivel desbloqueado!",
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Ya completaste este nivel",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "(No se otorgan más nebulones)",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Necesitas responder todas\nlas preguntas correctamente",
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón continuar
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onContinue() }
                    .padding(horizontal = 48.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Continuar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun QuestionCard(question: Question) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A237E).copy(alpha = 0.95f),
                        Color(0xFF0D47A1).copy(alpha = 0.90f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4A90E2),
                        Color(0xFF7B68EE)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        Text(
            text = question.questionText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
    }
}

@Composable
private fun AnswerOption(
    text: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect == true -> Color(0xFF4CAF50).copy(alpha = 0.9f)
        isWrong -> Color(0xFFFF6B6B).copy(alpha = 0.9f)
        isSelected -> Color(0xFF7B68EE).copy(alpha = 0.8f)
        else -> Color(0xFF2C2C2C).copy(alpha = 0.8f)
    }

    val borderColor = when {
        isCorrect == true -> Color(0xFF4CAF50)
        isWrong -> Color(0xFFFF6B6B)
        isSelected -> Color(0xFF7B68EE)
        else -> Color.White.copy(alpha = 0.3f)
    }

    val icon = when {
        isCorrect == true -> "✓"
        isWrong -> "✗"
        else -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${'A' + index})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(end = 12.dp)
            )

            Text(
                text = text,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            if (icon.isNotEmpty()) {
                Text(
                    text = icon,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FeedbackCard(
    isCorrect: Boolean,
    coinsEarned: Int,
    isFirstTime: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isCorrect) Color(0xFF4CAF50).copy(alpha = 0.9f)
                else Color(0xFFFF6B6B).copy(alpha = 0.9f)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isCorrect) "¡Correcto!" else "Incorrecto",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (isCorrect && isFirstTime && coinsEarned > 0) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "+$coinsEarned",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}

private suspend fun unlockNextLevel(
    userId: String,
    currentLevelId: String,
    learningRepository: LearningRepository
) {
    val planetOrder = listOf(
        "level_mercury",
        "level_venus",
        "level_earth",
        "level_mars",
        "level_jupiter",
        "level_saturn",
        "level_uranus",
        "level_neptune"
    )

    val currentIndex = planetOrder.indexOf(currentLevelId)

    if (currentIndex != -1 && currentIndex < planetOrder.size - 1) {
        val nextLevelId = planetOrder[currentIndex + 1]
        learningRepository.unlockLevel(userId, nextLevelId)
    }
}

private suspend fun unlockAchievementForLevel(
    userId: String,
    levelId: String,
    achievementsRepository: AchievementsRepository,
    onAchievementUnlocked: (Achievement) -> Unit
) {
    achievementsRepository.getAchievementByLevel(levelId).fold(
        onSuccess = { achievement ->
            if (achievement != null) {
                achievementsRepository.unlockAchievement(userId, achievement.id).fold(
                    onSuccess = { wasUnlocked ->
                        if (wasUnlocked) {
                            Log.d("QuestionScreen", "Logro desbloqueado: ${achievement.name}")
                            onAchievementUnlocked(achievement)
                        }
                    },
                    onFailure = { error ->
                        Log.e("QuestionScreen", "Error al desbloquear logro", error)
                    }
                )
            }
        },
        onFailure = { error ->
            Log.e("QuestionScreen", "Error al buscar logro", error)
        }
    )

    delay(500)

    achievementsRepository.checkAndUnlockSolarSystemAchievement(userId).fold(
        onSuccess = { wasUnlocked ->
            if (wasUnlocked) {
                Log.d("QuestionScreen", "¡Logro Sistema Solar desbloqueado!")

                // Obtener el logro y mostrar notificación
                delay(3500) // Esperar a que termine la primera notificación

                achievementsRepository.getSolarSystemAchievement().fold(
                    onSuccess = { solarSystemAchievement ->
                        if (solarSystemAchievement != null) {
                            onAchievementUnlocked(solarSystemAchievement)
                        }
                    },
                    onFailure = { error ->
                        Log.e("QuestionScreen", "Error al obtener logro Sistema Solar", error)
                    }
                )
            }
        },
        onFailure = { error ->
            Log.e("QuestionScreen", "Error al verificar logro Sistema Solar", error)
        }
    )
}