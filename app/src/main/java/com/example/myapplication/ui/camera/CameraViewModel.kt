package com.example.myapplication.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

data class CameraUiState(
    val hasCameraPermission: Boolean = false,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val capturedPhotos: List<File> = emptyList(),
    val selectedPhoto: File? = null,
    val faceDetectorResult: FaceDetectorResult? = null,
    val detectedImageWidth: Int = 0,
    val detectedImageHeight: Int = 0,
    val faceCount: Int = 0
)

class CameraViewModel(
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CameraUiState(hasCameraPermission = checkCameraPermission())
    )
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val photosDir = File(context.filesDir, "photos").apply { mkdirs() }

    var imageCapture: ImageCapture? = null

    init {
        loadExistingPhotos()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun loadExistingPhotos() {
        val photos = photosDir.listFiles()
            ?.filter { it.extension == "jpg" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        _uiState.update { it.copy(capturedPhotos = photos) }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = granted) }
    }

    fun switchCamera() {
        _uiState.update { state ->
            state.copy(
                lensFacing = if (state.lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT
                else
                    CameraSelector.LENS_FACING_BACK
            )
        }
    }

    fun takePhoto(executor: Executor) {
        val capture = imageCapture ?: return

        val photoFile = File(
            photosDir,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    _uiState.update { it.copy(
                        capturedPhotos = listOf(photoFile) + it.capturedPhotos
                    )}
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraViewModel", "Photo capture failed", exception)
                }
            }
        )
    }

    fun selectPhoto(photo: File) {
        _uiState.update { it.copy(selectedPhoto = photo) }
    }

    fun deleteSelectedPhoto() {
        val photo = _uiState.value.selectedPhoto ?: return
        photo.delete()
        _uiState.update { it.copy(
            capturedPhotos = it.capturedPhotos - photo,
            selectedPhoto = null
        )}
    }

    fun clearSelectedPhoto() {
        _uiState.update { it.copy(selectedPhoto = null) }
    }

    fun updateFaceDetectionResult(result: FaceDetectorResult, imageWidth: Int, imageHeight: Int) {
        _uiState.update { it.copy(
            faceDetectorResult = result,
            detectedImageWidth = imageWidth,
            detectedImageHeight = imageHeight,
            faceCount = result.detections().size
        )}
    }

    fun isFrontCamera(): Boolean = _uiState.value.lensFacing == CameraSelector.LENS_FACING_FRONT

    companion object {
        fun provideFactory(
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CameraViewModel(context) as T
            }
        }
    }
}
