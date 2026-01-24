package com.example.myapplication.ui.video

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VideoUiState(
    val videoUri: Uri? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val sliderPosition: Float = 0f,
    val isSeeking: Boolean = false
)

class VideoViewModel(
    context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = _uiState.asStateFlow()

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _uiState.update { it.copy(duration = this@apply.duration) }
                }
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                _uiState.update { it.copy(isPlaying = playing) }
                if (playing) {
                    startPositionUpdates()
                }
            }
        })
    }

    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                if (!_uiState.value.isSeeking) {
                    val currentPosition = exoPlayer.currentPosition
                    val duration = _uiState.value.duration
                    _uiState.update { it.copy(
                        currentPosition = currentPosition,
                        sliderPosition = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
                    )}
                }
                delay(100)
            }
        }
    }

    fun selectVideo(uri: Uri) {
        _uiState.update { it.copy(videoUri = uri) }
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.prepare()
    }

    fun selectAndPlayVideo(uri: Uri) {
        selectVideo(uri)
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun stop() {
        exoPlayer.stop()
        exoPlayer.seekTo(0)
        _uiState.update { it.copy(
            currentPosition = 0,
            sliderPosition = 0f
        )}
    }

    fun onSliderValueChange(value: Float) {
        _uiState.update { it.copy(isSeeking = true, sliderPosition = value) }
    }

    fun onSliderValueChangeFinished() {
        val duration = _uiState.value.duration
        exoPlayer.seekTo((_uiState.value.sliderPosition * duration).toLong())
        _uiState.update { it.copy(isSeeking = false) }
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    companion object {
        fun provideFactory(
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return VideoViewModel(context) as T
            }
        }
    }
}
