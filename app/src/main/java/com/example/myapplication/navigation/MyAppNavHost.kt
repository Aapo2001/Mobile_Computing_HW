package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.ui.audio.AudioScreen
import com.example.myapplication.ui.camera.CameraScreen
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.map.MapScreen
import com.example.myapplication.ui.profile.ProfileInputView
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.sensor.SensorScreen
import com.example.myapplication.ui.video.VideoScreen
import com.example.myapplication.repository.MessageRepository
import com.example.myapplication.repository.UserProfileRepository


@Composable
fun MyAppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    repository: UserProfileRepository,
    messageRepository: MessageRepository,
    startRoute: String = Home.route,
    initialShakeCount: Int = 0
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier
    ) {
        composable(Home.route) {
            HomeScreen(
                onclick = { navController.navigateSingleTopTo(Profile.route) },
                messageRepository = messageRepository,
                userProfileRepository = repository,
                navController = navController,
                currentDestination = currentDestination
            )
        }
        composable(Profile.route) {
            ProfileScreen(
                onclick = { navController.navigateSingleTopTo(Home.route) },
                onNavigateToEdit = {
                    navController.navigateSingleTopTo(
                        EditProfile.route,
                        Profile.route
                    )
                },
                repository = repository,
                navController = navController,
                currentDestination = currentDestination
            )
        }
        composable(EditProfile.route) {
            ProfileInputView(
                onclick = { navController.navigateSingleTopTo(Profile.route, Profile.route) },
                repository = repository,
                navController = navController,
                currentDestination = currentDestination
            )
        }
        composable(SensorDest.route) {
            SensorScreen(
                initialShakeCount = initialShakeCount,
                navController = navController,
                currentDestination = currentDestination
            )
        }
        composable(MapDest.route) {
            MapScreen(
                navController = navController,
                currentDestination = currentDestination
            )
        }
        composable(VideoDest.route) {
            VideoScreen(
                navController = navController,
                currentDestination = currentDestination
            )
        }
        composable(CameraDest.route) {
            CameraScreen(
                navController = navController,
                currentDestination = currentDestination
            )
        }
        composable(AudioDest.route) {
            AudioScreen(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String, popUpToRoute: String? = null) =
    this.navigate(route) {
        popUpTo(
               popUpToRoute ?: Home.route
        ) {
            inclusive = false
        }
        launchSingleTop = true
    }
