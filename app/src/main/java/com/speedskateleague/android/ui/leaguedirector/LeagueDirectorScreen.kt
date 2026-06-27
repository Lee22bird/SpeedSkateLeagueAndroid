package com.speedskateleague.android.ui.leaguedirector

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.network.PendingPersonDto
import com.speedskateleague.android.ui.coach.CoachAvatar
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

/** Native port of the League Director server-rendered tab (no Swift equivalent existed). */
@Composable
fun LeagueDirectorScreen(viewModel: LeagueDirectorViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.stats == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SslColors.Blue)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(SslSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
                ) {
                    state.stats?.let { stats ->
                        item {
                            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                                Text(stats.displayName.ifBlank { stats.league }, style = SslType.title)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = SslSpacing.sm),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                ) {
                                    StatTile("Teams", stats.teams)
                                    StatTile("Skaters", stats.skaters)
                                    StatTile("Coaches", stats.coaches)
                                }
                            }
                        }
                    }

                    if (state.message != null) {
                        item { Text(state.message!!, style = SslType.caption, color = SslColors.Orange) }
                    }

                    item { Text("PENDING COACH REQUESTS", style = SslType.label) }

                    if (state.pendingCoaches.isEmpty()) {
                        item { Text("No pending coach requests for your league.", style = SslType.body) }
                    } else {
                        items(state.pendingCoaches, key = { it.id }) { person ->
                            PendingCoachCard(
                                person = person,
                                onApprove = { viewModel.approve(person) },
                                onDeny = { viewModel.deny(person) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = SslType.title)
        Text(label, style = SslType.caption)
    }
}

@Composable
private fun PendingCoachCard(person: PendingPersonDto, onApprove: () -> Unit, onDeny: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoachAvatar(person.resolvedAvatarUrl, person.fullName ?: "?")
        Column(modifier = Modifier.weight(1f)) {
            Text(person.fullName ?: "Unknown", style = SslType.body)
            Text(
                listOfNotNull(person.team, person.ageGroup).joinToString(" • "),
                style = SslType.caption,
            )
        }
        OutlinedButton(onClick = onDeny) { Text("Deny") }
        Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = SslColors.Blue)) {
            Text("Approve")
        }
    }
}
