package com.example.myapplication.helper

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class GemmaHelper(private val context: Context) {
    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private var initError: String? = null

    companion object {
        private const val MODEL_NAME = "gemma3-1B-it-int4.task"
    }

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext true

        try {
            val modelFile = File(context.filesDir, MODEL_NAME)

            // Copy from assets if not already in files directory
            if (!modelFile.exists()) {
                try {
                    copyModelFromAssets(modelFile)
                } catch (e: Exception) {
                    initError = "Model file not found in assets. Please place $MODEL_NAME in app/src/main/assets/"
                    return@withContext false
                }
            }

            val options = LlmInferenceOptions.builder()
                .setModelPath(modelFile.path)
                .setMaxTopK(40)
                .setMaxTokens(1024)
                .setPreferredBackend(LlmInference.Backend.GPU)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
            true
        } catch (e: Exception) {
            initError = "Failed to initialize Gemma: ${e.message}"
            false
        }
    }

    private fun copyModelFromAssets(destFile: File) {
        context.assets.open(MODEL_NAME).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    suspend fun generateResponse(userMessage: String): String = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            return@withContext initError ?: "Model not initialized"
        }

        try {
            val prompt = formatPrompt(userMessage)
            llmInference?.generateResponse(prompt) ?: "No response generated"
        } catch (e: Exception) {
            "Error generating response: ${e.message}"
        }
    }

    private fun formatPrompt(userMessage: String): String {
        return "<start_of_turn>user\n$userMessage<end_of_turn>\n<start_of_turn>model\n"
    }

    fun isReady(): Boolean = isInitialized

    fun getError(): String? = initError

    fun close() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
    }
}
