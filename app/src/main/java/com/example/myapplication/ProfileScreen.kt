package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ProfileScreen(modifier: Modifier, onclick: () -> Unit, imageSaver: ImageSaver){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
    var imageUri by remember{ mutableStateOf<Uri?>(null)}

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            context.contentResolver.takePersistableUriPermission(uri, flag)
            imageUri = uri

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
                    imageUri?.let { imageSaver.saveFromUri(it) }

                }
            }) {
                Icon(
                    Icons.Filled.Add, null)
                Spacer(modifier.size(ButtonDefaults.IconSpacing))
                Text("Add photo")
            }

            AsyncImage(modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(8.dp)), model = imageUri, contentDescription = null)
        }
    }
    }



}


