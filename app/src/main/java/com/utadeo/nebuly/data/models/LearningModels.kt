package com.utadeo.nebuly.data.models

data class LearningModule(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val order: Int = 0,
    val isLocked: Boolean = true
)

data class Level(
    val id: String = "",
    val moduleId: String = "",
    val levelNumber: Int = 0,
    val planetName: String = "",
    val planetImageUrl: String = "",
    val coinsReward: Int = 0,
    val isLocked: Boolean = true
)

data class PlanetLevel(
    val id: String = "",
    val moduleId: String = "",
    val levelNumber: Int = 0,
    val planetName: String = "",
    val planetImageUrl: String = "",
    val description: String = "",
    val coinsReward: Int = 0
)