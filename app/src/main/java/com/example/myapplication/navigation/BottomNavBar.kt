package com.example.myapplication.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun BottomNavBar(
    navController: NavController,
    currentDestination: NavDestination?,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        bottomNavDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == destination.route
            } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) },
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
