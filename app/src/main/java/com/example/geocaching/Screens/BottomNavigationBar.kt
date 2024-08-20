package com.example.geocaching.Screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.geocaching.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Profile,
        BottomNavItem.Map,
        BottomNavItem.Table,
        BottomNavItem.Leaderboard
    )

    BottomNavigation(
        backgroundColor = Color.White
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry.value?.destination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(id = item.title)
                    )
                },
                label = { Text(text = stringResource(id = item.title), fontSize = 10.sp, color = Color.Black) },
                selected = currentRoute == item.route,
                selectedContentColor = Color.Green,
                unselectedContentColor = Color.Gray,
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { startDestination ->
                            popUpTo(startDestination) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

sealed class BottomNavItem(@StringRes val title: Int, val route: String, @DrawableRes val icon: Int) {
    object Profile : BottomNavItem(R.string.profile, "profile", R.drawable.ic_profile)
    object Map : BottomNavItem(R.string.map, "map", R.drawable.ic_map)
    object Table : BottomNavItem(R.string.table, "table", R.drawable.ic_table)
    object Leaderboard : BottomNavItem(R.string.leaderboard, "leaderboard", R.drawable.ic_leaderboard)
}