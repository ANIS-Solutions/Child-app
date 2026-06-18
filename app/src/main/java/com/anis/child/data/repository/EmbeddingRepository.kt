package com.anis.child.data.repository

import android.content.Context
import android.util.Log
import com.anis.child.data.PreferenceManager
import com.anis.child.network.ApiResult
import com.anis.child.network.ApiService
import com.anis.child.network.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmbeddingRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    @ApplicationContext private val context: Context
) {
    suspend fun fetchAndSaveEmbeddings(promptId: String): Boolean {
        return when (val result = safeApiCall { apiService.getPromptsEmbedding() }) {
            is ApiResult.Success -> {
                val body = result.data.body() ?: return false
                val bytes = body.bytes()
                if (bytes.isEmpty()) {
                    Log.e(TAG, "Empty embedding response for promptId=$promptId")
                    return false
                }
                val file = getEmbeddingFile(promptId)
                file.parentFile?.mkdirs()
                file.writeBytes(bytes)
                preferenceManager.promptsEmbeddingId = promptId
                preferenceManager.promptsEmbeddingVersion = System.currentTimeMillis()
                Log.d(TAG, "Saved embeddings for promptId=$promptId (${bytes.size} bytes)")
                true
            }
            is ApiResult.Error -> {
                Log.e(TAG, "Failed to fetch embeddings: ${result.message}")
                false
            }
        }
    }

    fun getLocalEmbeddingFile(promptId: String): File? {
        val file = getEmbeddingFile(promptId)
        return if (file.exists()) file else null
    }

    fun getCurrentEmbeddingFile(): File? {
        val promptId = preferenceManager.promptsEmbeddingId ?: return null
        return getLocalEmbeddingFile(promptId)
    }

    private fun getEmbeddingFile(promptId: String): File {
        return File(context.filesDir, "$EMBEDDINGS_DIR/$promptId.json")
    }

    companion object {
        private const val TAG = "EmbeddingRepository"
        private const val EMBEDDINGS_DIR = "embeddings"
    }
}
