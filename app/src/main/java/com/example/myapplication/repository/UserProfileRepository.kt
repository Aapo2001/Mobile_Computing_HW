package com.example.myapplication.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.UserProfile
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

/**
 * Repository responsible for user profile persistence.
 *
 * It coordinates two storage concerns:
 *
 * - structured profile data in Room
 * - the profile picture file in app-private storage
 */
class UserProfileRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val userProfileDao = database.userProfileDao()

    /** Reactive stream of the currently saved profile row. */
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()

    /**
     * Updates the stored profile.
     *
     * When a new [imageUri] is supplied, the image is copied into app-private storage and the
     * stored path is updated. If no image is supplied, the previous image path is retained.
     */
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

    /**
     * Copies a user-selected image into internal storage so the app keeps access to it even if the
     * original picker URI becomes unavailable.
     */
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

    /** Returns the saved profile image file when it exists on disk. */
    fun getImageFile(): File? {
        val file = File(context.filesDir, "profile_picture.jpg")
        return if (file.exists()) file else null
    }
}
