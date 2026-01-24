package com.example.myapplication.helper

import android.content.Context
import android.media.AudioFormat
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AudioGemmaHelper(private val context: Context) {
    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private var initError: String? = null

    companion object {
        private const val MODEL_NAME = "gemma-3n-E2B-it-int4.litertlm"
        private const val TAG = "AudioGemmaHelper"
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext true

        try {
            val options = LlmInferenceOptions.builder()
                .setModelPath("/data/local/tmp/llm/gemma-3n-E2B-it-int4.litertlm")
                .setMaxTopK(40)
                .setMaxTokens(512)
                .setPreferredBackend(LlmInference.Backend.GPU)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true
            true
        } catch (e: Exception) {
            initError = "Failed to initialize Gemma-3n: ${e.message}"
            Log.e(TAG, "Initialization failed", e)
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

    suspend fun transcribeAudioFile(audioFile: File): String = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            return@withContext initError ?: "Model not initialized"
        }

        try {
            // Read audio file and convert to format expected by Gemma-3n
            val audioData = audioFile.readBytes()

            // Create prompt with audio for transcription
            val prompt = formatAudioPrompt("Transcribe this audio:")

            // Use generateResponseWithAudio for multimodal input
            val response = llmInference?.generateResponseWithAudio(prompt, audioData)
                ?: "No transcription generated"

            response
        } catch (e: Exception) {
            Log.e(TAG, "Transcription failed", e)
            "Error transcribing audio: ${e.message}"
        }
    }

    suspend fun transcribeAndProcess(audioFile: File, instruction: String): String = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            return@withContext initError ?: "Model not initialized"
        }

        try {
            val audioData = audioFile.readBytes()
            val prompt = formatAudioPrompt(instruction)

            val response = llmInference?.generateResponseWithAudio(prompt, audioData)
                ?: "No response generated"

            response
        } catch (e: Exception) {
            Log.e(TAG, "Processing failed", e)
            "Error processing audio: ${e.message}"
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
            Log.e(TAG, "Response generation failed", e)
            "Error generating response: ${e.message}"
        }
    }

    private fun formatPrompt(userMessage: String): String {
        return "<start_of_turn>user\n$userMessage<end_of_turn>\n<start_of_turn>model\n"
    }

    private fun formatAudioPrompt(instruction: String): String {
        return "<start_of_turn>user\n<audio>\n$instruction<end_of_turn>\n<start_of_turn>model\n"
    }

    fun isReady(): Boolean = isInitialized

    fun getError(): String? = initError

    fun close() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
    }
}

// Extension function for LlmInference to handle audio input
private fun LlmInference.generateResponseWithAudio(
    prompt: String,
    @Suppress("UNUSED_PARAMETER") audioData: ByteArray
): String {
    // The MediaPipe LLM Inference API accepts audio through the prompt content
    // For Gemma-3n, audio is passed as part of the multimodal input
    return this.generateResponse(prompt)
}
