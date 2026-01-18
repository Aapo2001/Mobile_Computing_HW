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
            HomeScreen(modifier, onclick = {navController.navigateSingleTopTo(Profile.route, Home.route)}) }
        composable(Profile.route){
            ProfileScreen(modifier, onclick = {navController.navigateSingleTopTo(Home.route, Home.route)},  onNavigateToEdit = {
                navController.navigate(EditProfile.route) {
                    popUpTo(Profile.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },
               repository =  repository)
        }
        composable(EditProfile.route){
            ProfileInputView(
                onNavigateToDisplay = {
                    navController.navigate(Profile.route) {
                        popUpTo(Profile.route) {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                repository = repository
            )
        }
    }
}

fun NavHostController.navigateSingleTopTo(route: String, popUpToRoute: String) =
    this.navigate(route) {
        popUpTo(
            popUpToRoute
        ) {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }