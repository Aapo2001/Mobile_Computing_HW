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

interface Destination {
    val icon: ImageVector
    val route: String
    val label: String
}

object Profile : Destination {
    override val icon = Icons.Default.Person
    override val route = "profile"
    override val label = "Profile"
}

object EditProfile : Destination {
    override val icon = Icons.Default.Edit
    override val route = "edit_profile"
    override val label = "Edit"
}

object Home : Destination {
    override val icon = Icons.AutoMirrored.Filled.Chat
    override val route = "home"
    override val label = "Chat"
}

object SensorDest : Destination {
    override val icon = Icons.Default.Sensors
    override val route = "sensor"
    override val label = "Sensors"
}

object MapDest : Destination {
    override val icon = Icons.Default.Map
    override val route = "map"
    override val label = "Map"
}

object VideoDest : Destination {
    override val icon = Icons.Default.VideoLibrary
    override val route = "video"
    override val label = "Video"
}

object CameraDest : Destination {
    override val icon = Icons.Default.CameraAlt
    override val route = "camera"
    override val label = "Camera"
}

object AudioDest : Destination {
    override val icon = Icons.Default.Mic
    override val route = "audio"
    override val label = "Audio"
}

// List of bottom navigation destinations
val bottomNavDestinations = listOf(
    Home,
    SensorDest,
    MapDest,
    VideoDest,
    CameraDest,
    AudioDest
)