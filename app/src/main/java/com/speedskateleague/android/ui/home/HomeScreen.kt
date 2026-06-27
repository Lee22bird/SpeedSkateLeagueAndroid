package com.speedskateleague.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.speedskateleague.android.network.MeProfile
import com.speedskateleague.android.ui.nav.SslDestination
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

/** Android equivalent of HomeDashboard.swift's dashboard view. */
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.profile == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SslColors.Blue)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(SslSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(SslSpacing.md),
                ) {
                    item { GreetingCard(profile = state.profile, isOffline = state.isOffline) }
                    item { Text("TODAY", style = SslType.label) }
                    item { StatsGrid(profile = state.profile, navController = navController) }
                    item { Text("YOUR LEAGUE", style = SslType.label) }
                    item {
                        FocusRow(
                            title = "Notifications",
                            value = (state.profile?.stats?.unreadNotifications ?: 0).let {
                                if (it == 0) "All caught up" else "$it unread"
                            },
                            onClick = { navController.navigate(SslDestination.Notifications.route) },
                        )
                    }
                    item {
                        FocusRow(
                            title = "Announcements",
                            value = "View latest",
                            onClick = { navController.navigate(SslDestination.Announcements.route) },
                        )
                    }
                    item {
                        FocusRow(
                            title = "Upcoming Meets",
                            value = "${state.profile?.stats?.upcomingMeets ?: 0} scheduled",
                            onClick = { navController.navigate(SslDestination.UpcomingMeets.route) },
                        )
                    }
                    item {
                        FocusRow(
                            title = "Profile Summary",
                            value = state.profile?.team ?: "View profile",
                            onClick = { navController.navigate(SslDestination.Profile.route) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GreetingCard(profile: MeProfile?, isOffline: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .padding(SslSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(profile = profile, size = 56.dp)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.md))
            Column {
                Text(
                    profile?.fullName?.let { "Hi, ${it.split(" ").first()}" } ?: "Welcome",
                    style = SslType.title,
                )
                if (profile?.team != null || profile?.league != null) {
                    Text(
                        listOfNotNull(profile.team, profile.league).joinToString(" · "),
                        style = SslType.caption,
                    )
                }
            }
        }
        if (isOffline) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.sm))
            Text("Offline Mode — showing cached data", style = SslType.caption, color = SslColors.Orange)
        }
        if (!profile?.roles.isNullOrEmpty()) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.sm))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(SslSpacing.xs)) {
                items(profile!!.roles) { role ->
                    RoleBadge(role)
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(SslSpacing.xs))
            .background(SslColors.Blue.copy(alpha = 0.18f))
            .padding(horizontal = SslSpacing.sm, vertical = 4.dp),
    ) {
        Text(role.uppercase(), style = SslType.label, color = SslColors.Blue)
    }
}

@Composable
private fun Avatar(profile: MeProfile?, size: androidx.compose.ui.unit.Dp) {
    val avatarUrl = profile?.avatarUrl
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(SslColors.BlueDeep),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = profile?.fullName,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
            )
        } else {
            Text(
                profile?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("") ?: "?",
                style = SslType.headline,
            )
        }
    }
}

@Composable
private fun StatsGrid(profile: MeProfile?, navController: NavHostController) {
    val stats = profile?.stats
    Column(verticalArrangement = Arrangement.spacedBy(SslSpacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm)) {
            StatCard(
                label = "Notifications",
                value = "${stats?.unreadNotifications ?: 0}",
                color = SslColors.Orange,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(SslDestination.Notifications.route) },
            )
            StatCard(
                label = "Upcoming Meets",
                value = "${stats?.upcomingMeets ?: 0}",
                color = SslColors.Blue,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(SslDestination.UpcomingMeets.route) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm)) {
            StatCard(
                label = "Race Results",
                value = "${stats?.raceResults ?: 0}",
                color = Color.White,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(SslDestination.RaceResults.route) },
            )
            StatCard(
                label = "Time Trials",
                value = "${stats?.timeTrialResults ?: 0}",
                color = SslColors.Orange,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(SslDestination.RaceResults.route) },
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .sslGlassCard()
            .clickable { onClick() }
            .padding(SslSpacing.md),
    ) {
        Text(value, style = SslType.display.copy(fontSize = 26.sp, color = color))
        Text(label, style = SslType.caption)
    }
}

@Composable
private fun FocusRow(title: String, value: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .clickable { onClick() }
            .padding(SslSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = SslType.body)
        Text(value, style = SslType.caption)
    }
}
