package com.example.myapplication.ui.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.helper.AudioGemmaHelper
import com.example.myapplication.helper.AudioTranscriptionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

data class AudioUiState(
    val hasAudioPermission: Boolean = false,
    val isRecording: Boolean = false,
    val recordingDuration: Float = 0f,
    val recordings: List<File> = emptyList(),
    val currentlyPlaying: File? = null,
    val isPlaying: Boolean = false,
    val playbackProgress: Float = 0f,
    val transcriptionState: AudioTranscriptionHelper.TranscriptionState = AudioTranscriptionHelper.TranscriptionState.Idle,
    val isTranscribing: Boolean = false,
    val lastTranscription: String = "",
    val gemmaStatus: String = "Initializing Gemma-3n...",
    val isProcessingWithGemma: Boolean = false,
    val gemmaTranscription: String = "",
    val gemmaResponse: String = ""
)

class AudioViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AudioUiState(hasAudioPermission = checkAudioPermission())
    )

    private val recordingsDir = File(context.filesDir, "recordings").apply { mkdirs() }
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null

    private val audioGemmaHelper = AudioGemmaHelper(context)
    val transcriptionHelper = AudioTranscriptionHelper(context)

    val uiState: StateFlow<AudioUiState> = combine(
        _uiState,
        transcriptionHelper.transcriptionState,
        transcriptionHelper.isListening
    ) { state, transcriptionState, isTranscribing ->
        state.copy(
            transcriptionState = transcriptionState,
            isTranscribing = isTranscribing,
            lastTranscription = if (transcriptionState is AudioTranscriptionHelper.TranscriptionState.Success) {
                transcriptionState.text
            } else {
                state.lastTranscription
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AudioUiState(hasAudioPermission = checkAudioPermission())
    )

    init {
        loadExistingRecordings()
        initializeGemma()
    }

    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun loadExistingRecordings() {
        val recordings = recordingsDir.listFiles()
            ?.filter { it.extension == "waw" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        _uiState.update { it.copy(recordings = recordings) }
    }

    private fun initializeGemma() {
        viewModelScope.launch {
            val success = audioGemmaHelper.initialize()
            _uiState.update { it.copy(
                gemmaStatus = if (success) "Gemma-3n ready" else (audioGemmaHelper.getError() ?: "Failed to initialize")
            )}
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasAudioPermission = granted) }
    }

    fun startRecording() {
        val audioFile = File(
            recordingsDir,
            "recording_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.waw"
        )

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
                setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
                _uiState.update { it.copy(isRecording = true, recordingDuration = 0f) }
                startRecordingTimer()
            } catch (e: IOException) {
                Log.e("AudioViewModel", "Recording failed", e)
            }
        }
    }

    private fun startRecordingTimer() {
        viewModelScope.launch {
            while (_uiState.value.isRecording) {
                delay(100)
                _uiState.update { it.copy(recordingDuration = it.recordingDuration + 0.1f) }
            }
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioViewModel", "Stop recording failed", e)
        }
        recorder = null
        _uiState.update { it.copy(isRecording = false) }
        loadExistingRecordings()
    }

    fun playRecording(file: File) {
        player?.release()
        player = null
        _uiState.update { it.copy(isPlaying = false) }

        player = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
                _uiState.update { it.copy(currentlyPlaying = file, isPlaying = true) }
                startPlaybackProgressUpdates()

                setOnCompletionListener {
                    _uiState.update { it.copy(isPlaying = false, currentlyPlaying = null, playbackProgress = 0f) }
                }
            } catch (e: IOException) {
                Log.e("AudioViewModel", "Playback failed", e)
            }
        }
    }

    private fun startPlaybackProgressUpdates() {
        viewModelScope.launch {
            while (_uiState.value.isPlaying && player?.isPlaying == true) {
                val current = player?.currentPosition?.toFloat() ?: 0f
                val total = player?.duration?.toFloat() ?: 1f
                _uiState.update { it.copy(playbackProgress = current / total) }
                delay(100)
            }
            if (!_uiState.value.isPlaying || player?.isPlaying != true) {
                _uiState.update { it.copy(playbackProgress = 0f) }
            }
        }
    }

    fun stopPlayback() {
        player?.stop()
        player?.release()
        player = null
        _uiState.update { it.copy(isPlaying = false, currentlyPlaying = null, playbackProgress = 0f) }
    }

    fun deleteRecording(file: File) {
        if (_uiState.value.currentlyPlaying == file) {
            stopPlayback()
        }
        file.delete()
        _uiState.update { it.copy(recordings = it.recordings - file) }
    }

    fun startTranscription() {
        transcriptionHelper.startListening()
    }

    fun stopTranscription() {
        transcriptionHelper.stopListening()
    }

    fun transcribeWithGemma(audioFile: File) {
        if (!audioGemmaHelper.isReady()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingWithGemma = true, gemmaTranscription = "", gemmaResponse = "") }
            val transcription = audioGemmaHelper.transcribeAudioFile(audioFile)
            _uiState.update { it.copy(gemmaTranscription = transcription, isProcessingWithGemma = false) }
        }
    }

    fun processWithGemma(text: String) {
        if (text.isBlank() || !audioGemmaHelper.isReady()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingWithGemma = true, gemmaResponse = "") }
            val prompt = "Please summarize or enhance the following transcribed speech:\n\n\"$text\"\n\nProvide a clear and concise response:"
            val response = audioGemmaHelper.generateResponse(prompt)
            _uiState.update { it.copy(gemmaResponse = response, isProcessingWithGemma = false) }
        }
    }

    fun isGemmaReady(): Boolean = audioGemmaHelper.isReady()

    override fun onCleared() {
        super.onCleared()
        recorder?.release()
        player?.release()
        transcriptionHelper.close()
        audioGemmaHelper.close()
    }

    companion object {
        fun provideFactory(
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AudioViewModel(context) as T
            }
        }
    }
}
