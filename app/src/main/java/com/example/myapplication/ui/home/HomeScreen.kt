package com.example.myapplication.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.Home
import com.example.myapplication.navigation.NavBar
import com.example.myapplication.navigation.Profile
import com.example.myapplication.repository.MessageRepository
import com.example.myapplication.repository.UserProfileRepository

@Composable
fun HomeScreen(
    onclick: () -> Unit,
    messageRepository: MessageRepository,
    userProfileRepository: UserProfileRepository,
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current

    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(
            messageRepository = messageRepository,
            userProfileRepository = userProfileRepository,
            context = context
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    // Convert to display messages with sample data fallback
    val displayMessages = uiState.messages.ifEmpty { SampleData.conversationSample }
    val userImagePath = uiState.userProfile?.imagePath

    Scaffold(
        topBar = {
            NavBar(
                title = Home.label,
                navigationIcon = Profile.icon,
                navigationIconDescription = "Go to Profile",
                onClick = onclick
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Conversation takes available space
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.weight(1f)
            ) {
                Conversation(messages = displayMessages, userImagePath = userImagePath)
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Gemma status indicator
            Text(
                text = if (uiState.isGenerating) "Generating response..." else uiState.gemmaStatus,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Message input row at bottom - M3 styled
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = uiState.newMessageText,
                        onValueChange = { viewModel.updateNewMessageText(it) },
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                        placeholder = { Text("Type a message...") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { viewModel.sendMessage() }),
                        trailingIcon = {
                            IconButton(
                                onClick = { viewModel.sendMessage() },
                                enabled = !uiState.isGenerating
                            ) {
                                if (uiState.isGenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
