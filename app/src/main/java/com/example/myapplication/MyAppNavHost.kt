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