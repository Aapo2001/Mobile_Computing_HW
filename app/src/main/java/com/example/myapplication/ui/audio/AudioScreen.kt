package com.example.myapplication.ui.audio

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.helper.AudioTranscriptionHelper
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.AudioDest
import com.example.myapplication.navigation.NavBar
import androidx.navigation.NavController
import androidx.navigation.NavDestination

@Composable
fun AudioScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val viewModel: AudioViewModel = viewModel(
        factory = AudioViewModel.provideFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            // ViewModel handles cleanup in onCleared
        }
    }

    val extraBottomPadding = contentPadding.calculateBottomPadding()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NavBar(title = AudioDest.label)
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    ) { scaffoldPadding ->
        val bottomBarPadding = scaffoldPadding.calculateBottomPadding() + extraBottomPadding
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 88.dp + bottomBarPadding),
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (!uiState.hasAudioPermission) {
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
                                imageVector = Icons.Default.MicOff,
                                contentDescription = "Microphone",
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Microphone Permission Required",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Grant microphone permission to record audio",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                } else {
                    // Recording Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isRecording)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (uiState.isRecording) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Recording Status",
                                modifier = Modifier.size(64.dp),
                                tint = if (uiState.isRecording)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (uiState.isRecording) "Recording..." else "Ready to Record",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (uiState.isRecording) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "%.1f seconds".format(uiState.recordingDuration),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (uiState.isRecording)
                                    "Tap the button to stop"
                                else
                                    "Tap the microphone button to start recording",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Transcription Card
                if (uiState.hasAudioPermission) {
                    Card(
                        modifier = modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Live Transcription",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (uiState.isTranscribing) {
                                    OutlinedButton(
                                        onClick = { viewModel.stopTranscription() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Stop"
                                        )
                                        Text(" Stop")
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.startTranscription() },
                                        enabled = !uiState.isRecording
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = "Transcribe"
                                        )
                                        Text(" Start")
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Transcription status and result
                            when (val state = uiState.transcriptionState) {
                                is AudioTranscriptionHelper.TranscriptionState.Idle -> {
                                    Text(
                                        text = "Tap Start to begin live transcription",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                is AudioTranscriptionHelper.TranscriptionState.Starting -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "  Starting...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                is AudioTranscriptionHelper.TranscriptionState.Listening -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "  Listening... Speak now",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                is AudioTranscriptionHelper.TranscriptionState.Processing -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "  Processing speech...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                is AudioTranscriptionHelper.TranscriptionState.PartialResult -> {
                                    Text(
                                        text = state.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Light
                                    )
                                }
                                is AudioTranscriptionHelper.TranscriptionState.Success -> {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            text = state.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                                is AudioTranscriptionHelper.TranscriptionState.Error -> {
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            // Show last successful transcription
                            if (uiState.lastTranscription.isNotEmpty() &&
                                uiState.transcriptionState !is AudioTranscriptionHelper.TranscriptionState.Success
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Last transcription:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uiState.lastTranscription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Process with Gemma button
                            if (uiState.lastTranscription.isNotEmpty() ||
                                uiState.transcriptionState is AudioTranscriptionHelper.TranscriptionState.Success
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val textToProcess =
                                            if (uiState.transcriptionState is AudioTranscriptionHelper.TranscriptionState.Success) {
                                                (uiState.transcriptionState as AudioTranscriptionHelper.TranscriptionState.Success).text
                                            } else {
                                                uiState.lastTranscription
                                            }
                                        viewModel.processWithGemma(textToProcess)
                                    },
                                    enabled = viewModel.isGemmaReady() && !uiState.isProcessingWithGemma
                                ) {
                                    if (uiState.isProcessingWithGemma) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text("  Processing...")
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Process with Gemma"
                                        )
                                        Text(" Process with Gemma")
                                    }
                                }
                            }

                            // Gemma status
                            if (!viewModel.isGemmaReady()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.gemmaStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Gemma Transcription Card
                    if (uiState.gemmaTranscription.isNotEmpty() || uiState.gemmaResponse.isNotEmpty() || uiState.isProcessingWithGemma) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Gemma-3n",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "  Gemma-3n Audio Processing",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (uiState.isProcessingWithGemma) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "  Processing audio...",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                if (uiState.gemmaTranscription.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Transcription:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Text(
                                            text = uiState.gemmaTranscription,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (uiState.gemmaResponse.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Response:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.gemmaResponse,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Recordings List
                if (uiState.recordings.isNotEmpty()) {
                    Text(
                        text = "Recordings (${uiState.recordings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        uiState.recordings.forEach { recording ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (uiState.currentlyPlaying == recording)
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = recording.nameWithoutExtension,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "%.1f KB".format(recording.length() / 1024f),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row {
                                            // Transcribe with Gemma button
                                            IconButton(
                                                onClick = { viewModel.transcribeWithGemma(recording) },
                                                enabled = viewModel.isGemmaReady() && !uiState.isProcessingWithGemma
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "Transcribe with Gemma",
                                                    tint = if (viewModel.isGemmaReady())
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (uiState.currentlyPlaying == recording && uiState.isPlaying) {
                                                        viewModel.stopPlayback()
                                                    } else {
                                                        viewModel.playRecording(recording)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (uiState.currentlyPlaying == recording && uiState.isPlaying)
                                                        Icons.Default.Pause
                                                    else
                                                        Icons.Default.PlayArrow,
                                                    contentDescription = if (uiState.currentlyPlaying == recording && uiState.isPlaying)
                                                        "Pause"
                                                    else
                                                        "Play"
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteRecording(recording) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    if (uiState.currentlyPlaying == recording) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { uiState.playbackProgress },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (uiState.hasAudioPermission) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "No recordings yet.\nTap the microphone button to start.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            FloatingActionButton(
                onClick = {
                    if (!uiState.hasAudioPermission) return@FloatingActionButton
                    if (uiState.isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .alpha(if (uiState.hasAudioPermission) 1f else 0.5f),
                containerColor = when {
                    !uiState.hasAudioPermission -> MaterialTheme.colorScheme.surfaceVariant
                    uiState.isRecording -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            ) {
                Icon(
                    imageVector = when {
                        !uiState.hasAudioPermission -> Icons.Default.MicOff
                        uiState.isRecording -> Icons.Default.Stop
                        else -> Icons.Default.Mic
                    },
                    contentDescription = when {
                        !uiState.hasAudioPermission -> "Microphone Permission Required"
                        uiState.isRecording -> "Stop Recording"
                        else -> "Start Recording"
                    }
                )
            }
        }
    }
}
