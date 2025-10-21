package com.utadeo.nebuly.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.utadeo.nebuly.data.models.LearningModule
import com.utadeo.nebuly.data.models.Level
import com.utadeo.nebuly.data.models.PlanetLevel
import com.utadeo.nebuly.data.models.User
import kotlinx.coroutines.tasks.await

class LearningRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val modulesCollection = firestore.collection("learning_modules")
    private val levelsCollection = firestore.collection("levels")
    private val planetLevelsCollection = firestore.collection("planet_levels")
    private val usersCollection = firestore.collection("users")

    /**
     * Obtiene todos los módulos de aprendizaje con su estado de desbloqueo
     */
    suspend fun getModulesForUser(userId: String): Result<List<LearningModule>> {
        return try {
            Log.d("LearningRepository", "Obteniendo módulos para usuario: $userId")

            // Obtener usuario
            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(Exception("Error al procesar usuario"))

            Log.d("LearningRepository", "Módulos desbloqueados: ${user.unlockedModules}")

            // Obtener módulos
            val modulesSnapshot = modulesCollection
                .orderBy("order")
                .get()
                .await()

            val modules = modulesSnapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val description = doc.getString("description") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val order = doc.getLong("order")?.toInt() ?: 0
                    val isUnlocked = user.unlockedModules.contains(id)

                    LearningModule(
                        id = id,
                        name = name,
                        description = description,
                        imageUrl = imageUrl,
                        order = order,
                        isLocked = !isUnlocked
                    )
                } catch (e: Exception) {
                    Log.e("LearningRepository", "Error al procesar módulo ${doc.id}", e)
                    null
                }
            }

            Log.d("LearningRepository", "Módulos cargados: ${modules.size}")
            Result.success(modules)
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error al obtener módulos", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene los niveles de un módulo específico con su estado de desbloqueo
     */
    suspend fun getLevelsForModule(userId: String, moduleId: String): Result<List<Level>> {
        return try {
            Log.d("LearningRepository", "Obteniendo niveles para módulo: $moduleId")

            // Obtener usuario
            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(Exception("Error al procesar usuario"))

            // Obtener niveles del módulo
            val levelsSnapshot = levelsCollection
                .whereEqualTo("moduleId", moduleId)
                .orderBy("levelNumber")
                .get()
                .await()

            val levels = levelsSnapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val levelNumber = doc.getLong("levelNumber")?.toInt() ?: 0
                    val planetName = doc.getString("planetName") ?: ""
                    val planetImageUrl = doc.getString("planetImageUrl") ?: ""
                    val coinsReward = doc.getLong("coinsReward")?.toInt() ?: 0
                    val isUnlocked = user.unlockedLevels.contains(id)

                    Level(
                        id = id,
                        moduleId = moduleId,
                        levelNumber = levelNumber,
                        planetName = planetName,
                        planetImageUrl = planetImageUrl,
                        coinsReward = coinsReward,
                        isLocked = !isUnlocked
                    )
                } catch (e: Exception) {
                    Log.e("LearningRepository", "Error al procesar nivel ${doc.id}", e)
                    null
                }
            }

            Log.d("LearningRepository", "Niveles cargados: ${levels.size}")
            Result.success(levels)
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error al obtener niveles", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene la información detallada de un nivel/planeta
     */
    suspend fun getPlanetLevel(levelId: String): Result<PlanetLevel> {
        return try {
            Log.d("LearningRepository", "Obteniendo información del nivel: $levelId")

            val doc = planetLevelsCollection.document(levelId).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Nivel no encontrado"))
            }

            val moduleId = doc.getString("moduleId") ?: ""
            val levelNumber = doc.getLong("levelNumber")?.toInt() ?: 0
            val planetName = doc.getString("planetName") ?: ""
            val planetImageUrl = doc.getString("planetImageUrl") ?: ""
            val description = doc.getString("description") ?: ""
            val coinsReward = doc.getLong("coinsReward")?.toInt() ?: 0

            val planetLevel = PlanetLevel(
                id = doc.id,
                moduleId = moduleId,
                levelNumber = levelNumber,
                planetName = planetName,
                planetImageUrl = planetImageUrl,
                description = description,
                coinsReward = coinsReward
            )

            Result.success(planetLevel)
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error al obtener nivel", e)
            Result.failure(e)
        }
    }

    /**
     * Desbloquea un módulo para el usuario
     */
    suspend fun unlockModule(userId: String, moduleId: String): Result<Boolean> {
        return try {
            Log.d("LearningRepository", "Desbloqueando módulo: $moduleId para usuario: $userId")

            usersCollection.document(userId)
                .update("unlockedModules", FieldValue.arrayUnion(moduleId))
                .await()

            Log.d("LearningRepository", "Módulo desbloqueado exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error al desbloquear módulo", e)
            Result.failure(e)
        }
    }

    /**
     * Desbloquea un nivel para el usuario
     */
    suspend fun unlockLevel(userId: String, levelId: String): Result<Boolean> {
        return try {
            Log.d("LearningRepository", "Desbloqueando nivel: $levelId para usuario: $userId")

            usersCollection.document(userId)
                .update("unlockedLevels", FieldValue.arrayUnion(levelId))
                .await()

            Log.d("LearningRepository", "Nivel desbloqueado exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error al desbloquear nivel", e)
            Result.failure(e)
        }
    }

    /**
     * Completa un nivel y otorga recompensas
     */
    suspend fun completeLevel(userId: String, levelId: String, coinsReward: Int): Result<Boolean> {
        return try {
            Log.d("LearningRepository", "Completando nivel: $levelId")

            val userRef = usersCollection.document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCoins = snapshot.getLong("coins")?.toInt() ?: 0

                // Agregar monedas
                transaction.update(userRef, "coins", currentCoins + coinsReward)

                // Marcar nivel como completado (agregar a desbloqueados si no está)
                transaction.update(userRef, "unlockedLevels", FieldValue.arrayUnion(levelId))
            }.await()

            Log.d("LearningRepository", "Nivel completado, recompensa otorgada: $coinsReward")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("LearningRepository", "Error al completar nivel", e)
            Result.failure(e)
        }
    }
}