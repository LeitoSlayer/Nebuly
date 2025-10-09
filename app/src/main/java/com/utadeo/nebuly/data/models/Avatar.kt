package com.utadeo.nebuly.data.models

data class Avatar(
    val id: String = "",
    val imageUrl: String = "",
    val requiredLevel: Int = 1,
    val category: String = "default",
    val isLocked: Boolean = true
) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", "", 1, "default", true)
}