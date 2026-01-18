package com.example.myapplication

import SampleData
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.repository.UserProfileRepository
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}


@Composable
fun MyApp(){
    MyApplicationTheme {
        val navController = rememberNavController()
        val context = LocalContext.current
        val repository = UserProfileRepository(context)
        MyAppNavHost(
            navController = navController,
            modifier = Modifier,
            repository = repository
        )
    }
}

@Composable
fun HomeScreen(modifier: Modifier, onclick: () -> Unit){
    Scaffold(
        topBar = {
            NavBar(onClick = onclick, modifier = modifier, destination = Profile)
        }
    ){innerPadding ->  Surface(modifier = Modifier.padding(innerPadding), color = MaterialTheme.colorScheme.background) {
        Conversation(messages = SampleData.conversationSample)
    }}



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

