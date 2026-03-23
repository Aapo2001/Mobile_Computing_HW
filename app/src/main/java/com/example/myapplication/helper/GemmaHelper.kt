package com.example.myapplication.helper

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Thin wrapper around MediaPipe's Gemma text model integration.
 *
 * The helper is responsible for:
 *
 * - copying the bundled model into app-private storage when needed
 * - creating the native MediaPipe inference engine
 * - formatting chat prompts for the home screen
 * - exposing initialization errors in a UI-friendly way
 */
class GemmaHelper(private val context: Context) {
    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private var initError: String? = null

    companion object {
        private const val MODEL_NAME = "gemma3-1B-it-int4.task"
    }

    /**
     * Initializes the inference engine on a background dispatcher.
     *
     * Repeated calls are cheap after the model has already been initialized.
     */
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

    /** Copies the model file from the APK assets folder into internal storage. */
    private fun copyModelFromAssets(destFile: File) {
        context.assets.open(MODEL_NAME).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /** Formats a user message and asks Gemma to continue as the model speaker. */
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

    /** Converts plain user text into the turn-based prompt template expected by the model. */
    private fun formatPrompt(userMessage: String): String {
        return "<start_of_turn>user\n$userMessage<end_of_turn>\n<start_of_turn>model\n"
    }

    /** Returns `true` once the model has been initialized successfully. */
    fun isReady(): Boolean = isInitialized

    /** Returns the last initialization error captured by [initialize], if any. */
    fun getError(): String? = initError

    /** Releases the native inference engine and resets helper state. */
    fun close() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
    }
}
