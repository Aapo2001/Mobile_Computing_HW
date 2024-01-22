package com.example.myapplication

import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(modifier: Modifier, onclick: () -> Unit){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            context.contentResolver.takePersistableUriPermission(uri, flag)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }
    Scaffold(
        topBar = {
            NavBar(onClick = onclick, modifier = modifier, destination = Home)
        }
    ){innerPadding ->  Surface(modifier = Modifier.padding(innerPadding)) {
        Text(text = "Profile")

        Row {
            TextButton(onClick = {

                coroutineScope.launch {
                    pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                }
            }) {
                Icon(
                    Icons.Filled.Add, null)
                Spacer(modifier.size(ButtonDefaults.IconSpacing))
                Text("Add photo")
            }
        }
    }
    }



}


