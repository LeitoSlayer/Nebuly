package com.utadeo.nebuly.data.models.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.utadeo.nebuly.data.models.PlanetData
import com.utadeo.nebuly.data.models.PlanetPreview
import com.utadeo.nebuly.data.models.PointOfInterest
import kotlinx.coroutines.tasks.await
import java.io.File

class PlanetRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val poisCollection = firestore.collection("planet_pois")
    private val levelsCollection = firestore.collection("levels")

    suspend fun getPlanetPreviews(): Result<List<PlanetPreview>> {
        return try {
            Log.d("PlanetRepository", "Cargando lista de planetas...")

            val snapshot = levelsCollection
                .whereEqualTo("moduleId", "module_solar_system")
                .get()
                .await()

            val planets = snapshot.documents.mapNotNull { doc ->
                try {
                    val planetName = doc.getString("planetName") ?: return@mapNotNull null
                    val imageUrl = doc.getString("planetImageUrl") ?: return@mapNotNull null

                    val planetId = doc.id.removePrefix("level_")

                    PlanetPreview(
                        planetId = planetId,
                        planetName = planetName,
                        imageUrl = imageUrl,
                        isUnlocked = true // Por ahora todos desbloqueados
                    )
                } catch (e: Exception) {
                    Log.e("PlanetRepository", "Error procesando planeta ${doc.id}", e)
                    null
                }
            }

            Log.d("PlanetRepository", "Planetas cargados: ${planets.size}")
            Result.success(planets)
        } catch (e: Exception) {
            Log.e("PlanetRepository", "Error cargando planetas", e)
            Result.failure(e)
        }
    }

    suspend fun getPlanetData(planetId: String): Result<PlanetData> {
        return try {
            Log.d("PlanetRepository", "Cargando datos del planeta: $planetId")

            val doc = poisCollection.document(planetId).get().await()

            if (!doc.exists()) {
                Log.e("PlanetRepository", "Planeta no encontrado: $planetId")
                return Result.failure(Exception("Planeta no encontrado"))
            }

            val planetName = doc.getString("planetName") ?: ""
            val modelUrl = doc.getString("modelUrl") ?: ""

            val poisList = doc.get("pois") as? List<HashMap<String, Any>> ?: emptyList()
            val pois = poisList.mapNotNull { poiMap ->
                try {
                    PointOfInterest(
                        id = poiMap["id"] as? String ?: "",
                        titulo = poiMap["titulo"] as? String ?: "",
                        latitud = (poiMap["latitud"] as? Number)?.toFloat() ?: 0f,
                        longitud = (poiMap["longitud"] as? Number)?.toFloat() ?: 0f,
                        descripcion = poiMap["descripcion"] as? String ?: "",
                        emoji = poiMap["emoji"] as? String ?: "📍"
                    )
                } catch (e: Exception) {
                    Log.e("PlanetRepository", "Error parseando POI", e)
                    null
                }
            }

            val planetData = PlanetData(
                planetId = planetId,
                planetName = planetName,
                modelUrl = modelUrl,
                pois = pois
            )

            Log.d("PlanetRepository", "Planeta cargado: $planetName, POIs: ${pois.size}")
            Result.success(planetData)
        } catch (e: Exception) {
            Log.e("PlanetRepository", "Error cargando datos del planeta", e)
            Result.failure(e)
        }
    }

    suspend fun downloadPlanetModel(modelUrl: String, cacheDir: File): Result<String> {
        return try {
            Log.d("PlanetRepository", "Descargando modelo: $modelUrl")

            val fileName = modelUrl.substringAfterLast("/")
            val localFile = File(cacheDir, fileName)

            if (localFile.exists()) {
                Log.d("PlanetRepository", "Modelo ya existe en cache: ${localFile.absolutePath}")
                return Result.success(localFile.absolutePath)
            }

            val storageRef = storage.reference.child(modelUrl)
            storageRef.getFile(localFile).await()

            Log.d("PlanetRepository", "Modelo descargado: ${localFile.absolutePath}")
            Result.success(localFile.absolutePath)
        } catch (e: Exception) {
            Log.e("PlanetRepository", "Error descargando modelo", e)
            Result.failure(e)
        }
    }
}

