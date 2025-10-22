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

    private val TAG = "🔥LEARNING_DEBUG🔥"

    suspend fun getModulesForUser(userId: String): Result<List<LearningModule>> {
        return try {
            Log.e(TAG, "========================================")
            Log.e(TAG, "OBTENIENDO MÓDULOS PARA: $userId")
            Log.e(TAG, "========================================")

            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) {
                Log.e(TAG, "❌ USUARIO NO ENCONTRADO")
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val user = userDoc.toObject(User::class.java)
                ?: return Result.failure(Exception("Error al procesar usuario"))

            Log.e(TAG, "✅ USUARIO ENCONTRADO")
            Log.e(TAG, "Módulos desbloqueados: ${user.unlockedModules}")

            val modulesSnapshot = modulesCollection.orderBy("order").get().await()
            Log.e(TAG, "Documentos en learning_modules: ${modulesSnapshot.documents.size}")

            val modules = modulesSnapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val isUnlocked = user.unlockedModules.contains(id)

                    Log.e(TAG, "Módulo: $id → ${if(isUnlocked) "DESBLOQUEADO" else "BLOQUEADO"}")

                    LearningModule(
                        id = id,
                        name = name,
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        order = doc.getLong("order")?.toInt() ?: 0,
                        isLocked = !isUnlocked
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando módulo ${doc.id}: ${e.message}")
                    null
                }
            }

            Log.e(TAG, "Total módulos cargados: ${modules.size}")
            Result.success(modules)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR GENERAL: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getLevelsForModule(userId: String, moduleId: String): Result<List<Level>> {
        return try {
            Log.e(TAG, "========================================")
            Log.e(TAG, "OBTENIENDO NIVELES")
            Log.e(TAG, "Usuario: $userId")
            Log.e(TAG, "Módulo: $moduleId")
            Log.e(TAG, "========================================")

            // Obtener usuario
            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) {
                Log.e(TAG, "❌ USUARIO NO ENCONTRADO EN FIRESTORE")
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val user = userDoc.toObject(User::class.java)
            if (user == null) {
                Log.e(TAG, "❌ ERROR AL CONVERTIR USUARIO")
                return Result.failure(Exception("Error al procesar usuario"))
            }

            Log.e(TAG, "✅ USUARIO ENCONTRADO")
            Log.e(TAG, "Niveles desbloqueados del usuario:")
            user.unlockedLevels.forEach { levelId ->
                Log.e(TAG, "  - $levelId")
            }

            // Obtener niveles
            val levelsSnapshot = levelsCollection
                .whereEqualTo("moduleId", moduleId)
                .orderBy("levelNumber")
                .get()
                .await()

            Log.e(TAG, "Documentos encontrados en 'levels': ${levelsSnapshot.documents.size}")

            if (levelsSnapshot.documents.isEmpty()) {
                Log.e(TAG, "⚠️ NO SE ENCONTRARON NIVELES PARA ESTE MÓDULO")
                Log.e(TAG, "Verifica que en Firebase 'levels' tengas documentos con moduleId='$moduleId'")
            }

            val levels = levelsSnapshot.documents.mapNotNull { doc ->
                try {
                    val documentId = doc.id
                    val levelNumber = doc.getLong("levelNumber")?.toInt() ?: 0
                    val planetName = doc.getString("planetName") ?: ""
                    val planetImageUrl = doc.getString("planetImageUrl") ?: ""
                    val coinsReward = doc.getLong("coinsReward")?.toInt() ?: 0
                    val moduleIdFromDoc = doc.getString("moduleId") ?: ""

                    Log.e(TAG, "----------------------------------------")
                    Log.e(TAG, "PROCESANDO NIVEL:")
                    Log.e(TAG, "  ID del documento: '$documentId'")
                    Log.e(TAG, "  levelNumber: $levelNumber")
                    Log.e(TAG, "  planetName: '$planetName'")
                    Log.e(TAG, "  moduleId: '$moduleIdFromDoc'")

                    // Verificar si está desbloqueado
                    val isUnlocked = user.unlockedLevels.contains(documentId)

                    Log.e(TAG, "  ¿Está en unlockedLevels? $isUnlocked")
                    Log.e(TAG, "  Estado: ${if(isUnlocked) "🔓 DESBLOQUEADO" else "🔒 BLOQUEADO"}")

                    if (!isUnlocked) {
                        Log.e(TAG, "  ⚠️ Este nivel NO está en la lista de desbloqueados del usuario")
                        Log.e(TAG, "  ⚠️ Buscando '$documentId' en: ${user.unlockedLevels}")
                    }

                    Level(
                        id = documentId,
                        moduleId = moduleIdFromDoc,
                        levelNumber = levelNumber,
                        planetName = planetName,
                        planetImageUrl = planetImageUrl,
                        coinsReward = coinsReward,
                        isLocked = !isUnlocked
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ ERROR procesando nivel ${doc.id}: ${e.message}")
                    null
                }
            }

            Log.e(TAG, "========================================")
            Log.e(TAG, "RESUMEN FINAL:")
            Log.e(TAG, "Total niveles: ${levels.size}")
            levels.forEach { level ->
                Log.e(TAG, "  ${level.planetName}: ${if(level.isLocked) "🔒 BLOQUEADO" else "🔓 DESBLOQUEADO"}")
            }
            Log.e(TAG, "========================================")

            Result.success(levels)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR GENERAL AL OBTENER NIVELES: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getPlanetLevel(levelId: String): Result<PlanetLevel> {
        return try {
            Log.e(TAG, "========================================")
            Log.e(TAG, "OBTENIENDO PLANETA: $levelId")
            Log.e(TAG, "========================================")

            val doc = planetLevelsCollection.document(levelId).get().await()

            if (!doc.exists()) {
                Log.e(TAG, "❌ NO EXISTE DOCUMENTO EN planet_levels CON ID: $levelId")
                return Result.failure(Exception("Nivel no encontrado"))
            }

            Log.e(TAG, "✅ DOCUMENTO ENCONTRADO")

            val planetLevel = PlanetLevel(
                id = doc.id,
                moduleId = doc.getString("moduleId") ?: "",
                levelNumber = doc.getLong("levelNumber")?.toInt() ?: 0,
                planetName = doc.getString("planetName") ?: "",
                planetImageUrl = doc.getString("planetImageUrl") ?: "",
                description = doc.getString("description") ?: "",
                coinsReward = doc.getLong("coinsReward")?.toInt() ?: 0
            )

            Log.e(TAG, "Planeta: ${planetLevel.planetName}")
            Log.e(TAG, "Descripción: ${planetLevel.description.take(50)}...")

            Result.success(planetLevel)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR AL OBTENER PLANETA: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun unlockModule(userId: String, moduleId: String): Result<Boolean> {
        return try {
            usersCollection.document(userId)
                .update("unlockedModules", FieldValue.arrayUnion(moduleId))
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlockLevel(userId: String, levelId: String): Result<Boolean> {
        return try {
            Log.e(TAG, "🔓 Desbloqueando nivel: $levelId")

            usersCollection.document(userId)
                .update("unlockedLevels", FieldValue.arrayUnion(levelId))
                .await()

            Log.e(TAG, "✅ Nivel desbloqueado correctamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al desbloquear: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun completeLevel(userId: String, levelId: String, coinsReward: Int): Result<Boolean> {
        return try {
            Log.e(TAG, "========================================")
            Log.e(TAG, "COMPLETANDO NIVEL: $levelId")
            Log.e(TAG, "Recompensa: $coinsReward monedas")
            Log.e(TAG, "========================================")

            val userRef = usersCollection.document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCoins = snapshot.getLong("coins")?.toInt() ?: 0

                Log.e(TAG, "Monedas actuales: $currentCoins")
                Log.e(TAG, "Nuevas monedas: ${currentCoins + coinsReward}")

                transaction.update(userRef, "coins", currentCoins + coinsReward)
                transaction.update(userRef, "unlockedLevels", FieldValue.arrayUnion(levelId))
            }.await()

            Log.e(TAG, "✅ NIVEL COMPLETADO EXITOSAMENTE")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR AL COMPLETAR NIVEL: ${e.message}", e)
            Result.failure(e)
        }
    }
}