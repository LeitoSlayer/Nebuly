package com.utadeo.nebuly.data.models

data class Avatar(
    val id: String = "",
    val imageUrl: String = "",
    val requiredLevel: Int = 1,
    val requiredCoins: Int = 500,
    val category: String = "default",
    val isLocked: Boolean = true
) {
    constructor() : this("", "", 1, 500, "default", true)
}