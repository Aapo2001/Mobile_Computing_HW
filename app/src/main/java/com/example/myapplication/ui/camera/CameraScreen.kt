package com.example.myapplication.ui.camera

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.helper.FaceDetectorHelper
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.CameraDest
import com.example.myapplication.navigation.NavBar
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewModel: CameraViewModel = viewModel(
        factory = CameraViewModel.provideFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // Face detector
    val faceDetectorHelper = remember {
        FaceDetectorHelper(
            context = context,
            resultListener = object : FaceDetectorHelper.DetectorListener {
                override fun onResults(
                    result: FaceDetectorResult,
                    imageWidth: Int,
                    imageHeight: Int
                ) {
                    viewModel.updateFaceDetectionResult(result, imageWidth, imageHeight)
                }

                override fun onError(error: String) {
                    Log.e("CameraScreen", "Face detection error: $error")
                }
            }
        )
    }

    // Camera executor
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    // Setup camera when permission is granted
    LaunchedEffect(uiState.hasCameraPermission, uiState.lensFacing) {
        if (uiState.hasCameraPermission && previewView != null) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView!!.surfaceProvider)
                }

                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                viewModel.imageCapture = imageCapture

                // Image analysis for face detection
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            faceDetectorHelper.detectLiveStream(imageProxy, viewModel.isFrontCamera())
                            imageProxy.close()
                        }
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(uiState.lensFacing)
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraScreen", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            faceDetectorHelper.close()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            NavBar(title = CameraDest.label)
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!uiState.hasCameraPermission) {
                // Permission Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Grant camera permission to take photos",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else {
                // Camera Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f),
                        contentAlignment = Alignment.Center
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).also { previewView = it }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Face detection overlay
                        FaceOverlay(
                            faceDetectorResult = uiState.faceDetectorResult,
                            imageWidth = uiState.detectedImageWidth,
                            imageHeight = uiState.detectedImageHeight,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Face count indicator
                        if (uiState.faceCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Faces: ${uiState.faceCount}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        // Camera controls overlay
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Switch camera button
                                IconButton(
                                    onClick = { viewModel.switchCamera() },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cameraswitch,
                                        contentDescription = "Switch Camera",
                                        tint = Color.White
                                    )
                                }

                                // Capture button - M3 Large FAB
                                FloatingActionButton(
                                    onClick = { viewModel.takePhoto(cameraExecutor) },
                                    modifier = Modifier.size(72.dp),
                                    containerColor = MaterialTheme.colorScheme.primary
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Camera,
                                        contentDescription = "Take Photo",
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                // Placeholder for symmetry
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                        }
                    }
                }
            }

            // Captured Photos Section
            if (uiState.capturedPhotos.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Captured Photos (${uiState.capturedPhotos.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(uiState.capturedPhotos) { photo ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (uiState.selectedPhoto == photo) 3.dp else 0.dp,
                                            color = if (uiState.selectedPhoto == photo) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.selectPhoto(photo) }
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(photo),
                                        contentDescription = "Captured Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected Photo Preview
            uiState.selectedPhoto?.let { photo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected Photo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Image(
                            painter = rememberAsyncImagePainter(photo),
                            contentDescription = "Selected Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3f / 4f)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.deleteSelectedPhoto() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Photo")
                        }
                    }
                }
            }
        }
    }
}
