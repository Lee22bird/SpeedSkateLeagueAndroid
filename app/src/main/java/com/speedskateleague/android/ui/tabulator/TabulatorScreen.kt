package com.speedskateleague.android.ui.tabulator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.network.TabulatorAssignmentDto
import com.speedskateleague.android.network.SslDateParser
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabulatorScreen(viewModel: TabulatorViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.load() },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(SslSpacing.md),
                verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tabulator", style = SslType.display)
                        Text(
                            "Your upcoming meet assignments",
                            style = SslType.caption,
                            color = SslColors.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        StatChip(
                            value = "${state.assignments.size}",
                            label = if (state.assignments.size == 1) "Meet" else "Meets",
                        )
                    }
                }

                if (state.isLoading && state.assignments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = SslSpacing.xl),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = SslColors.Blue)
                        }
                    }
                } else if (state.errorMessage != null) {
                    item {
                        Text(
                            state.errorMessage!!,
                            style = SslType.body,
                            color = SslColors.Orange,
                            modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
                        )
                    }
                } else if (state.assignments.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("No upcoming assignments", style = SslType.headline)
                            Text(
                                "You don't have any meets assigned yet. Check back closer to the season.",
                                style = SslType.caption,
                                color = SslColors.TextSecondary,
                            )
                        }
                    }
                } else {
                    items(state.assignments, key = { it.meetId }) { assignment ->
                        AssignmentCard(assignment)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(value: String, label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SslColors.GlassFill)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = SslType.headline)
            Text(label, style = SslType.label)
        }
    }
}

@Composable
private fun AssignmentCard(assignment: TabulatorAssignmentDto) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val dateMillis = SslDateParser.parseMillis(assignment.meetDate)
        if (dateMillis != null) {
            val cal = java.util.Calendar.getInstance().also { it.timeInMillis = dateMillis }
            val monthAbbr = listOf("JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC")
                .getOrElse(cal.get(java.util.Calendar.MONTH)) { "" }
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SslColors.Blue.copy(alpha = 0.12f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(monthAbbr, style = SslType.label, color = SslColors.Blue)
                    Text("$day", style = SslType.headline)
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(assignment.meetTitle ?: "Meet", style = SslType.headline)

            val dateMillis2 = SslDateParser.parseMillis(assignment.meetDate)
            if (dateMillis2 != null) {
                val formatted = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(dateMillis2))
                Text(formatted, style = SslType.caption, color = SslColors.TextSecondary)
            }

            val roleColor = if (assignment.isDirectorRole) SslColors.Orange else SslColors.Blue
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(roleColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    assignment.roleLabel.uppercase(),
                    style = SslType.label,
                    color = roleColor,
                )
            }
        }
    }
}
