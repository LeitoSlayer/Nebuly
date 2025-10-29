package com.utadeo.nebuly.data.models

data class Achievement(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val requiredLevelId: String = "", // Nivel que debe completar para desbloquear
    val planetName: String = "",      // Nombre del planeta (ej: "Mercurio")
    val order: Int = 0                // Orden de visualización
) {
    // Constructor vacío para Firestore
    constructor() : this("", "", "", "", "", "", 0)
}