package com.speedskateleague.android.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.speedskateleague.android.ui.home.HomeScreen
import com.speedskateleague.android.ui.profile.ProfileScreen
import com.speedskateleague.android.ui.settings.SettingsScreen
import com.speedskateleague.android.ui.theme.SslColors

sealed class SslDestination(val route: String, val label: String) {
    data object Home : SslDestination("home", "Home")
    data object Profile : SslDestination("profile", "Profile")
    data object Settings : SslDestination("settings", "Settings")
}

private val tabs = listOf(SslDestination.Home, SslDestination.Profile, SslDestination.Settings)

/** Android equivalent of LoggedInShell's tab/side-menu routing in ContentView.swift. */
@Composable
fun SslNavHost(onSignOut: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        containerColor = SslColors.Navy,
        bottomBar = {
            NavigationBar(containerColor = SslColors.Panel) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            val icon = when (tab) {
                                SslDestination.Home -> Icons.Filled.Home
                                SslDestination.Profile -> Icons.Filled.Person
                                SslDestination.Settings -> Icons.Filled.Settings
                            }
                            Icon(icon, contentDescription = tab.label)
                        },
                        label = { Text(tab.label) },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = SslColors.Blue,
                            selectedTextColor = SslColors.Blue,
                            unselectedIconColor = SslColors.TextTertiary,
                            unselectedTextColor = SslColors.TextTertiary,
                            indicatorColor = SslColors.GlassFill,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SslDestination.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
        ) {
            composable(SslDestination.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(SslDestination.Profile.route) {
                ProfileScreen()
            }
            composable(SslDestination.Settings.route) {
                SettingsScreen(onSignOut = onSignOut)
            }
        }
    }
}
