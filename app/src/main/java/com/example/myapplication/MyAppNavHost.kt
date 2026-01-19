package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
){
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier
    ) {
        composable(Home.route) {
            HomeScreen(
                modifier,
                onclick = {navController.navigateSingleTopTo(Profile.route)},
                onNavigateToSensor = {navController.navigateSingleTopTo(SensorDest.route)},
                onNavigateToMap = {navController.navigateSingleTopTo(MapDest.route)},
                onNavigateToVideo = {navController.navigateSingleTopTo(VideoDest.route)},
                onNavigateToCamera = {navController.navigateSingleTopTo(CameraDest.route)},
                onNavigateToAudio = {navController.navigateSingleTopTo(AudioDest.route)},
                messageRepository = messageRepository
            )
        }
        composable(Profile.route){
            ProfileScreen(modifier, onclick = {navController.navigateSingleTopTo(Home.route)},  onNavigateToEdit = {
                navController.navigateSingleTopTo(EditProfile.route, Profile.route)
            },repository)
        }
        composable(EditProfile.route){
            ProfileInputView(
                onclick = {
                    navController.navigateSingleTopTo(Profile.route, Profile.route)
                },
                repository = repository
            )
        }
        composable(SensorDest.route){
            SensorScreen(
                modifier,
                onclick = {navController.navigateSingleTopTo(Home.route)},
                initialShakeCount = initialShakeCount)
        }
        composable(MapDest.route){
            MapScreen(
                modifier,
                onclick = {navController.navigateSingleTopTo(Home.route)}
            )
        }
        composable(VideoDest.route){
            VideoScreen(
                modifier,
                onclick = {navController.navigateSingleTopTo(Home.route)}
            )
        }
        composable(CameraDest.route){
            CameraScreen(
                modifier,
                onclick = {navController.navigateSingleTopTo(Home.route)}
            )
        }
        composable(AudioDest.route){
            AudioScreen(
                modifier,
                onclick = {navController.navigateSingleTopTo(Home.route)}
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