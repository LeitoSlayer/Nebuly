package com.utadeo.nebuly.data.models

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val level: Int = 1,
    val coins: Int = 1000, // 🆕 Nebulones/monedas del usuario
    val currentAvatarId: String = "avatar_default",
    val unlockedAvatars: List<String> = listOf("avatar_default")
) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", "", "", 1, 1000, "avatar_default", listOf("avatar_default"))
}