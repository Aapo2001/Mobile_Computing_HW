package com.example.myapplication.helper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class AudioTranscriptionHelper(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _transcriptionState = MutableStateFlow<TranscriptionState>(TranscriptionState.Idle)
    val transcriptionState: StateFlow<TranscriptionState> = _transcriptionState

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            setupSpeechRecognizer()
        } else {
            _transcriptionState.value = TranscriptionState.Error("Speech recognition not available on this device")
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _transcriptionState.value = TranscriptionState.Listening
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {
                    _transcriptionState.value = TranscriptionState.Processing
                }

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                        else -> "Unknown error"
                    }
                    _transcriptionState.value = TranscriptionState.Error(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _transcriptionState.value = TranscriptionState.Success(matches[0])
                    } else {
                        _transcriptionState.value = TranscriptionState.Error("No transcription result")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _transcriptionState.value = TranscriptionState.PartialResult(matches[0])
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            _transcriptionState.value = TranscriptionState.Error("Speech recognizer not initialized")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        _transcriptionState.value = TranscriptionState.Starting
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun resetState() {
        _transcriptionState.value = TranscriptionState.Idle
    }

    fun close() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    sealed class TranscriptionState {
        data object Idle : TranscriptionState()
        data object Starting : TranscriptionState()
        data object Listening : TranscriptionState()
        data object Processing : TranscriptionState()
        data class PartialResult(val text: String) : TranscriptionState()
        data class Success(val text: String) : TranscriptionState()
        data class Error(val message: String) : TranscriptionState()
    }
}
