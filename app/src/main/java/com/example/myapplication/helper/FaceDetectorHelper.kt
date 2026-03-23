package com.example.myapplication.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult

/**
 * Encapsulates MediaPipe face detection setup for the camera screen.
 *
 * The helper handles delegate selection, model configuration, image rotation/mirroring, and
 * forwarding detection results back to the UI through [DetectorListener].
 */
class FaceDetectorHelper(
    private val context: Context,
    private val runningMode: RunningMode = RunningMode.LIVE_STREAM,
    private val minDetectionConfidence: Float = 0.5f,
    private val resultListener: DetectorListener? = null
) {
    companion object {
        private const val TAG = "FaceDetectorHelper"
    }

    private var faceDetector: FaceDetector? = null

    init {
        setupFaceDetector()
    }

    /**
     * Attempts to initialize the detector with GPU first and falls back to CPU when needed.
     *
     * This makes the helper more tolerant of devices where the preferred delegate is unavailable.
     */
    private fun setupFaceDetector() {
        val delegates = listOf(Delegate.GPU, Delegate.CPU)
        var lastError: Exception? = null

        for (delegate in delegates) {
            try {
                faceDetector = FaceDetector.createFromOptions(context, buildOptions(delegate))
                Log.d(TAG, "Face detector initialized with $delegate delegate")
                return
            } catch (e: Exception) {
                lastError = e
                Log.w(TAG, "Face detector initialization failed with $delegate delegate", e)
            }
        }

        resultListener?.onError(
            "Face detector initialization failed: ${lastError?.message ?: "Unknown error"}"
        )
    }

    /** Builds MediaPipe detector options for the requested delegate. */
    private fun buildOptions(delegate: Delegate): FaceDetector.FaceDetectorOptions {
        val baseOptions = BaseOptions.builder()
            .setDelegate(delegate)
            .setModelAssetPath("face_detection_short_range.tflite")
            .build()

        return FaceDetector.FaceDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setMinDetectionConfidence(minDetectionConfidence)
            .setRunningMode(runningMode)
            .apply {
                if (runningMode == RunningMode.LIVE_STREAM) {
                    setResultListener { result, input ->
                        resultListener?.onResults(
                            result,
                            input.width,
                            input.height
                        )
                    }
                    setErrorListener { error ->
                        resultListener?.onError(error.message ?: "Face detection error")
                    }
                }
            }
            .build()
    }

    /**
     * Converts a CameraX frame into a correctly rotated bitmap and submits it for asynchronous
     * face detection.
     *
     * @param isFrontCamera whether the current preview is mirrored due to front-camera usage.
     */
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

    /** Releases the MediaPipe detector. */
    fun close() {
        faceDetector?.close()
        faceDetector = null
    }

    /** Callback contract used by the camera UI to receive results and errors. */
    interface DetectorListener {
        fun onResults(result: FaceDetectorResult, imageWidth: Int, imageHeight: Int)
        fun onError(error: String)
    }
}
