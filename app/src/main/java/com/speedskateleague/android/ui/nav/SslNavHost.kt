@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.speedskateleague.android.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.speedskateleague.android.ui.admin.AdminScreen
import com.speedskateleague.android.ui.announcements.AnnouncementsScreen
import com.speedskateleague.android.ui.coach.CoachToolsScreen
import com.speedskateleague.android.ui.home.HomeScreen
import com.speedskateleague.android.ui.leaguedirector.LeagueDirectorScreen
import com.speedskateleague.android.ui.meetdirector.MeetDirectorScreen
import com.speedskateleague.android.ui.tabulator.TabulatorScreen
import com.speedskateleague.android.ui.teamhub.TeamHubScreen
import com.speedskateleague.android.ui.meets.DiscoverMeetsScreen
import com.speedskateleague.android.ui.meets.UpcomingMeetsScreen
import com.speedskateleague.android.ui.notifications.NotificationsScreen
import com.speedskateleague.android.ui.profile.ProfileScreen
import com.speedskateleague.android.ui.results.RaceResultsScreen
import com.speedskateleague.android.ui.settings.SettingsScreen
import com.speedskateleague.android.ui.theme.SslColors

sealed class SslDestination(val route: String, val label: String) {
    data object Home : SslDestination("home", "Home")
    data object Profile : SslDestination("profile", "Profile")
    data object Settings : SslDestination("settings", "Settings")
    data object Notifications : SslDestination("notifications", "Notifications")
    data object Announcements : SslDestination("announcements", "Announcements")
    data object UpcomingMeets : SslDestination("upcoming_meets", "Upcoming Meets")
    data object DiscoverMeets : SslDestination("discover_meets", "Discover Meets")
    data object RaceResults : SslDestination("race_results", "Race Results")
    data object CoachTools : SslDestination("coach_tools", "Coach Tools")
    data object LeagueDirector : SslDestination("league_director", "League Director")
    data object Tabulator : SslDestination("tabulator", "Tabulator")
    data object TeamHub : SslDestination("team_hub", "Team Hub")
    data object MeetDirector : SslDestination("meet_director", "Meet Director")
    data object Admin : SslDestination("admin", "Admin")
    // Skater community feed retired (July 2026) — removed to match iOS.
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
                                else -> Icons.Filled.Home
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
            composable(SslDestination.Notifications.route) {
                DetailScaffold(title = SslDestination.Notifications.label, navController = navController) {
                    NotificationsScreen()
                }
            }
            composable(SslDestination.Announcements.route) {
                DetailScaffold(title = SslDestination.Announcements.label, navController = navController) {
                    AnnouncementsScreen()
                }
            }
            composable(SslDestination.UpcomingMeets.route) {
                DetailScaffold(title = SslDestination.UpcomingMeets.label, navController = navController) {
                    UpcomingMeetsScreen()
                }
            }
            composable(SslDestination.DiscoverMeets.route) {
                DetailScaffold(title = SslDestination.DiscoverMeets.label, navController = navController) {
                    DiscoverMeetsScreen()
                }
            }
            composable(SslDestination.RaceResults.route) {
                DetailScaffold(title = SslDestination.RaceResults.label, navController = navController) {
                    RaceResultsScreen()
                }
            }
            composable(SslDestination.CoachTools.route) {
                DetailScaffold(title = SslDestination.CoachTools.label, navController = navController) {
                    CoachToolsScreen()
                }
            }
            composable(SslDestination.LeagueDirector.route) {
                DetailScaffold(title = SslDestination.LeagueDirector.label, navController = navController) {
                    LeagueDirectorScreen()
                }
            }
            composable(SslDestination.Tabulator.route) {
                DetailScaffold(title = SslDestination.Tabulator.label, navController = navController) {
                    TabulatorScreen()
                }
            }
            composable(SslDestination.TeamHub.route) {
                DetailScaffold(title = SslDestination.TeamHub.label, navController = navController) {
                    TeamHubScreen()
                }
            }
            composable(SslDestination.MeetDirector.route) {
                DetailScaffold(title = SslDestination.MeetDirector.label, navController = navController) {
                    MeetDirectorScreen()
                }
            }
            composable(SslDestination.Admin.route) {
                DetailScaffold(title = SslDestination.Admin.label, navController = navController) {
                    AdminScreen()
                }
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun DetailScaffold(
    title: String,
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    Scaffold(
        containerColor = SslColors.Navy,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SslColors.Navy,
                    titleContentColor = SslColors.TextPrimary,
                    navigationIconContentColor = SslColors.TextPrimary,
                ),
            )
        },
    ) { padding ->
        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.padding(padding)) {
            content()
        }
    }
}
