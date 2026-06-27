package com.speedskateleague.android.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.speedskateleague.android.network.AnnouncementDto
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.relativeTimeLabel
import com.speedskateleague.android.ui.theme.sslGlassCard
import com.speedskateleague.android.network.SslDateParser

@Composable
internal fun CoachAnnouncementsTab(state: CoachToolsUiState, viewModel: CoachToolsViewModel) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = tabContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                Text("POST ANNOUNCEMENT", style = SslType.label)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                SslPrimaryButton(
                    text = "Post",
                    onClick = {
                        viewModel.postAnnouncement(title, body)
                        title = ""
                        body = ""
                    },
                )
            }
        }
        items(state.announcements, key = { it.id ?: it.hashCode() }) { announcement ->
            CoachAnnouncementCard(announcement = announcement, onDelete = { viewModel.deleteAnnouncement(announcement) })
        }
    }
}

@Composable
private fun CoachAnnouncementCard(announcement: AnnouncementDto, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(announcement.title ?: "Announcement", style = SslType.body)
            Text(relativeTimeLabel(SslDateParser.parseMillis(announcement.createdAt)), style = SslType.label)
        }
        if (!announcement.body.isNullOrBlank()) {
            Text(announcement.body, style = SslType.caption)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            announcement.authorName?.let { Text(it, style = SslType.label) }
            Text(
                "Delete",
                style = SslType.label,
                color = SslColors.Urgent,
                modifier = Modifier.clickable { onDelete() },
            )
        }
    }
}

@Composable
internal fun coachFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = SslColors.GlassBorder,
    unfocusedBorderColor = SslColors.GlassBorder,
    focusedLabelColor = SslColors.TextSecondary,
    unfocusedLabelColor = SslColors.TextSecondary,
)
