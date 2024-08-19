package com.example.geocaching.Screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        composable(route = "login") { LoginScreen(navController) }
        composable(route = "register") { RegisterScreen(navController) }
        composable(route = "map") { MapScreen(navController) }
        composable(route = "profile") { ProfileScreen(navController) }
        composable(route = "leaderboard") { LeaderboardScreen(navController) }
        composable(route = "table") { ObjectsTableScreen(navController)}
        composable(route = "add_object") { AddObjectScreen(navController) }
    }
}