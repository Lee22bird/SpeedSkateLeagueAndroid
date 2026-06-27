package com.speedskateleague.android.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.network.SslRaceResultMeet
import com.speedskateleague.android.network.SslTimeTrialGroup
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.dateLabel
import com.speedskateleague.android.ui.theme.sslGlassCard

/** Android equivalent of SSLRaceResultsView in SSLRaceResults.swift. */
@Composable
fun RaceResultsScreen(viewModel: RaceResultsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.payload == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SslColors.Blue)
                }
            }
            state.payload == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage ?: "No results yet.", style = SslType.body)
                }
            }
            else -> {
                val payload = state.payload!!
                LazyColumn(
                    contentPadding = PaddingValues(SslSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(SslSpacing.md),
                ) {
                    item { SummaryRow(payload.summary.wins, payload.summary.podiums, payload.summary.competitions) }
                    if (payload.meets.isNotEmpty()) {
                        item { Text("MEETS", style = SslType.label) }
                        items(payload.meets) { meet -> MeetResultCard(meet) }
                    }
                    if (payload.timeTrials.isNotEmpty()) {
                        item { Text("TIME TRIALS", style = SslType.label) }
                        items(payload.timeTrials) { group -> TimeTrialCard(group) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(wins: Int, podiums: Int, meets: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SummaryTile("Wins", wins)
        SummaryTile("Podiums", podiums)
        SummaryTile("Meets", meets)
    }
}

@Composable
private fun SummaryTile(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = SslType.title)
        Text(label, style = SslType.caption)
    }
}

@Composable
private fun MeetResultCard(meet: SslRaceResultMeet) {
    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        Text(meet.meetName, style = SslType.body)
        Text(
            listOfNotNull(dateLabel(meet.meetDateMillis), meet.league, meet.location).joinToString(" · "),
            style = SslType.caption,
        )
        meet.lines.forEach { line ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = SslSpacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(line.label, style = SslType.caption)
                Text(line.placeLabel, style = SslType.caption, color = SslColors.Orange)
            }
        }
    }
}

@Composable
private fun TimeTrialCard(group: SslTimeTrialGroup) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(group.distance, style = SslType.body)
        Column(horizontalAlignment = Alignment.End) {
            Text("PB: ${group.personalBestSeconds?.let { "%.2fs".format(it) } ?: "—"}", style = SslType.caption)
            Text("Season: ${group.seasonBestSeconds?.let { "%.2fs".format(it) } ?: "—"}", style = SslType.caption)
        }
    }
}
