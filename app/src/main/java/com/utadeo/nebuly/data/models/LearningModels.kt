package com.utadeo.nebuly.data.models

/**
 * Representa un módulo de aprendizaje (ej: Sistema Solar, Galaxia, Universo)
 */
data class LearningModule(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val order: Int = 0,
    val isLocked: Boolean = true
) {
    constructor() : this("", "", "", "", 0, true)
}

/**
 * Representa un nivel dentro de un módulo (ej: Mercurio, Venus, Tierra)
 */
data class Level(
    val id: String = "",
    val moduleId: String = "",
    val levelNumber: Int = 0,
    val planetName: String = "",
    val planetImageUrl: String = "",
    val coinsReward: Int = 0,
    val isLocked: Boolean = true
) {
    constructor() : this("", "", 0, "", "", 0, true)
}

/**
 * Representa la información detallada de un planeta/nivel
 */
data class PlanetLevel(
    val id: String = "",
    val moduleId: String = "",
    val levelNumber: Int = 0,
    val planetName: String = "",
    val planetImageUrl: String = "",
    val description: String = "",
    val coinsReward: Int = 0
) {
    constructor() : this("", "", 0, "", "", "", 0)
}