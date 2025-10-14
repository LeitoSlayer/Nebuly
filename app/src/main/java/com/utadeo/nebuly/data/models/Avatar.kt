package com.utadeo.nebuly.data.models

data class Avatar(
    val id: String = "",
    val imageUrl: String = "",
    val requiredLevel: Int = 1, // Mantener por compatibilidad, pero ya no se usa
    val requiredCoins: Int = 500, // 🆕 Precio en monedas
    val category: String = "default",
    val isLocked: Boolean = true
) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", "", 1, 500, "default", true)
}