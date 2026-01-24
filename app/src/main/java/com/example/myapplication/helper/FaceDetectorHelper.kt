package com.example.myapplication.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult

class FaceDetectorHelper(
    private val context: Context,
    private val runningMode: RunningMode = RunningMode.LIVE_STREAM,
    private val minDetectionConfidence: Float = 0.5f,
    private val resultListener: DetectorListener? = null
) {
    private var faceDetector: FaceDetector? = null

    init {
        setupFaceDetector()
    }

    private fun setupFaceDetector() {
        val baseOptionsBuilder = BaseOptions.builder()
            .setDelegate(Delegate.GPU)
            .setModelAssetPath("face_detection_short_range.tflite")

        val optionsBuilder = FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setMinDetectionConfidence(minDetectionConfidence)
            .setRunningMode(runningMode)

        if (runningMode == RunningMode.LIVE_STREAM) {
            optionsBuilder.setResultListener { result, input ->
                resultListener?.onResults(
                    result,
                    input.width,
                    input.height
                )
            }.setErrorListener { error ->
                resultListener?.onError(error.message ?: "Face detection error")
            }
        }

        try {
            faceDetector = FaceDetector.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            resultListener?.onError("Face detector initialization failed: ${e.message}")
        }
    }

    fun detectLiveStream(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        val frameTime = SystemClock.uptimeMillis()

        val bitmap = imageProxy.toBitmap()
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            if (isFrontCamera) {
                postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            }
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        faceDetector?.detectAsync(mpImage, frameTime)
    }

    fun close() {
        faceDetector?.close()
        faceDetector = null
    }

    interface DetectorListener {
        fun onResults(result: FaceDetectorResult, imageWidth: Int, imageHeight: Int)
        fun onError(error: String)
    }
}
