package com.example.myapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Minimal contract implemented by every navigation destination used by the app shell.
 *
 * Each destination exposes:
 *
 * - an icon for the navigation UI
 * - a stable route string used by Navigation Compose
 * - a human-readable label for app bars and nav items
 */
interface Destination {
    val icon: ImageVector
    val route: String
    val label: String
}

/** Profile display screen destination. */
object Profile : Destination {
    override val icon = Icons.Default.Person
    override val route = "profile"
    override val label = "Profile"
}

/** Profile editing screen destination. */
object EditProfile : Destination {
    override val icon = Icons.Default.Edit
    override val route = "edit_profile"
    override val label = "Edit"
}

/** Chat / home screen destination. */
object Home : Destination {
    override val icon = Icons.AutoMirrored.Filled.Chat
    override val route = "home"
    override val label = "Chat"
}

/** Sensor and notifications demo destination. */
object SensorDest : Destination {
    override val icon = Icons.Default.Sensors
    override val route = "sensor"
    override val label = "Sensors"
}

/** Map and location demo destination. */
object MapDest : Destination {
    override val icon = Icons.Default.Map
    override val route = "map"
    override val label = "Map"
}

/** Video playback screen destination. */
object VideoDest : Destination {
    override val icon = Icons.Default.VideoLibrary
    override val route = "video"
    override val label = "Video"
}

/** Camera capture screen destination. */
object CameraDest : Destination {
    override val icon = Icons.Default.CameraAlt
    override val route = "camera"
    override val label = "Camera"
}

/** Audio recording and transcription destination. */
object AudioDest : Destination {
    override val icon = Icons.Default.Mic
    override val route = "audio"
    override val label = "Audio"
}

/** Ordered list of destinations rendered in the bottom navigation bar. */
val bottomNavDestinations = listOf(
    Home,
    SensorDest,
    MapDest,
    VideoDest,
    CameraDest,
    AudioDest
)
