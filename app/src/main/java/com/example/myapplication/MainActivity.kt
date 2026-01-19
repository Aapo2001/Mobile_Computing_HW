package com.example.myapplication

import SampleData
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.repository.MessageRepository
import com.example.myapplication.repository.UserProfileRepository
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Optional: Keep splash screen on while loading
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        // Handle navigation from notification tap
        val navigateTo = intent?.getStringExtra("navigate_to")
        val shakeCount = intent?.getIntExtra("shake_count", 0) ?: 0

        setContent {

            keepSplashOnScreen = false

            MyApp(
                startRoute = if (navigateTo == "sensor") SensorDest.route else Home.route,
                initialShakeCount = shakeCount
            )
        }
    }
}


@Composable
fun MyApp(
    startRoute: String = Home.route,
    initialShakeCount: Int = 0
){
    MyApplicationTheme {
        val navController = rememberNavController()
        val context = LocalContext.current
        val repository = UserProfileRepository(context)
        val messageRepository = MessageRepository(context)
        MyAppNavHost(
            navController = navController,
            modifier = Modifier,
            repository = repository,
            messageRepository = messageRepository,
            startRoute = startRoute,
            initialShakeCount = initialShakeCount
        )
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier,
    onclick: () -> Unit,
    onNavigateToSensor: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToVideo: () -> Unit = {},
    onNavigateToCamera: () -> Unit = {},
    onNavigateToAudio: () -> Unit = {},
    messageRepository: MessageRepository
){
    val scope = rememberCoroutineScope()
    val messages by messageRepository.getAllMessages().collectAsState(initial = emptyList())
    var newMessageText by remember { mutableStateOf("") }

    // Convert database messages to UI messages
    val uiMessages = messages.map { Message(it.author, it.body) }
    // Combine with sample messages if database is empty
    val displayMessages = uiMessages.ifEmpty { SampleData.conversationSample }

    Scaffold(
        topBar = {
            NavBar(onClick = onclick, modifier = modifier, destination = Profile)
        }
    ){innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Feature buttons row (scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onNavigateToSensor) {
                    Icon(imageVector = Icons.Default.Sensors, contentDescription = "Sensors")
                    Text(" Sensors", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(onClick = onNavigateToMap) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = "Map")
                    Text(" Map", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(onClick = onNavigateToVideo) {
                    Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = "Video")
                    Text(" Video", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(onClick = onNavigateToCamera) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera")
                    Text(" Camera", modifier = Modifier.padding(start = 4.dp))
                }
                OutlinedButton(onClick = onNavigateToAudio) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Audio")
                    Text(" Audio", modifier = Modifier.padding(start = 4.dp))
                }
            }

            // Message input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMessageText,
                    onValueChange = { newMessageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newMessageText.isNotBlank()) {
                            scope.launch {
                                messageRepository.insertMessage("You", newMessageText)
                                newMessageText = ""
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.weight(1f)) {
                Conversation(messages = displayMessages)
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode"
)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Conversation(SampleData.conversationSample)
        }
    }
}

