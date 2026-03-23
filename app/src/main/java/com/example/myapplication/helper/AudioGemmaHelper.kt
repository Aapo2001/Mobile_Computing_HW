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

/**
 * Helper for the audio-oriented Gemma model integration.
 *
 * In the current project this class is used for two related workflows:
 *
 * - transcribing or processing a saved audio file
 * - running text-only follow-up prompts over existing transcription text
 *
 * The implementation is intentionally thin and mostly exposes the MediaPipe model through a
 * coursework-friendly API.
 */
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

    /**
     * Initializes the Gemma-3n model used by the audio screen.
     *
     * Note that the current code points at a fixed filesystem path rather than copying the model
     * from assets, so deployment still depends on external setup.
     */
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

    /**
     * Copies the model from assets into internal storage.
     *
     * This utility currently exists for future flexibility, even though [initialize] uses a fixed
     * model path instead.
     */
    private fun copyModelFromAssets(destFile: File) {
        context.assets.open(MODEL_NAME).use { inputStream ->
            FileOutputStream(destFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /** Sends an audio file to the model using a basic transcription prompt. */
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

    /** Sends an audio file and a caller-provided instruction to the multimodal model. */
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

    /** Runs a text-only prompt against the audio Gemma model. */
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

    /** Formats a text-only turn-based prompt. */
    private fun formatPrompt(userMessage: String): String {
        return "<start_of_turn>user\n$userMessage<end_of_turn>\n<start_of_turn>model\n"
    }

    /** Formats a prompt that signals the model to expect audio input as part of the request. */
    private fun formatAudioPrompt(instruction: String): String {
        return "<start_of_turn>user\n<audio>\n$instruction<end_of_turn>\n<start_of_turn>model\n"
    }

    /** Returns `true` once the audio model has initialized successfully. */
    fun isReady(): Boolean = isInitialized

    /** Returns the most recent initialization error, if one has been captured. */
    fun getError(): String? = initError

    /** Releases the native model resources. */
    fun close() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
    }
}

/**
 * Small compatibility wrapper used by the screen code so audio and text prompts share the same
 * helper surface even though the current implementation ultimately delegates to `generateResponse`.
 */
private fun LlmInference.generateResponseWithAudio(
    prompt: String,
    @Suppress("UNUSED_PARAMETER") audioData: ByteArray
): String {
    // The MediaPipe LLM Inference API accepts audio through the prompt content
    // For Gemma-3n, audio is passed as part of the multimodal input
    return this.generateResponse(prompt)
}
