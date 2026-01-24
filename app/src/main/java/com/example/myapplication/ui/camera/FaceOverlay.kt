package com.example.myapplication.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult

@Composable
fun FaceOverlay(
    faceDetectorResult: FaceDetectorResult?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        faceDetectorResult?.detections()?.forEach { detection ->
            val boundingBox = detection.boundingBox()

            // Scale factors to map detection coordinates to canvas size
            val scaleX = size.width / imageWidth
            val scaleY = size.height / imageHeight

            val left = boundingBox.left * scaleX
            val top = boundingBox.top * scaleY
            val width = boundingBox.width() * scaleX
            val height = boundingBox.height() * scaleY

            // Draw bounding box
            drawRect(
                color = Color.Green,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = 4f)
            )

            // Draw keypoints (eyes, nose, mouth, ears)
            detection.keypoints().orElse(emptyList()).forEach { keypoint ->
                val x = keypoint.x() * size.width
                val y = keypoint.y() * size.height

                drawCircle(
                    color = Color.Cyan,
                    radius = 8f,
                    center = Offset(x, y)
                )
            }

            // Draw confidence score
            val confidence = detection.categories().firstOrNull()?.score() ?: 0f
            drawRect(
                color = Color.Green.copy(alpha = 0.3f),
                topLeft = Offset(left, top - 30f),
                size = Size(width * confidence, 25f)
            )
        }
    }
}
