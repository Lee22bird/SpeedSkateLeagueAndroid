package com.speedskateleague.android.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType

internal val adminContentPadding = PaddingValues(horizontal = SslSpacing.md, vertical = SslSpacing.sm)

/** Native port of the Admin/Super Admin portal tab. Core tools only (stats, users, approvals, schedule). */
@Composable
fun AdminScreen(viewModel: AdminViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyRow(
                modifier = Modifier.padding(SslSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(SslSpacing.xs),
            ) {
                items(AdminTab.entries) { tab ->
                    AdminTabChip(
                        label = tabLabel(tab),
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                    )
                }
            }

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
                AdminTab.STATS -> AdminStatsTab(state = state)
                AdminTab.USERS -> AdminUsersTab(state = state, viewModel = viewModel)
                AdminTab.APPROVALS -> AdminApprovalsTab(state = state, viewModel = viewModel)
                AdminTab.SCHEDULE -> AdminScheduleTab(state = state, viewModel = viewModel)
            }
        }
    }
}

private fun tabLabel(tab: AdminTab) = when (tab) {
    AdminTab.STATS -> "Stats"
    AdminTab.USERS -> "Users"
    AdminTab.APPROVALS -> "Approvals"
    AdminTab.SCHEDULE -> "Schedule"
}

@Composable
private fun AdminTabChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
