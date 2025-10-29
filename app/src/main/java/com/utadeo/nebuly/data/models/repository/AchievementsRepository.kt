package com.utadeo.nebuly.data.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.utadeo.nebuly.data.models.Achievement
import kotlinx.coroutines.tasks.await

class AchievementsRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val achievementsCollection = firestore.collection("achievements")
    private val usersCollection = firestore.collection("users")

    private val TAG = "🔥ACHIEVEMENTS_DEBUG🔥"

    suspend fun getAllAchievements(): Result<List<Achievement>> {
        return try {
            Log.e(TAG, "========================================")
            Log.e(TAG, "OBTENIENDO TODOS LOS LOGROS")
            Log.e(TAG, "========================================")

            val snapshot = achievementsCollection
                .orderBy("order")
                .get()
                .await()

            Log.e(TAG, "Documentos encontrados: ${snapshot.documents.size}")

            val achievements = snapshot.documents.mapNotNull { doc ->
                try {
                    Log.e(TAG, "Procesando documento: ${doc.id}")

                    val achievement = Achievement(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        requiredLevelId = doc.getString("requiredLevelId") ?: "",
                        planetName = doc.getString("planetName") ?: "",
                        order = doc.getLong("order")?.toInt() ?: 0
                    )

                    Log.e(TAG, "  ✅ ${achievement.planetName} (order: ${achievement.order})")
                    achievement
                } catch (e: Exception) {
                    Log.e(TAG, "  ❌ Error procesando logro ${doc.id}", e)
                    null
                }
            }

            if (achievements.isEmpty()) {
                Log.e(TAG, "⚠️ NO SE ENCONTRARON LOGROS EN FIREBASE")
                Log.e(TAG, "Verifica que la colección 'achievements' existe")
            } else {
                Log.e(TAG, "✅ Total logros cargados: ${achievements.size}")
            }

            Result.success(achievements)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR AL OBTENER LOGROS: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUnlockedAchievements(userId: String): Result<List<String>> {
        return try {
            Log.e(TAG, "Obteniendo logros desbloqueados del usuario: $userId")

            val doc = usersCollection.document(userId).get().await()

            if (!doc.exists()) {
                Log.e(TAG, "❌ Usuario no encontrado: $userId")
                return Result.failure(Exception("Usuario no encontrado"))
            }

            @Suppress("UNCHECKED_CAST")
            val unlockedAchievements = doc.get("unlockedAchievements") as? List<String> ?: emptyList()

            Log.e(TAG, "✅ Logros desbloqueados: ${unlockedAchievements.size}")
            unlockedAchievements.forEach { achievementId ->
                Log.e(TAG, "  - $achievementId")
            }

            Result.success(unlockedAchievements)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener logros desbloqueados", e)
            Result.failure(e)
        }
    }

    suspend fun unlockAchievement(userId: String, achievementId: String): Result<Boolean> {
        return try {
            Log.e(TAG, "Desbloqueando logro: $achievementId para usuario: $userId")

            // Verificar si ya está desbloqueado
            val unlockedResult = getUnlockedAchievements(userId)
            if (unlockedResult.isSuccess) {
                val unlocked = unlockedResult.getOrNull() ?: emptyList()
                if (unlocked.contains(achievementId)) {
                    Log.w(TAG, "⚠️ Logro ya desbloqueado: $achievementId")
                    return Result.success(false)
                }
            }

            usersCollection.document(userId)
                .update("unlockedAchievements", FieldValue.arrayUnion(achievementId))
                .await()

            Log.e(TAG, "✅ Logro desbloqueado exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al desbloquear logro", e)
            Result.failure(e)
        }
    }

    suspend fun getAchievementByLevel(levelId: String): Result<Achievement?> {
        return try {
            Log.e(TAG, "Buscando logro para nivel: $levelId")

            val snapshot = achievementsCollection
                .whereEqualTo("requiredLevelId", levelId)
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                Log.w(TAG, "⚠️ No se encontró logro para el nivel: $levelId")
                return Result.success(null)
            }

            val doc = snapshot.documents.first()
            val achievement = Achievement(
                id = doc.id,
                name = doc.getString("name") ?: "",
                description = doc.getString("description") ?: "",
                imageUrl = doc.getString("imageUrl") ?: "",
                requiredLevelId = doc.getString("requiredLevelId") ?: "",
                planetName = doc.getString("planetName") ?: "",
                order = doc.getLong("order")?.toInt() ?: 0
            )

            Log.e(TAG, "✅ Logro encontrado: ${achievement.name}")
            Result.success(achievement)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al buscar logro por nivel", e)
            Result.failure(e)
        }
    }

    suspend fun checkAndUnlockSolarSystemAchievement(userId: String): Result<Boolean> {
        return try {
            Log.e(TAG, "🌌 Verificando logro Sistema Solar para usuario: $userId")

            val allRequiredLevels = listOf(
                "level_mercury",
                "level_venus",
                "level_earth",
                "level_mars",
                "level_jupiter",
                "level_saturn",
                "level_uranus",
                "level_neptune"
            )

            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            @Suppress("UNCHECKED_CAST")
            val completedLevels = userDoc.get("completedLevels") as? List<String> ?: emptyList()

            Log.e(TAG, "Niveles completados: ${completedLevels.size}/${allRequiredLevels.size}")

            val completedAllLevels = allRequiredLevels.all { level ->
                completedLevels.contains(level)
            }

            if (completedAllLevels) {
                Log.e(TAG, "✅ ¡Todos los niveles completados! Desbloqueando logro Sistema Solar")

                // Buscar el logro del sistema solar
                val achievementSnapshot = achievementsCollection
                    .whereEqualTo("requiredLevelId", "all_levels")
                    .limit(1)
                    .get()
                    .await()

                if (!achievementSnapshot.documents.isEmpty()) {
                    val achievementId = achievementSnapshot.documents.first().id

                    // Verificar si ya está desbloqueado
                    val unlockedResult = getUnlockedAchievements(userId)
                    if (unlockedResult.isSuccess) {
                        val unlocked = unlockedResult.getOrNull() ?: emptyList()
                        if (unlocked.contains(achievementId)) {
                            Log.w(TAG, "⚠️ Logro Sistema Solar ya estaba desbloqueado")
                            return Result.success(false)
                        }
                    }

                    // Desbloquear el logro
                    usersCollection.document(userId)
                        .update("unlockedAchievements", FieldValue.arrayUnion(achievementId))
                        .await()

                    Log.e(TAG, "🎉 ¡Logro Sistema Solar desbloqueado!")
                    return Result.success(true)
                } else {
                    Log.w(TAG, "⚠️ No se encontró el logro Sistema Solar en la base de datos")
                    return Result.success(false)
                }
            } else {
                val remaining = allRequiredLevels.filter { !completedLevels.contains(it) }
                Log.e(TAG, "❌ Aún faltan ${remaining.size} niveles: $remaining")
                return Result.success(false)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al verificar logro Sistema Solar", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el logro del Sistema Solar
     */
    suspend fun getSolarSystemAchievement(): Result<Achievement?> {
        return try {
            Log.e(TAG, "Obteniendo logro Sistema Solar")

            val snapshot = achievementsCollection
                .whereEqualTo("requiredLevelId", "all_levels")
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isEmpty()) {
                Log.w(TAG, "⚠️ No se encontró el logro Sistema Solar")
                return Result.success(null)
            }

            val doc = snapshot.documents.first()
            val achievement = Achievement(
                id = doc.id,
                name = doc.getString("name") ?: "",
                description = doc.getString("description") ?: "",
                imageUrl = doc.getString("imageUrl") ?: "",
                requiredLevelId = doc.getString("requiredLevelId") ?: "",
                planetName = doc.getString("planetName") ?: "",
                order = doc.getLong("order")?.toInt() ?: 0
            )

            Log.e(TAG, "✅ Logro Sistema Solar encontrado: ${achievement.name}")
            Result.success(achievement)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al obtener logro Sistema Solar", e)
            Result.failure(e)
        }
    }
}