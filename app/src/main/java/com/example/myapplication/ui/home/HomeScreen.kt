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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.example.myapplication.helper.GemmaHelper
import com.example.myapplication.navigation.BottomNavBar
import com.example.myapplication.navigation.Home
import com.example.myapplication.navigation.NavBar
import com.example.myapplication.navigation.Profile
import com.example.myapplication.repository.MessageRepository
import com.example.myapplication.repository.UserProfileRepository
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onclick: () -> Unit,
    messageRepository: MessageRepository,
    userProfileRepository: UserProfileRepository,
    navController: NavController,
    currentDestination: NavDestination?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val dbMessages by messageRepository.getAllMessages().collectAsState(initial = emptyList())
    val userProfile by userProfileRepository.userProfile.collectAsState(initial = null)

    var newMessageText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var gemmaStatus by remember { mutableStateOf("Initializing Gemma...") }

    val gemmaHelper = remember { GemmaHelper(context) }

    LaunchedEffect(Unit) {
        val success = gemmaHelper.initialize()
        gemmaStatus = if (success) "Gemma ready" else (gemmaHelper.getError() ?: "Failed to initialize")
    }

    DisposableEffect(Unit) {
        onDispose { gemmaHelper.close() }
    }

    val username = userProfile?.username ?: "You"
    val userImagePath = userProfile?.imagePath

    val displayMessages = dbMessages.map {
        Message(
            author = it.author,
            body = it.body,
            imagePath = if (it.author == username || it.author == "You") userImagePath else null
        )
    }.ifEmpty { SampleData.conversationSample }

    fun sendMessage() {
        val messageText = newMessageText
        if (messageText.isBlank() || isGenerating) return
        newMessageText = ""

        coroutineScope.launch {
            messageRepository.insertMessage(username, messageText)

            if (gemmaHelper.isReady()) {
                isGenerating = true
                val response = gemmaHelper.generateResponse(messageText)
                messageRepository.insertMessage("Gemma", response)
                isGenerating = false
            }
        }
    }

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
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.weight(1f)
            ) {
                Conversation(messages = displayMessages, userImagePath = userImagePath)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isGenerating) "Generating response..." else gemmaStatus,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

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
                        value = newMessageText,
                        onValueChange = { newMessageText = it },
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
                        keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                        trailingIcon = {
                            IconButton(
                                onClick = { sendMessage() },
                                enabled = !isGenerating
                            ) {
                                if (isGenerating) {
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
