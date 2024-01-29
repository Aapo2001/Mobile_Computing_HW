package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable



@Composable
fun MyAppNavHost(    navController: NavHostController,
                     modifier: Modifier = Modifier,
                     imageSaver: ImageSaver
){
    NavHost(
        navController = navController,
        startDestination = Home.route,
        modifier = modifier
    ) {
        composable(Home.route) {
            HomeScreen(modifier, onclick = {navController.navigateSingleTopTo(Profile.route)}) }
        composable(Profile.route){
            ProfileScreen(modifier, onclick = {navController.navigateSingleTopTo(Home.route)}, imageSaver)
        }

    }
}

fun NavHostController.navigateSingleTopTo(route: String) =
    this.navigate(route) {
        popUpTo(
            this@navigateSingleTopTo.graph.findStartDestination().id
        ) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }