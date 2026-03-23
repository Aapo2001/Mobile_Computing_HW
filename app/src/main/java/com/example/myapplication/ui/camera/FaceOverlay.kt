package com.example.myapplication.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult

/**
 * Draws face-detection results on top of the live camera preview.
 *
 * The overlay converts MediaPipe detection coordinates into the current canvas coordinate space and
 * then draws rounded bounding boxes, keypoints, and a small confidence badge for each detection.
 */
@Composable
fun FaceOverlay(
    faceDetectorResult: FaceDetectorResult?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (imageWidth <= 0 || imageHeight <= 0) return@Canvas

        faceDetectorResult?.detections()?.forEach { detection ->
            val boundingBox = detection.boundingBox()

            // Scale factors to map detection coordinates to canvas size
            val scaleX = size.width / imageWidth
            val scaleY = size.height / imageHeight

            val left = boundingBox.left * scaleX
            val top = boundingBox.top * scaleY
            val width = boundingBox.width() * scaleX
            val height = boundingBox.height() * scaleY

            // Draw bounding box with thick stroke
            drawRoundRect(
                color = Color.Green,
                topLeft = Offset(left, top),
                size = Size(width, height),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 6f)
            )

            // Draw keypoints (eyes, nose, mouth, ears)
            detection.keypoints().orElse(emptyList()).forEach { keypoint ->
                val x = keypoint.x() * size.width
                val y = keypoint.y() * size.height

                // Outer ring
                drawCircle(
                    color = Color.Cyan,
                    radius = 12f,
                    center = Offset(x, y),
                    style = Stroke(width = 3f)
                )
                // Inner filled dot
                drawCircle(
                    color = Color.Cyan,
                    radius = 6f,
                    center = Offset(x, y)
                )
            }

            // Draw confidence label background
            val confidence = detection.categories().firstOrNull()?.score() ?: 0f
            val labelText = "${(confidence * 100).toInt()}%"
            val labelBgHeight = 32f
            val labelBgWidth = 64f

            drawRoundRect(
                color = Color.Green.copy(alpha = 0.85f),
                topLeft = Offset(left, top - labelBgHeight - 4f),
                size = Size(labelBgWidth, labelBgHeight),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // Draw confidence text
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 24f
                    isFakeBoldText = true
                    isAntiAlias = true
                }
                canvas.nativeCanvas.drawText(
                    labelText,
                    left + 8f,
                    top - 10f,
                    paint
                )
            }
        }
    }
}
