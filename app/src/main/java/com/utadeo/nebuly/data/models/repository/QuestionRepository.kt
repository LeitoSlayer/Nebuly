package com.utadeo.nebuly.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.utadeo.nebuly.data.models.Question
import kotlinx.coroutines.tasks.await

class QuestionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val questionsCollection = firestore.collection("questions")

    private val TAG = "🔥QUESTIONS_DEBUG🔥"

    /**
     * Obtiene las 5 preguntas de un nivel específico
     */
    suspend fun getQuestionsForLevel(levelId: String): Result<List<Question>> {
        return try {
            Log.e(TAG, "========================================")
            Log.e(TAG, "OBTENIENDO PREGUNTAS PARA: $levelId")
            Log.e(TAG, "========================================")

            val snapshot = questionsCollection
                .whereEqualTo("levelId", levelId)
                .orderBy("questionNumber")
                .get()
                .await()

            Log.e(TAG, "Preguntas encontradas: ${snapshot.documents.size}")

            val questions = snapshot.documents.mapNotNull { doc ->
                try {
                    val question = Question(
                        id = doc.id,
                        levelId = doc.getString("levelId") ?: "",
                        questionNumber = doc.getLong("questionNumber")?.toInt() ?: 0,
                        questionText = doc.getString("questionText") ?: "",
                        options = (doc.get("options") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        correctAnswer = doc.getLong("correctAnswer")?.toInt() ?: 0,
                        reward = doc.getLong("reward")?.toInt() ?: 50
                    )

                    Log.e(TAG, "Pregunta ${question.questionNumber}: ${question.questionText.take(50)}...")
                    question
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando pregunta ${doc.id}", e)
                    null
                }
            }

            if (questions.isEmpty()) {
                Log.e(TAG, "⚠️ NO SE ENCONTRARON PREGUNTAS PARA $levelId")
                return Result.failure(Exception("No hay preguntas disponibles para este nivel"))
            }

            Log.e(TAG, "✅ Total preguntas cargadas: ${questions.size}")
            Result.success(questions)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR AL OBTENER PREGUNTAS: ${e.message}", e)
            Result.failure(e)
        }
    }
}