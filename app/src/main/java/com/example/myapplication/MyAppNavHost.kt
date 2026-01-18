package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.repository.UserProfileRepository


@Composable
fun MyAppNavHost(    navController: NavHostController,
                     modifier: Modifier = Modifier,
                     repository: UserProfileRepository
){
    NavHost(
        navController = navController,
        startDestination = Home.route,
        modifier = modifier
    ) {
        composable(Home.route) {
            HomeScreen(modifier, onclick = {navController.navigateSingleTopTo(Profile.route)}) }
        composable(Profile.route){
            ProfileScreen(modifier, onclick = {navController.navigateSingleTopTo(Home.route)},  onNavigateToEdit = {
                navController.navigate(EditProfile.route) {
                    popUpTo(Profile.route) {
                        inclusive = false
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },repository)
        }
        composable(EditProfile.route){
            ProfileInputView(
                onNavigateToDisplay = {
                    navController.navigate(Profile.route) {
                        popUpTo(Profile.route) {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                repository = repository
            )
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }