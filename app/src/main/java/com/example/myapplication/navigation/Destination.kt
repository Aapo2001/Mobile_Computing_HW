package com.example.myapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
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

// List of bottom navigation destinations
val bottomNavDestinations = listOf(
    Home,
    SensorDest
)
