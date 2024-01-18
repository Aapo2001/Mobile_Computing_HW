package com.example.myapplication

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(modifier: Modifier, onclick: () -> Unit){
    Scaffold(
        topBar = {
            NavBar(onClick = onclick, modifier = modifier, destination = Home)
        }
    ){innerPadding ->  Surface(modifier = Modifier.padding(innerPadding)) {
        Text(text = "Profile")

    }
    }



}