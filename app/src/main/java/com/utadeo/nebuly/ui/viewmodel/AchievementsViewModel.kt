package com.utadeo.nebuly.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utadeo.nebuly.data.models.Achievement
import com.utadeo.nebuly.data.repository.AchievementsRepository
import kotlinx.coroutines.launch

class AchievementsViewModel : ViewModel() {
    private val repository = AchievementsRepository()

    private val _achievements = mutableStateOf<List<Achievement>>(emptyList())
    val achievements: State<List<Achievement>> = _achievements

    private val _unlockedAchievements = mutableStateOf<List<String>>(emptyList())
    val unlockedAchievements: State<List<String>> = _unlockedAchievements

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    private val TAG = "AchievementsViewModel"

    fun loadAchievements(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            Log.d(TAG, "Cargando logros para usuario: $userId")

            // Cargar todos los logros
            repository.getAllAchievements().fold(
                onSuccess = { allAchievements ->
                    _achievements.value = allAchievements
                    Log.d(TAG, "Logros cargados: ${allAchievements.size}")

                    // Cargar logros desbloqueados
                    repository.getUnlockedAchievements(userId).fold(
                        onSuccess = { unlocked ->
                            _unlockedAchievements.value = unlocked
                            Log.d(TAG, "Logros desbloqueados: ${unlocked.size}")
                            _isLoading.value = false
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error al cargar logros desbloqueados", error)
                            _errorMessage.value = error.message
                            _isLoading.value = false
                        }
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Error al cargar logros", error)
                    _errorMessage.value = error.message
                    _isLoading.value = false
                }
            )
        }
    }

    fun isAchievementUnlocked(achievementId: String): Boolean {
        return _unlockedAchievements.value.contains(achievementId)
    }
}