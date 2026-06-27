package com.speedskateleague.android.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType

/** Android equivalent of CoachToolsView in CoachTools.swift. */
@Composable
fun CoachToolsScreen(viewModel: CoachToolsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(SslSpacing.md)) {
                Text(state.team.ifBlank { "Coach Tools" }, style = SslType.title)
                if (state.league.isNotBlank()) {
                    Text(state.league, style = SslType.caption)
                }
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = SslSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(SslSpacing.xs),
            ) {
                items(CoachTab.entries) { tab ->
                    CoachTabChip(
                        label = tabLabel(tab),
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.sm))

            if (state.message != null) {
                Text(
                    state.message!!,
                    style = SslType.caption,
                    color = SslColors.Orange,
                    modifier = Modifier.padding(horizontal = SslSpacing.md),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.sm))
            }

            when (state.selectedTab) {
                CoachTab.ANNOUNCEMENTS -> CoachAnnouncementsTab(state = state, viewModel = viewModel)
                CoachTab.PRACTICES -> CoachPracticesTab(state = state, viewModel = viewModel)
                CoachTab.ROSTER -> CoachRosterTab(state = state)
                CoachTab.DUES -> CoachDuesTab(state = state, viewModel = viewModel)
                CoachTab.REQUESTS -> CoachRequestsTab(state = state, viewModel = viewModel)
            }
        }
    }
}

private fun tabLabel(tab: CoachTab) = when (tab) {
    CoachTab.ANNOUNCEMENTS -> "Announcements"
    CoachTab.PRACTICES -> "Practices"
    CoachTab.ROSTER -> "Roster"
    CoachTab.DUES -> "Monthly Dues"
    CoachTab.REQUESTS -> "Requests"
}

@Composable
private fun CoachTabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(RoundedCornerShape(SslSpacing.sm))
            .background(if (selected) SslColors.Blue else SslColors.GlassFill)
            .clickable { onClick() }
            .padding(horizontal = SslSpacing.md, vertical = SslSpacing.sm),
    ) {
        Text(label, style = SslType.caption, color = if (selected) Color.White else SslColors.TextSecondary)
    }
}

internal val tabContentPadding = PaddingValues(horizontal = SslSpacing.md, vertical = SslSpacing.sm)
