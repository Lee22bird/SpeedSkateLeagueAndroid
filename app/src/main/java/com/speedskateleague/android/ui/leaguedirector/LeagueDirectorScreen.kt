package com.speedskateleague.android.ui.leaguedirector

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.network.DiscussionDto
import com.speedskateleague.android.network.DiscussionReplyDto
import com.speedskateleague.android.network.PendingPersonDto
import com.speedskateleague.android.network.SslDateParser
import com.speedskateleague.android.ui.coach.CoachAvatar
import com.speedskateleague.android.ui.coach.coachFieldColors
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.relativeTimeLabel
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
fun LeagueDirectorScreen(viewModel: LeagueDirectorViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Directors Forum")

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = SslColors.Navy,
                contentColor = SslColors.Blue,
                edgePadding = SslSpacing.md,
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = SslType.caption) },
                        selectedContentColor = SslColors.Blue,
                        unselectedContentColor = SslColors.TextTertiary,
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewTab(state, viewModel)
                1 -> DirectorsForumTab(state, viewModel)
            }
        }
    }
}

@Composable
private fun OverviewTab(state: LeagueDirectorUiState, viewModel: LeagueDirectorViewModel) {
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
                    item { Text(state.message, style = SslType.caption, color = SslColors.Orange) }
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

@Composable
private fun DirectorsForumTab(state: LeagueDirectorUiState, viewModel: LeagueDirectorViewModel) {
    when {
        state.selectedDiscussion != null -> {
            LdDiscussionDetail(
                discussion = state.selectedDiscussion,
                onBack = { viewModel.selectDiscussion(null) },
                onReply = { body -> viewModel.replyToDiscussion(state.selectedDiscussion.id, body) },
            )
        }
        else -> {
            LdDiscussionList(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun LdDiscussionList(state: LeagueDirectorUiState, viewModel: LeagueDirectorViewModel) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                Text("NEW DISCUSSION", style = SslType.label)
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
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                SslPrimaryButton(
                    text = "Post",
                    enabled = title.isNotBlank() && body.isNotBlank(),
                    onClick = {
                        viewModel.createDiscussion(title, body) {}
                        title = ""
                        body = ""
                    },
                )
            }
        }

        if (state.forumLoading && state.discussions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(SslSpacing.lg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SslColors.Blue)
                }
            }
        } else if (state.discussions.isEmpty()) {
            item { Text(state.forumError ?: "No discussions yet.", style = SslType.body) }
        } else {
            items(state.discussions, key = { it.id }) { discussion ->
                LdDiscussionRow(discussion, onClick = { viewModel.selectDiscussion(discussion) })
            }
        }
    }
}

@Composable
private fun LdDiscussionRow(discussion: DiscussionDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .clickable { onClick() }
            .padding(SslSpacing.md),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            if (discussion.pinned) {
                Text("PINNED", style = SslType.label, color = SslColors.Orange)
            } else {
                Text("Directors Forum", style = SslType.label, color = SslColors.Blue)
            }
            Text(relativeTimeLabel(SslDateParser.parseMillis(discussion.createdAt)), style = SslType.label)
        }
        Text(discussion.title ?: "Discussion", style = SslType.body)
        Text(
            "${discussion.authorName ?: "Unknown"} · ${discussion.replyCount} replies",
            style = SslType.caption,
        )
    }
}

@Composable
private fun LdDiscussionDetail(discussion: DiscussionDto, onBack: () -> Unit, onReply: (String) -> Unit) {
    var replyText by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            Text(
                "← Back to discussions",
                style = SslType.caption,
                color = SslColors.Blue,
                modifier = Modifier.clickable { onBack() },
            )
        }
        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                Text(discussion.title ?: "Discussion", style = SslType.title)
                Text(
                    "${discussion.authorName ?: "Unknown"} · ${relativeTimeLabel(SslDateParser.parseMillis(discussion.createdAt))}",
                    style = SslType.caption,
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                Text(discussion.body ?: "", style = SslType.body)
            }
        }
        item { Text("REPLIES", style = SslType.label) }
        items(discussion.replies, key = { it.id }) { reply -> LdReplyRow(reply) }
        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Add a reply") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                SslPrimaryButton(
                    text = "Reply",
                    enabled = replyText.isNotBlank(),
                    onClick = {
                        onReply(replyText)
                        replyText = ""
                    },
                )
            }
        }
    }
}

@Composable
private fun LdReplyRow(reply: DiscussionReplyDto) {
    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(reply.authorName ?: "Unknown", style = SslType.caption, color = SslColors.Blue)
            Text(relativeTimeLabel(SslDateParser.parseMillis(reply.createdAt)), style = SslType.label)
        }
        Text(reply.body ?: "", style = SslType.body)
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
