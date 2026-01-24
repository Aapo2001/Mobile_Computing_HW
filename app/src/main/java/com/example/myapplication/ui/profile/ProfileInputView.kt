package com.example.myapplication.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.repository.UserProfileRepository
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileInputView(
    onclick: () -> Unit,
    repository: UserProfileRepository,
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current

    val viewModel: ProfileEditViewModel = viewModel(
        factory = ProfileEditViewModel.provideFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.selectImage(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile Setup",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Image preview with M3 styling
            Surface(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 4.dp
            ) {
                when {
                    uiState.selectedImageUri != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uiState.selectedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    uiState.displayImagePath != null -> {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(uiState.displayImagePath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(36.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pick image button - M3 FilledTonalButton
            FilledTonalButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose Photo")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Username input with M3 styling
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save and navigate button - M3 Filled Button
            Button(
                onClick = {
                    viewModel.saveProfile { onclick() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.username.isNotBlank() && !uiState.isSaving,
                shape = MaterialTheme.shapes.large
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save & View Profile",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigate without saving - M3 Text Button
            TextButton(
                onClick = onclick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Skip for Now",
                    style = MaterialTheme.typography.labelLarge
                )
            }

        }
    }
}
