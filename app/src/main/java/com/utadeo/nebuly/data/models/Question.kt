package com.utadeo.nebuly.data.models

data class Question(
    val id: String = "",
    val levelId: String = "",
    val questionNumber: Int = 0,
    val questionText: String = "",
    val options: List<String> = listOf(),
    val correctAnswer: Int = 0,
    val reward: Int = 50
) {
    constructor() : this("", "", 0, "", emptyList(), 0, 50)
}