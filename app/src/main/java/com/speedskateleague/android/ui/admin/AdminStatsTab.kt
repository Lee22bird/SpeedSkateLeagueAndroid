package com.speedskateleague.android.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.speedskateleague.android.network.AdminStatsDto
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
internal fun AdminStatsTab(state: AdminUiState) {
    val stats = state.stats
    if (state.isLoading && stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SslColors.Blue)
        }
        return
    }
    if (stats == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Stats are not available right now.", style = SslType.body)
        }
        return
    }

    val tiles = listOf(
        "Total Users" to stats.totalUsers,
        "Skaters" to stats.skaters,
        "Coaches" to stats.coaches,
        "League Directors" to stats.leagueDirectors,
        "Meet Directors" to stats.meetDirectors,
        "Tabulators" to stats.tabulators,
        "Referees" to stats.referees,
        "Announcers" to stats.announcers,
        "Pending Roles" to stats.pendingRoles,
        "Active Members" to stats.activeTeamMembers,
        "Child Skaters" to stats.childSkaters,
        "Account Users" to stats.accountUsers,
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = adminContentPadding,
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        items(tiles) { (label, value) -> StatCard(label, value) }
    }
}

@Composable
private fun StatCard(label: String, value: Int) {
    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        Text("$value", style = SslType.title, color = SslColors.Blue)
        Text(label, style = SslType.caption)
    }
}
