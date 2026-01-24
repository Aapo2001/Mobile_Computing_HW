package com.example.myapplication.ui.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import com.example.myapplication.navigation.NavBar
import com.example.myapplication.navigation.Profile
import com.example.myapplication.repository.UserProfileRepository
import java.io.File

@Composable
fun ProfileScreen(
    onclick: () -> Unit,
    onNavigateToEdit: () -> Unit,
    repository: UserProfileRepository,
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.provideFactory(repository)
    )

    val uiState by viewModel.uiState.collectAsState()
    val userProfile = uiState.userProfile

    Scaffold(
        topBar = {
            NavBar(
                title = Profile.label,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationIconDescription = "Go back",
                onClick = onclick
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
            Spacer(modifier = Modifier.height(32.dp))

            // Profile header
            Text(
                text = "Your Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile image with M3 styling
            Surface(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                color = MaterialTheme.colorScheme.secondaryContainer,
                tonalElevation = 4.dp
            ) {
                if (userProfile?.imagePath?.isNotEmpty() == true) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(userProfile.imagePath))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Default avatar",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(40.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Username display with M3 Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Username",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = userProfile?.username ?: "No username set",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // M3 FilledTonalButton for edit action
            FilledTonalButton(
                onClick = onNavigateToEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.labelLarge
                )
            }

        }
    }
}
