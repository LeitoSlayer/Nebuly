package com.utadeo.nebuly.data.models

data class PlanetData(
    val planetId: String = "",
    val planetName: String = "",
    val modelUrl: String = "", // Ruta en Firebase Storage: "planets/earth.glb"
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

// Modelo para la lista de planetas en la pantalla de selección
data class PlanetPreview(
    val planetId: String,
    val planetName: String,
    val imageUrl: String, // De la colección "levels"
    val isUnlocked: Boolean = true
)

