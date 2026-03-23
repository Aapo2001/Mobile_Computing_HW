package com.example.myapplication.ui.audio

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.helper.AudioGemmaHelper
import com.example.myapplication.helper.AudioTranscriptionHelper
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.AudioDest
import com.example.myapplication.navigation.NavBar
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Audio feature screen.
 *
 * The screen groups several related audio workflows into one place:
 *
 * - recording microphone audio with [MediaRecorder]
 * - playing saved recordings with [MediaPlayer]
 * - live speech-to-text through [AudioTranscriptionHelper]
 * - optional Gemma-based transcription and text processing through [AudioGemmaHelper]
 *
 * Saved recordings are written into `filesDir/recordings/`.
 */
@Composable
fun AudioScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Compose-local state mirrors the current recording, playback, transcription, and AI session.
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableFloatStateOf(0f) }
    var recordings by remember { mutableStateOf<List<File>>(emptyList()) }
    var currentlyPlaying by remember { mutableStateOf<File?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }
    var lastTranscription by remember { mutableStateOf("") }
    var gemmaStatus by remember { mutableStateOf("Initializing Gemma-3n...") }
    var isProcessingWithGemma by remember { mutableStateOf(false) }
    var gemmaTranscription by remember { mutableStateOf("") }
    var gemmaResponse by remember { mutableStateOf("") }

    val recordingsDir = remember { File(context.filesDir, "recordings").apply { mkdirs() } }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }

    val audioGemmaHelper = remember { AudioGemmaHelper(context) }
    val transcriptionHelper = remember { AudioTranscriptionHelper(context) }

    // Collect transcription state
    val transcriptionState by transcriptionHelper.transcriptionState.collectAsState()
    val isTranscribing by transcriptionHelper.isListening.collectAsState()

    // Track last successful transcription
    LaunchedEffect(transcriptionState) {
        if (transcriptionState is AudioTranscriptionHelper.TranscriptionState.Success) {
            lastTranscription = (transcriptionState as AudioTranscriptionHelper.TranscriptionState.Success).text
        }
    }

    // Load existing recordings
    LaunchedEffect(Unit) {
        val existingRecordings = recordingsDir.listFiles()
            ?.filter { it.extension == "waw" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        recordings = existingRecordings
    }

    // Initialize Gemma
    LaunchedEffect(Unit) {
        val success = audioGemmaHelper.initialize()
        gemmaStatus = if (success) "Gemma-3n ready" else (audioGemmaHelper.getError() ?: "Failed to initialize")
    }

    // Recording timer
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(100)
            recordingDuration += 0.1f
        }
    }

    // Playback progress updates
    LaunchedEffect(isPlaying) {
        while (isPlaying && player?.isPlaying == true) {
            val current = player?.currentPosition?.toFloat() ?: 0f
            val total = player?.duration?.toFloat() ?: 1f
            playbackProgress = current / total
            delay(100)
        }
        if (!isPlaying || player?.isPlaying != true) {
            playbackProgress = 0f
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            recorder?.release()
            player?.release()
            transcriptionHelper.close()
            audioGemmaHelper.close()
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    val loadRecordings = {
        val existingRecordings = recordingsDir.listFiles()
            ?.filter { it.extension == "waw" }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        recordings = existingRecordings
    }

    // Starts a new recorder session and writes into a timestamped file in internal storage.
    val startRecording = {
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
                isRecording = true
                recordingDuration = 0f
            } catch (e: IOException) {
                Log.e("AudioScreen", "Recording failed", e)
            }
        }
    }

    val stopRecording = {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("AudioScreen", "Stop recording failed", e)
        }
        recorder = null
        isRecording = false
        loadRecordings()
    }

    // Shared stop helper so all playback exits reset the same UI state consistently.
    val stopPlayback = {
        player?.stop()
        player?.release()
        player = null
        isPlaying = false
        currentlyPlaying = null
        playbackProgress = 0f
    }

    val playRecording = { file: File ->
        player?.release()
        player = null
        isPlaying = false

        player = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
                currentlyPlaying = file
                isPlaying = true

                setOnCompletionListener {
                    isPlaying = false
                    currentlyPlaying = null
                    playbackProgress = 0f
                }
            } catch (e: IOException) {
                Log.e("AudioScreen", "Playback failed", e)
            }
        }
    }

    val deleteRecording = { file: File ->
        if (currentlyPlaying == file) {
            stopPlayback()
        }
        file.delete()
        recordings = recordings - file
    }

    // Uses the audio Gemma model to transcribe or interpret a recorded file.
    val transcribeWithGemma = { audioFile: File ->
        if (audioGemmaHelper.isReady()) {
            coroutineScope.launch {
                isProcessingWithGemma = true
                gemmaTranscription = ""
                gemmaResponse = ""
                val transcription = audioGemmaHelper.transcribeAudioFile(audioFile)
                gemmaTranscription = transcription
                isProcessingWithGemma = false
            }
        }
    }

    // Sends already transcribed text to Gemma for summarization or refinement.
    val processWithGemma = { text: String ->
        if (text.isNotBlank() && audioGemmaHelper.isReady()) {
            coroutineScope.launch {
                isProcessingWithGemma = true
                gemmaResponse = ""
                val prompt = "Please summarize or enhance the following transcribed speech:\n\n\"$text\"\n\nProvide a clear and concise response:"
                val response = audioGemmaHelper.generateResponse(prompt)
                gemmaResponse = response
                isProcessingWithGemma = false
            }
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

                if (!hasAudioPermission) {
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
                            containerColor = if (isRecording)
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
                                imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Recording Status",
                                modifier = Modifier.size(64.dp),
                                tint = if (isRecording)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isRecording) "Recording..." else "Ready to Record",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (isRecording) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "%.1f seconds".format(recordingDuration),
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
                                text = if (isRecording)
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
                if (hasAudioPermission) {
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

                                if (isTranscribing) {
                                    OutlinedButton(
                                        onClick = { transcriptionHelper.stopListening() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Stop"
                                        )
                                        Text(" Stop")
                                    }
                                } else {
                                    Button(
                                        onClick = { transcriptionHelper.startListening() },
                                        enabled = !isRecording
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
                            when (val state = transcriptionState) {
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

                            // Keep the most recent final transcription visible even after state changes.
                            if (lastTranscription.isNotEmpty() &&
                                transcriptionState !is AudioTranscriptionHelper.TranscriptionState.Success
                            ) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Last transcription:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = lastTranscription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Process with Gemma button
                            if (lastTranscription.isNotEmpty() ||
                                transcriptionState is AudioTranscriptionHelper.TranscriptionState.Success
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val textToProcess =
                                            if (transcriptionState is AudioTranscriptionHelper.TranscriptionState.Success) {
                                                (transcriptionState as AudioTranscriptionHelper.TranscriptionState.Success).text
                                            } else {
                                                lastTranscription
                                            }
                                        processWithGemma(textToProcess)
                                    },
                                    enabled = audioGemmaHelper.isReady() && !isProcessingWithGemma
                                ) {
                                    if (isProcessingWithGemma) {
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
                            if (!audioGemmaHelper.isReady()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = gemmaStatus,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Gemma Transcription Card
                    if (gemmaTranscription.isNotEmpty() || gemmaResponse.isNotEmpty() || isProcessingWithGemma) {
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

                                if (isProcessingWithGemma) {
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

                                if (gemmaTranscription.isNotEmpty()) {
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
                                            text = gemmaTranscription,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (gemmaResponse.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Response:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = gemmaResponse,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Recordings List
                if (recordings.isNotEmpty()) {
                    Text(
                        text = "Recordings (${recordings.size})",
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
                        recordings.forEach { recording ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentlyPlaying == recording)
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
                                                onClick = { transcribeWithGemma(recording) },
                                                enabled = audioGemmaHelper.isReady() && !isProcessingWithGemma
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "Transcribe with Gemma",
                                                    tint = if (audioGemmaHelper.isReady())
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    if (currentlyPlaying == recording && isPlaying) {
                                                        stopPlayback()
                                                    } else {
                                                        playRecording(recording)
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (currentlyPlaying == recording && isPlaying)
                                                        Icons.Default.Pause
                                                    else
                                                        Icons.Default.PlayArrow,
                                                    contentDescription = if (currentlyPlaying == recording && isPlaying)
                                                        "Pause"
                                                    else
                                                        "Play"
                                                )
                                            }

                                            IconButton(
                                                onClick = { deleteRecording(recording) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    // The active playback item gets a progress bar for quick status feedback.
                                    if (currentlyPlaying == recording) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { playbackProgress },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (hasAudioPermission) {
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
                    if (!hasAudioPermission) return@FloatingActionButton
                    if (isRecording) {
                        stopRecording()
                    } else {
                        startRecording()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .alpha(if (hasAudioPermission) 1f else 0.5f),
                containerColor = when {
                    !hasAudioPermission -> MaterialTheme.colorScheme.surfaceVariant
                    isRecording -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            ) {
                Icon(
                    imageVector = when {
                        !hasAudioPermission -> Icons.Default.MicOff
                        isRecording -> Icons.Default.Stop
                        else -> Icons.Default.Mic
                    },
                    contentDescription = when {
                        !hasAudioPermission -> "Microphone Permission Required"
                        isRecording -> "Stop Recording"
                        else -> "Start Recording"
                    }
                )
            }
        }
    }
}
