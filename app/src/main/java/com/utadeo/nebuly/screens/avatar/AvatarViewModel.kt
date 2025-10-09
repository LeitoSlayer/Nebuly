package com.utadeo.nebuly.screens.avatar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utadeo.nebuly.data.models.Avatar
import com.utadeo.nebuly.data.models.User
import com.utadeo.nebuly.data.repository.AvatarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AvatarUiState(
    val avatars: List<Avatar> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class AvatarViewModel(
    private val repository: AvatarRepository = AvatarRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvatarUiState())
    val uiState: StateFlow<AvatarUiState> = _uiState.asStateFlow()

    fun loadAvatars(userId: String) {
        viewModelScope.launch {
            Log.d("AvatarViewModel", "=== INICIANDO CARGA DE AVATARES ===")
            Log.d("AvatarViewModel", "UserId recibido: '$userId'")

            if (userId.isBlank()) {
                Log.e("AvatarViewModel", "UserId está vacío!")
                _uiState.value = AvatarUiState(
                    isLoading = false,
                    error = "Error: ID de usuario no válido"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Cargar usuario
            Log.d("AvatarViewModel", "Cargando datos del usuario...")
            val userResult = repository.getUser(userId)

            if (userResult.isFailure) {
                Log.e("AvatarViewModel", "Error al cargar usuario: ${userResult.exceptionOrNull()?.message}")
                _uiState.value = AvatarUiState(
                    isLoading = false,
                    error = "Error al cargar usuario: ${userResult.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val user = userResult.getOrNull()
            Log.d("AvatarViewModel", "Usuario cargado: ${user?.username}, Level: ${user?.level}")

            // Cargar avatares
            Log.d("AvatarViewModel", "Cargando avatares...")
            val avatarsResult = repository.getAvatarsForUser(userId)

            if (avatarsResult.isFailure) {
                Log.e("AvatarViewModel", "Error al cargar avatares: ${avatarsResult.exceptionOrNull()?.message}")
                _uiState.value = AvatarUiState(
                    isLoading = false,
                    error = "Error al cargar avatares: ${avatarsResult.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val avatars = avatarsResult.getOrNull() ?: emptyList()
            Log.d("AvatarViewModel", "Avatares cargados: ${avatars.size}")

            avatars.forEach { avatar ->
                Log.d("AvatarViewModel", "  - ${avatar.id}: locked=${avatar.isLocked}, level=${avatar.requiredLevel}, url=${avatar.imageUrl}")
            }

            _uiState.value = AvatarUiState(
                avatars = avatars,
                currentUser = user,
                isLoading = false,
                error = if (avatars.isEmpty()) "No hay avatares disponibles" else null
            )

            Log.d("AvatarViewModel", "=== CARGA COMPLETADA ===")
        }
    }

    fun selectAvatar(userId: String, avatarId: String) {
        viewModelScope.launch {
            Log.d("AvatarViewModel", "Intentando seleccionar avatar: $avatarId")

            val avatar = _uiState.value.avatars.find { it.id == avatarId }

            if (avatar == null) {
                Log.e("AvatarViewModel", "Avatar no encontrado: $avatarId")
                _uiState.value = _uiState.value.copy(error = "Avatar no encontrado")
                return@launch
            }

            if (avatar.isLocked) {
                Log.w("AvatarViewModel", "Avatar bloqueado. Nivel ${avatar.requiredLevel} requerido")
                _uiState.value = _uiState.value.copy(
                    error = "Avatar bloqueado. Nivel ${avatar.requiredLevel} requerido"
                )
                return@launch
            }

            Log.d("AvatarViewModel", "Guardando avatar seleccionado...")
            val result = repository.setCurrentAvatar(userId, avatarId)

            if (result.isSuccess) {
                Log.d("AvatarViewModel", "Avatar seleccionado exitosamente")
                loadAvatars(userId)
            } else {
                Log.e("AvatarViewModel", "Error al cambiar avatar: ${result.exceptionOrNull()?.message}")
                _uiState.value = _uiState.value.copy(
                    error = "Error al cambiar avatar: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}