package com.speedskateleague.android.ui.meetdirector

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.network.DiscussionDto
import com.speedskateleague.android.network.DiscussionReplyDto
import com.speedskateleague.android.network.SslDateParser
import com.speedskateleague.android.ui.coach.coachFieldColors
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.relativeTimeLabel
import com.speedskateleague.android.ui.theme.sslGlassCard

private const val SSM_URL = "https://speedskatemeet.com"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetDirectorScreen(viewModel: MeetDirectorViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.load() },
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                state.selected != null -> DiscussionDetail(
                    discussion = state.selected!!,
                    onBack = { viewModel.select(null) },
                    onReply = { body -> viewModel.reply(state.selected!!.id, body) },
                )
                else -> DiscussionList(
                    discussions = state.discussions,
                    isLoading = state.isLoading,
                    errorMessage = state.errorMessage,
                    onSelect = { viewModel.select(it) },
                    onPost = { title, body -> viewModel.createDiscussion(title, body) {} },
                )
            }
        }
    }
}

@Composable
private fun DiscussionList(
    discussions: List<DiscussionDto>,
    isLoading: Boolean,
    errorMessage: String?,
    onSelect: (DiscussionDto) -> Unit,
    onPost: (String, String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item { QuickToolsCard(onOpenUrl = { url -> context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }) }

        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                Text("NEW DISCUSSION", style = SslType.label)
                Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coachFieldColors(),
                )
                Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Body") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coachFieldColors(),
                )
                Spacer(modifier = Modifier.height(SslSpacing.sm))
                SslPrimaryButton(
                    text = "Post",
                    enabled = title.isNotBlank() && body.isNotBlank(),
                    onClick = {
                        onPost(title, body)
                        title = ""
                        body = ""
                    },
                )
            }
        }

        if (isLoading && discussions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(SslSpacing.lg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SslColors.Blue)
                }
            }
        } else if (discussions.isEmpty()) {
            item { Text(errorMessage ?: "No discussions yet.", style = SslType.body) }
        } else {
            items(discussions, key = { it.id }) { discussion ->
                DiscussionRow(discussion, onClick = { onSelect(discussion) })
            }
        }
    }
}

@Composable
private fun QuickToolsCard(onOpenUrl: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        Text("DIRECTOR QUICK TOOLS", style = SslType.label)
        Text("Your SpeedSkateLeague login works on SpeedSkateMeet.", style = SslType.caption)
        Spacer(modifier = Modifier.height(SslSpacing.sm))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm)) {
            items(
                listOf(
                    "Open SpeedSkateMeet" to SSM_URL,
                    "Create New Meet" to "$SSM_URL/meets/new",
                    "My SSM Meets" to "$SSM_URL/meets",
                ),
            ) { (label, url) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(SslSpacing.sm))
                        .background(SslColors.GlassFill)
                        .clickable { onOpenUrl(url) }
                        .padding(horizontal = SslSpacing.md, vertical = SslSpacing.sm),
                ) {
                    Text(label, style = SslType.caption, color = SslColors.Blue)
                }
            }
        }
    }
}

@Composable
private fun DiscussionRow(discussion: DiscussionDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().sslGlassCard().clickable { onClick() }.padding(SslSpacing.md),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            if (discussion.pinned) {
                Box(modifier = Modifier.clip(RoundedCornerShape(SslSpacing.xs)).background(SslColors.Orange.copy(alpha = 0.2f))) {
                    Text("PINNED", style = SslType.label, color = SslColors.Orange, modifier = Modifier.padding(horizontal = SslSpacing.sm))
                }
            } else {
                Text(discussion.category ?: "General", style = SslType.label, color = SslColors.Blue)
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
private fun DiscussionDetail(discussion: DiscussionDto, onBack: () -> Unit, onReply: (String) -> Unit) {
    var replyText by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            Text("← Back to discussions", style = SslType.caption, color = SslColors.Blue,
                modifier = Modifier.clickable { onBack() })
        }
        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                Text(discussion.title ?: "Discussion", style = SslType.title)
                Text(
                    "${discussion.authorName ?: "Unknown"} · ${relativeTimeLabel(SslDateParser.parseMillis(discussion.createdAt))}",
                    style = SslType.caption,
                )
                Spacer(modifier = Modifier.height(SslSpacing.sm))
                Text(discussion.body ?: "", style = SslType.body)
            }
        }
        item { Text("REPLIES", style = SslType.label) }
        items(discussion.replies, key = { it.id }) { reply -> ReplyRow(reply) }
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
                Spacer(modifier = Modifier.height(SslSpacing.sm))
                SslPrimaryButton(
                    text = "Reply",
                    enabled = replyText.isNotBlank(),
                    onClick = { onReply(replyText); replyText = "" },
                )
            }
        }
    }
}

@Composable
private fun ReplyRow(reply: DiscussionReplyDto) {
    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(reply.authorName ?: "Unknown", style = SslType.caption, color = SslColors.Blue)
            Text(relativeTimeLabel(SslDateParser.parseMillis(reply.createdAt)), style = SslType.label)
        }
        Text(reply.body ?: "", style = SslType.body)
    }
}
