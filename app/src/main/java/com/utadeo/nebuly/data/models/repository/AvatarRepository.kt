package com.utadeo.nebuly.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.utadeo.nebuly.data.models.Avatar
import com.utadeo.nebuly.data.models.User
import kotlinx.coroutines.tasks.await
import android.util.Log

class AvatarRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val avatarsCollection = firestore.collection("avatars")
    private val usersCollection = firestore.collection("users")

    suspend fun getAllAvatars(): Result<List<Avatar>> {
        return try {
            Log.d("AvatarRepository", "Iniciando carga de avatares...")

            val snapshot = avatarsCollection.get().await()
            Log.d("AvatarRepository", "Documentos encontrados: ${snapshot.documents.size}")

            val avatars = snapshot.documents.mapNotNull { doc ->
                try {
                    // Obtenemos los datos manualmente para mayor control
                    val category = doc.getString("category") ?: "default"
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val requiredLevel = doc.getLong("requiredLevel")?.toInt() ?: 1

                    Log.d("AvatarRepository", "Avatar cargado: ${doc.id}, imageUrl: $imageUrl, level: $requiredLevel")

                    Avatar(
                        id = doc.id,
                        imageUrl = imageUrl,
                        requiredLevel = requiredLevel,
                        category = category,
                        isLocked = true // Por defecto bloqueado
                    )
                } catch (e: Exception) {
                    Log.e("AvatarRepository", "Error al procesar documento ${doc.id}", e)
                    null
                }
            }

            Log.d("AvatarRepository", "Total avatares cargados: ${avatars.size}")

            if (avatars.isEmpty()) {
                Log.w("AvatarRepository", "No se cargaron avatares. Verifica la estructura en Firebase.")
            }

            Result.success(avatars)
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error al cargar avatares", e)
            Result.failure(e)
        }
    }

    suspend fun getAvatarsForUser(userId: String): Result<List<Avatar>> {
        return try {
            Log.d("AvatarRepository", "Cargando avatares para usuario: $userId")

            // Obtenemos el usuario
            val userDoc = usersCollection.document(userId).get().await()

            if (!userDoc.exists()) {
                Log.e("AvatarRepository", "Documento de usuario no existe: $userId")
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val user = userDoc.toObject(User::class.java)

            if (user == null) {
                Log.e("AvatarRepository", "No se pudo convertir usuario: $userId")
                return Result.failure(Exception("Usuario no encontrado"))
            }

            Log.d("AvatarRepository", "Usuario encontrado - Level: ${user.level}, Avatares desbloqueados: ${user.unlockedAvatars}")

            // Obtenemos todos los avatares
            val avatarsSnapshot = avatarsCollection.get().await()
            Log.d("AvatarRepository", "Documentos de avatares: ${avatarsSnapshot.documents.size}")

            val avatars = avatarsSnapshot.documents.mapNotNull { doc ->
                try {
                    val category = doc.getString("category") ?: "default"
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val requiredLevel = doc.getLong("requiredLevel")?.toInt() ?: 1

                    // Verificar si está desbloqueado
                    val isUnlocked = user.unlockedAvatars.contains(doc.id) ||
                            user.level >= requiredLevel

                    Log.d("AvatarRepository", "Avatar: ${doc.id}, Level req: $requiredLevel, User level: ${user.level}, Desbloqueado: $isUnlocked")

                    Avatar(
                        id = doc.id,
                        imageUrl = imageUrl,
                        requiredLevel = requiredLevel,
                        category = category,
                        isLocked = !isUnlocked
                    )
                } catch (e: Exception) {
                    Log.e("AvatarRepository", "Error al procesar avatar ${doc.id}", e)
                    null
                }
            }

            Log.d("AvatarRepository", "Total avatares procesados: ${avatars.size}")

            if (avatars.isEmpty()) {
                Log.w("AvatarRepository", "No se procesaron avatares. Verifica los datos en Firebase.")
            }

            Result.success(avatars)
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error al cargar avatares para usuario", e)
            Result.failure(e)
        }
    }

    suspend fun setCurrentAvatar(userId: String, avatarId: String): Result<Boolean> {
        return try {
            Log.d("AvatarRepository", "Actualizando avatar para usuario: $userId -> $avatarId")

            usersCollection.document(userId)
                .update("currentAvatarId", avatarId)
                .await()

            Log.d("AvatarRepository", "Avatar actualizado exitosamente")
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error al actualizar avatar", e)
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            Log.d("AvatarRepository", "Obteniendo usuario: $userId")

            val doc = usersCollection.document(userId).get().await()

            if (!doc.exists()) {
                Log.e("AvatarRepository", "Documento de usuario no existe")
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val user = doc.toObject(User::class.java)

            if (user != null) {
                Log.d("AvatarRepository", "Usuario obtenido - Level: ${user.level}, CurrentAvatar: ${user.currentAvatarId}")
                Result.success(user)
            } else {
                Log.e("AvatarRepository", "No se pudo convertir el documento a User")
                Result.failure(Exception("Error al procesar datos del usuario"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Error al obtener usuario", e)
            Result.failure(e)
        }
    }
}