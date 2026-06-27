package com.speedskateleague.android.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.speedskateleague.android.network.MeProfile
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

/** Android equivalent of ProfileCard.swift's hero card + identity grid. */
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading && state.profile == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SslColors.Blue)
                }
            }
            state.profile == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage ?: "Unable to load profile.", style = SslType.body)
                }
            }
            else -> ProfileContent(profile = state.profile!!)
        }
    }
}

@Composable
private fun ProfileContent(profile: MeProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.md),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .sslGlassCard()
                .padding(SslSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape),
            ) {
                if (!profile.avatarUrl.isNullOrBlank()) {
                    AsyncImage(model = profile.avatarUrl, contentDescription = profile.fullName, modifier = Modifier.fillMaxSize())
                } else {
                    Box(modifier = Modifier.fillMaxSize().clip(CircleShape)) {
                        Text(
                            profile.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("") ?: "?",
                            style = SslType.title,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.sm))
            Text(profile.fullName ?: "Unknown Skater", style = SslType.title)
            profile.sslSkaterId?.let { Text("SSL ID: $it", style = SslType.caption) }
            if (profile.roles.isNotEmpty()) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.xs))
                Text(profile.roles.joinToString(" · ") { it.uppercase() }, style = SslType.label, color = SslColors.Blue)
            }
        }

        IdentityGrid(profile)

        QuickStats(profile)
    }
}

@Composable
private fun IdentityGrid(profile: MeProfile) {
    Column(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        Text("IDENTITY", style = SslType.label)
        IdentityRow("Team", profile.team ?: "—")
        IdentityRow("League", profile.league ?: "—")
        IdentityRow("Helmet Number", profile.helmetNumber ?: "—")
        IdentityRow("Sponsor", profile.sponsor ?: "—")
    }
}

@Composable
private fun IdentityRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = SslType.caption)
        Text(value, style = SslType.body)
    }
}

@Composable
private fun QuickStats(profile: MeProfile) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        QuickStat("Race Results", profile.stats.raceResults)
        QuickStat("Time Trials", profile.stats.timeTrialResults)
        QuickStat("Official", profile.stats.officialAssignments)
    }
}

@Composable
private fun QuickStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = SslType.title)
        Text(label, style = SslType.caption)
    }
}
