package com.utadeo.nebuly.data.models

data class PlanetData(
    val planetId: String = "",
    val planetName: String = "",
    val modelUrl: String = "",
    val pois: List<PointOfInterest> = emptyList()
)

data class PointOfInterest(
    val id: String = "",
    val titulo: String = "",
    val latitud: Float = 0f,
    val longitud: Float = 0f,
    val descripcion: String = "",
    val emoji: String = "📍"
)

data class PlanetPreview(
    val planetId: String,
    val planetName: String,
    val imageUrl: String,
    val isUnlocked: Boolean = true
)

