package com.speedskateleague.android.ui.teamhub

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.network.AnnouncementDto
import com.speedskateleague.android.network.CoachPracticeDto
import com.speedskateleague.android.network.MeetDto
import com.speedskateleague.android.network.SslDateParser
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.relativeTimeLabel
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
fun TeamHubScreen(viewModel: TeamHubViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SslBackground(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SslColors.Blue)
                }
            }
            state.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage!!, style = SslType.body, color = SslColors.Orange)
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(SslSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
                ) {
                    if (state.teamName != null || state.league != null) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                                state.teamName?.let { Text(it, style = SslType.title) }
                                state.league?.let { Text(it, style = SslType.caption) }
                            }
                        }
                    }

                    item { Text("PRACTICE CALENDAR", style = SslType.label) }

                    if (state.practices.isEmpty()) {
                        item { Text("No upcoming practices scheduled.", style = SslType.body) }
                    } else {
                        items(state.practices, key = { it.id }) { practice ->
                            PracticeCard(practice)
                        }
                    }

                    item { Text("COACH ANNOUNCEMENTS", style = SslType.label) }

                    if (state.coachAnnouncements.isEmpty()) {
                        item { Text("No announcements.", style = SslType.body) }
                    } else {
                        items(state.coachAnnouncements, key = { it.id ?: it.title.orEmpty() }) { ann ->
                            AnnouncementCard(ann)
                        }
                    }

                    item { Text("LEAGUE ANNOUNCEMENTS", style = SslType.label) }

                    if (state.leagueAnnouncements.isEmpty()) {
                        item { Text("No league announcements.", style = SslType.body) }
                    } else {
                        items(state.leagueAnnouncements, key = { it.id ?: it.title.orEmpty() }) { ann ->
                            AnnouncementCard(ann)
                        }
                    }

                    item { Text("NEXT MEETS", style = SslType.label) }

                    if (state.nextMeets.isEmpty()) {
                        item { Text("No upcoming meets.", style = SslType.body) }
                    } else {
                        items(state.nextMeets, key = { it.identity }) { meet ->
                            MeetCard(meet, onClick = {
                                val url = meet.ssmUrl ?: meet.registrationUrl
                                if (!url.isNullOrBlank()) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeCard(practice: CoachPracticeDto) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DateBadge(practice.practiceDate)
        Column(modifier = Modifier.weight(1f)) {
            Text(practice.title ?: "Practice", style = SslType.body)
            val time = practice.startTime ?: practice.practiceTime
            if (!time.isNullOrBlank()) Text(time, style = SslType.caption)
            if (!practice.location.isNullOrBlank()) Text(practice.location, style = SslType.caption)
        }
    }
}

@Composable
private fun DateBadge(dateStr: String?) {
    if (dateStr == null) return
    val parts = dateStr.split("-")
    val month = if (parts.size >= 2) monthAbbr(parts[1].toIntOrNull() ?: 0) else ""
    val day = parts.getOrNull(2) ?: ""
    Column(
        modifier = Modifier.size(44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(month, style = SslType.label, color = SslColors.Blue)
        Text(day, style = SslType.headline)
    }
}

private fun monthAbbr(m: Int) = listOf("", "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC").getOrElse(m) { "" }

@Composable
private fun AnnouncementCard(ann: AnnouncementDto) {
    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        if (!ann.title.isNullOrBlank()) Text(ann.title, style = SslType.body)
        val bodyText = ann.body ?: ann.message
        if (!bodyText.isNullOrBlank()) Text(bodyText, style = SslType.caption)
        if (!ann.authorName.isNullOrBlank()) {
            Text(
                "${ann.authorName} · ${relativeTimeLabel(SslDateParser.parseMillis(ann.createdAt))}",
                style = SslType.label,
            )
        }
    }
}

@Composable
private fun MeetCard(meet: MeetDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().sslGlassCard().clickable { onClick() }.padding(SslSpacing.md),
    ) {
        Text(meet.resolvedTitle, style = SslType.body)
        val date = meet.resolvedDate
        if (!date.isNullOrBlank()) Text(date, style = SslType.caption, color = SslColors.Blue)
        if (!meet.resolvedLocation.isNullOrBlank()) Text(meet.resolvedLocation!!, style = SslType.caption)
        if (meet.ssmUrl != null || meet.registrationUrl != null) {
            Text("Tap to open →", style = SslType.label, color = SslColors.Blue)
        }
    }
}
