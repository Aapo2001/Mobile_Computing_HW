package com.example.myapplication


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.repository.UserProfileRepository
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInputView(
    onclick: () -> Unit,
    repository: UserProfileRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe current profile from database
    val currentProfile by repository.userProfile.collectAsState(initial = null)

    // Local state for editing
    var username by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var displayImagePath by remember { mutableStateOf<String?>(null) }

    // Update local state when profile loads from database
    LaunchedEffect(currentProfile) {
        currentProfile?.let { profile ->
            username = profile.username
            displayImagePath = profile.imagePath
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            displayImagePath = null // Clear old path to show new URI
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Profile Setup",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Image preview
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when {
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    displayImagePath != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(displayImagePath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Pick image button
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pick Image from Gallery")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Username input
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save and navigate button
            Button(
                onClick = {
                    scope.launch {
                        repository.updateProfile(username, selectedImageUri)
                        onclick()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = username.isNotBlank()
            ) {
                Text("Save & View Profile", style = MaterialTheme.typography.bodyLarge)
            }

            // Navigate without saving
            OutlinedButton(
                onClick = onclick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Profile Without Saving")
            }
        }
    }
}