package com.utadeo.nebuly.data.models

data class User(
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val level: Int = 1,
    val coins: Int = 1000,
    val currentAvatarId: String = "avatar_default",
    val unlockedAvatars: List<String> = listOf("avatar_default"),
    val unlockedModules: List<String> = listOf("module_solar_system"),
    val unlockedLevels: List<String> = listOf("level_mercury"),
    val unlockedAchievements: List<String> = listOf() // 🆕 Logros desbloqueados
) {
    // Constructor vacío requerido por Firestore
    constructor() : this(
        "", "", "", 1, 1000, "avatar_default",
        listOf("avatar_default"),
        listOf("module_solar_system"),
        listOf("level_mercury"),
        listOf() // 🆕
    )
}