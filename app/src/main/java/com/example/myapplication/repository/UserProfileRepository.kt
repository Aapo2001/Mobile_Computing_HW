package com.example.myapplication.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.UserProfile
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

class UserProfileRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userProfileDao = database.userProfileDao()

    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    suspend fun updateProfile(username: String, imageUri: Uri?) {
        val imagePath = imageUri?.let { saveImageToInternalStorage(it) }
            ?: userProfileDao.getUserProfileOnce()?.imagePath
            ?: ""

        val profile = UserProfile(
            id = 1,
            username = username,
            imagePath = imagePath
        )
        userProfileDao.insertOrUpdateProfile(profile)
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "profile_picture.jpg")

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }

    fun getImageFile(): File? {
        val file = File(context.filesDir, "profile_picture.jpg")
        return if (file.exists()) file else null
    }
}