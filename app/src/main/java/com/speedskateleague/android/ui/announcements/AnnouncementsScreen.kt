package com.speedskateleague.android.ui.announcements

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.network.SslAnnouncement
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.relativeTimeLabel
import com.speedskateleague.android.ui.theme.sslGlassCard

/** Android equivalent of AnnouncementCenterView in AnnouncementCenter.swift. */
@Composable
fun AnnouncementsScreen(viewModel: AnnouncementsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedId by remember { mutableStateOf<String?>(null) }

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("ANNOUNCEMENTS", style = SslType.label, modifier = Modifier.padding(SslSpacing.md))

            LazyRow(
                modifier = Modifier.padding(horizontal = SslSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(SslSpacing.xs),
            ) {
                items(AnnouncementFilter.entries) { filter ->
                    FilterChip(
                        label = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = state.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.sm))

            when {
                state.isLoading && state.all.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SslColors.Blue)
                    }
                }
                state.filtered.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.errorMessage ?: "No announcements yet.", style = SslType.body)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = SslSpacing.md, vertical = SslSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
                    ) {
                        items(state.filtered, key = { it.id }) { announcement ->
                            AnnouncementCard(
                                announcement = announcement,
                                expanded = expandedId == announcement.id,
                                onClick = {
                                    expandedId = if (expandedId == announcement.id) null else announcement.id
                                    viewModel.markRead(announcement)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(SslSpacing.sm))
            .background(if (selected) SslColors.Blue else SslColors.GlassFill)
            .clickable { onClick() }
            .padding(horizontal = SslSpacing.md, vertical = SslSpacing.sm),
    ) {
        Text(label, style = SslType.caption, color = if (selected) androidx.compose.ui.graphics.Color.White else SslColors.TextSecondary)
    }
}

@Composable
private fun AnnouncementCard(announcement: SslAnnouncement, expanded: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .clickable { onClick() }
            .padding(SslSpacing.md),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(announcement.source.uppercase(), style = SslType.label, color = sourceColor(announcement.source))
            Text(relativeTimeLabel(announcement.createdAtMillis), style = SslType.label)
        }
        Text(announcement.title, style = SslType.body)
        val bodyText = if (expanded) announcement.body else announcement.body.take(110)
        if (bodyText.isNotBlank()) {
            Text(bodyText, style = SslType.caption)
        }
        if (expanded) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.sm))
            announcement.author?.let { Text("Author: $it", style = SslType.label) }
            announcement.league?.let { Text("League: $it", style = SslType.label) }
            announcement.team?.let { Text("Team: $it", style = SslType.label) }
        }
    }
}

private fun sourceColor(source: String) = when (source) {
    "league" -> SslColors.Blue
    "team", "coach" -> SslColors.Orange
    else -> SslColors.TextSecondary
}
