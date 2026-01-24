package com.example.myapplication.ui.video

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.NavBar
import com.example.myapplication.navigation.VideoDest

@OptIn(UnstableApi::class)
@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current

    val viewModel: VideoViewModel = viewModel(
        factory = VideoViewModel.provideFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.selectVideo(it) }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            // ViewModel handles cleanup in onCleared
        }
    }

    // Sample video URLs
    val sampleVideos = listOf(
        "Big Buck Bunny" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "Elephant Dream" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "Sintel" to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            NavBar(title = VideoDest.label)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Video Player Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Video Player View
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.videoUri != null || viewModel.exoPlayer.mediaItemCount > 0) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = viewModel.exoPlayer
                                        useController = false
                                        layoutParams = FrameLayout.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "No Video",
                                    modifier = Modifier.padding(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "No video selected",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Slider
                    if (uiState.duration > 0) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Slider(
                                value = uiState.sliderPosition,
                                onValueChange = { viewModel.onSliderValueChange(it) },
                                onValueChangeFinished = { viewModel.onSliderValueChangeFinished() },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatTime(uiState.currentPosition),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = formatTime(uiState.duration),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Playback Controls - M3 styled
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = { viewModel.togglePlayPause() },
                            enabled = uiState.videoUri != null || viewModel.exoPlayer.mediaItemCount > 0,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play"
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { viewModel.stop() },
                            enabled = uiState.videoUri != null || viewModel.exoPlayer.mediaItemCount > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop"
                            )
                        }
                    }
                }
            }

            // Pick Video Button
            Button(
                onClick = { videoPickerLauncher.launch("video/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = "Pick Video"
                )
                Text(" Select Video from Device", modifier = Modifier.padding(start = 8.dp))
            }

            // Sample Videos Card - M3 ElevatedCard
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sample Videos (Online)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    sampleVideos.forEach { (name, url) ->
                        FilledTonalButton(
                            onClick = { viewModel.selectAndPlayVideo(url.toUri()) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(name)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
