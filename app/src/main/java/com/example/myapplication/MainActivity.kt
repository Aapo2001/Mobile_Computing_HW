package com.example.myapplication

import SampleData
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.Home
import com.example.myapplication.navigation.MyAppNavHost
import com.example.myapplication.navigation.SensorDest
import com.example.myapplication.repository.MessageRepository
import com.example.myapplication.repository.UserProfileRepository
import com.example.myapplication.ui.home.Conversation
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Single-activity entry point for the application.
 *
 * The activity is intentionally lightweight:
 *
 * - installs the splash screen
 * - reads notification deep-link extras
 * - creates the top-level Compose content
 * - hands repository creation and initial navigation state to [MyApp]
 *
 * All feature-specific behavior is delegated to composable screens and helper classes.
 */
class MainActivity : ComponentActivity() {
    /**
     * Reads any deep-link extras from a sensor notification and forwards the resolved
     * start destination into the Compose navigation graph.
     */
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

/**
 * Root composable for the app shell.
 *
 * This function applies the app theme, creates the shared navigation controller, constructs the
 * repositories that are passed into screens, and hosts the navigation graph.
 *
 * @param startRoute initial route shown by the navigation graph. This defaults to the chat screen
 * but can be overridden when the app is opened from a notification.
 * @param initialShakeCount optional shake count injected from the notification flow so the sensor
 * screen can restore the last count immediately.
 */
@Composable
fun MyApp(
    startRoute: String = Home.route,
    initialShakeCount: Int = 0,
) {
    MyApplicationTheme {
        val navController = rememberNavController()
        val context = LocalContext.current
        val repository = UserProfileRepository(context)
        val messageRepository = MessageRepository(context)

        MyAppNavHost(
            navController = navController,
            repository = repository,
            messageRepository = messageRepository,
            startRoute = startRoute,
            initialShakeCount = initialShakeCount
        )
    }
}

/**
 * Preview used for validating the conversation UI in isolation from runtime dependencies.
 */
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
            color = MaterialTheme.colorScheme.background
        ) {
            Conversation(SampleData.conversationSample)
        }
    }
}
