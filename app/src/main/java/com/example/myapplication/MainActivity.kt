package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.navigation.Home
import com.example.myapplication.navigation.MyAppNavHost
import com.example.myapplication.navigation.SensorDest
import com.example.myapplication.repository.MessageRepository
import com.example.myapplication.repository.UserProfileRepository
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = getActionBar()
        actionBar!!.hide()

        val navigateTo = intent?.getStringExtra("navigate_to")
        val shakeCount = intent?.getIntExtra("shake_count", 0) ?: 0

        setContent {
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
