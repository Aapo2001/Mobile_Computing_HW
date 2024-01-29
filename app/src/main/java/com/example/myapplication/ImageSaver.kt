package com.example.myapplication

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ImageSaver(context: Context, private val contentResolver: ContentResolver) {
    private var _image: File? = null
    val imageName = "profilePicture.jpg"
    fun getImage() = _image

    val imageFolder = File(context.filesDir, "images").also { it.mkdir() }


    private fun generateImageFile() = File(imageFolder, imageName)

    suspend fun saveFromUri(uri: Uri) {
        withContext(Dispatchers.IO) {
            contentResolver.openInputStream(uri)?.use { input ->
                val imageFile = generateImageFile()

                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                    _image = imageFile
                }
            }
        }
    }
}