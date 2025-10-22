package com.utadeo.nebuly.data.models

/**
 * Modelo para una pregunta del cuestionario
 */
data class Question(
    val id: String = "",
    val levelId: String = "",           // ej: "level_mercury"
    val questionNumber: Int = 0,        // 1 a 5
    val questionText: String = "",
    val options: List<String> = listOf(), // 4 opciones (a, b, c, d)
    val correctAnswer: Int = 0,         // índice de la respuesta correcta (0-3)
    val reward: Int = 50                // 50 nebulones por pregunta
) {
    // Constructor vacío para Firestore
    constructor() : this("", "", 0, "", emptyList(), 0, 50)
}