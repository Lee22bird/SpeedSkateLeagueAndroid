@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.speedskateleague.android.ui.skater

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.speedskateleague.android.network.SkaterPostDto
import com.speedskateleague.android.network.SkaterReplyDto
import com.speedskateleague.android.network.SkaterUserDto
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
fun SkaterScreen(vm: SkaterViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        when {
            state.selectedUser != null -> UserProfileView(state = state, vm = vm)
            state.expandedPost != null -> ExpandedPostView(state = state, vm = vm)
            else -> FeedView(state = state, vm = vm)
        }
    }
}

// ── Feed ──────────────────────────────────────────────────────────────────────

@Composable
private fun FeedView(state: SkaterUiState, vm: SkaterViewModel) {
    PullToRefreshBox(
        isRefreshing = state.isLoadingFeed && state.publicPosts.isEmpty() && state.followingPosts.isEmpty(),
        onRefresh = { vm.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(SslSpacing.md),
            verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Skater", style = SslType.display)
                    Text("Speed Skating Community", style = SslType.caption)
                }
            }

            item { ComposerCard(state = state, vm = vm) }

            item { TabRow(selected = state.tab, onSelect = vm::setTab) }

            val posts = if (state.tab == SkaterFeedTab.PUBLIC) state.publicPosts else state.followingPosts
            if (posts.isEmpty() && !state.isLoadingFeed) {
                item {
                    EmptyFeedCard(
                        message = if (state.tab == SkaterFeedTab.FOLLOWING)
                            "Follow skaters to see their posts here."
                        else
                            "No posts yet. Be the first!"
                    )
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    PostCard(post = post, currentUserId = state.currentUserId, vm = vm)
                }
                if (state.isLoadingFeed) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = SslColors.Blue, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Composer ──────────────────────────────────────────────────────────────────

@Composable
private fun ComposerCard(state: SkaterUiState, vm: SkaterViewModel) {
    Column(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        OutlinedTextField(
            value = state.composerText,
            onValueChange = vm::updateComposerText,
            placeholder = { Text("What's on your mind?", style = SslType.caption) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SslColors.TextPrimary,
                unfocusedTextColor = SslColors.TextPrimary,
                focusedBorderColor = SslColors.Blue,
                unfocusedBorderColor = SslColors.GlassBorder,
                cursorColor = SslColors.Blue,
            ),
        )

        if (state.postError != null) {
            Text(state.postError, style = SslType.caption, color = SslColors.Orange)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "${state.composerText.length}/300",
                style = SslType.label,
                color = if (state.composerText.length > 270) SslColors.Orange else SslColors.TextTertiary,
            )
            Button(
                onClick = vm::submitPost,
                enabled = !state.isPosting && state.composerText.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = SslColors.Blue),
            ) {
                if (state.isPosting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text("Post", style = SslType.body)
                }
            }
        }
    }
}

// ── Tab Row ───────────────────────────────────────────────────────────────────

@Composable
private fun TabRow(selected: SkaterFeedTab, onSelect: (SkaterFeedTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard(),
    ) {
        listOf(SkaterFeedTab.PUBLIC to "Public", SkaterFeedTab.FOLLOWING to "Following").forEach { (tab, label) ->
            val active = selected == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (active) SslColors.Blue.copy(alpha = 0.28f) else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = SslType.label,
                    color = if (active) SslColors.TextPrimary else SslColors.TextTertiary,
                )
            }
        }
    }
}

// ── Post Card ─────────────────────────────────────────────────────────────────

@Composable
fun PostCard(
    post: SkaterPostDto,
    currentUserId: String?,
    vm: SkaterViewModel,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .sslGlassCard()
            .clickable { vm.expandPost(post) }
            .padding(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        // Author row
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkaterAvatar(name = post.authorName, avatarUrl = post.authorAvatarUrl, size = 38.dp)
            Spacer(Modifier.width(SslSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(post.authorName, style = SslType.headline, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(post.createdAt.take(10), style = SslType.label, color = SslColors.TextTertiary)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = SslColors.TextTertiary)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    if (post.authorId == currentUserId) {
                        DropdownMenuItem(
                            text = { Text("Delete Post", color = SslColors.Orange) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = SslColors.Orange) },
                            onClick = { menuExpanded = false; showDeleteDialog = true },
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Report") },
                            leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                            onClick = { menuExpanded = false; showReportDialog = true },
                        )
                        DropdownMenuItem(
                            text = { Text("Block User", color = SslColors.Orange) },
                            onClick = { menuExpanded = false; vm.blockUser(post.authorId) },
                        )
                    }
                }
            }
        }

        // Body
        if (post.body.isNotEmpty()) {
            Text(post.body, style = SslType.body)
        }

        // Image
        if (!post.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(SslSpacing.xs)),
                contentScale = ContentScale.Crop,
            )
        }

        // Action row
        Row(horizontalArrangement = Arrangement.spacedBy(SslSpacing.md)) {
            // Reply count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = SslColors.TextTertiary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text("${post.replyCount}", style = SslType.label, color = SslColors.TextTertiary)
            }
            // Like
            Row(
                modifier = Modifier.clickable { vm.toggleLike(post) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (post.likedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.likedByMe) SslColors.Orange else SslColors.TextTertiary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${post.likeCount}",
                    style = SslType.label,
                    color = if (post.likedByMe) SslColors.Orange else SslColors.TextTertiary,
                )
            }
        }
    }

    // Delete confirm dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Post?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; vm.deletePost(post) }) {
                    Text("Delete", color = SslColors.Orange)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
        )
    }

    // Report dialog
    if (showReportDialog) {
        ReportDialog(
            onDismiss = { showReportDialog = false },
            onSubmit = { reason -> vm.reportPost(post, reason); showReportDialog = false },
        )
    }
}

// ── Expanded Post + Replies ───────────────────────────────────────────────────

@Composable
private fun ExpandedPostView(state: SkaterUiState, vm: SkaterViewModel) {
    val post = state.expandedPost ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            TextButton(onClick = vm::collapsePost) {
                Text("← Back to feed", style = SslType.body, color = SslColors.Blue)
            }
        }

        item {
            PostCard(post = post, currentUserId = state.currentUserId, vm = vm)
        }

        // Reply composer
        item {
            Column(
                modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
                verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
            ) {
                Text("REPLY", style = SslType.label)
                OutlinedTextField(
                    value = state.replyText,
                    onValueChange = vm::updateReplyText,
                    placeholder = { Text("Write a reply…", style = SslType.caption) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SslColors.TextPrimary,
                        unfocusedTextColor = SslColors.TextPrimary,
                        focusedBorderColor = SslColors.Blue,
                        unfocusedBorderColor = SslColors.GlassBorder,
                        cursorColor = SslColors.Blue,
                    ),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "${state.replyText.length}/300",
                        style = SslType.label,
                        color = if (state.replyText.length > 270) SslColors.Orange else SslColors.TextTertiary,
                    )
                    Button(
                        onClick = { vm.submitReply(post.id) },
                        enabled = !state.isReplying && state.replyText.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = SslColors.Blue),
                    ) {
                        if (state.isReplying) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("Reply", style = SslType.body)
                        }
                    }
                }
            }
        }

        // Replies
        val replies = post.replies.orEmpty()
        if (replies.isNotEmpty()) {
            item { Text("REPLIES", style = SslType.label) }
            items(replies, key = { it.id }) { reply ->
                ReplyCard(reply = reply)
            }
        }
    }
}

// ── User Profile ──────────────────────────────────────────────────────────────

@Composable
private fun UserProfileView(state: SkaterUiState, vm: SkaterViewModel) {
    val user = state.selectedUser ?: return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(SslSpacing.md),
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            TextButton(onClick = vm::clearSelectedUser) {
                Text("← Back to feed", style = SslType.body, color = SslColors.Blue)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkaterAvatar(name = user.fullName, avatarUrl = user.avatarUrl, size = 56.dp)
                Spacer(Modifier.width(SslSpacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.fullName, style = SslType.title)
                    if (!user.sslSkaterId.isNullOrBlank()) {
                        Text("SSL ID: ${user.sslSkaterId}", style = SslType.caption)
                    }
                    if (!user.team.isNullOrBlank()) {
                        Text(user.team, style = SslType.caption)
                    }
                }
                if (user.id != state.currentUserId) {
                    Button(
                        onClick = { vm.toggleFollow(user.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.selectedUserIsFollowing) SslColors.Blue.copy(alpha = 0.25f) else SslColors.Blue,
                        ),
                    ) {
                        Text(if (state.selectedUserIsFollowing) "Following" else "Follow", style = SslType.body)
                    }
                }
            }
        }

        if (state.userPosts.isEmpty()) {
            item { Text("No posts yet.", style = SslType.caption) }
        } else {
            items(state.userPosts, key = { it.id }) { post ->
                PostCard(post = post, currentUserId = state.currentUserId, vm = vm)
            }
        }
    }
}

// ── Reply Card ────────────────────────────────────────────────────────────────

@Composable
private fun ReplyCard(reply: SkaterReplyDto) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        SkaterAvatar(name = reply.authorName, avatarUrl = reply.authorAvatarUrl, size = 30.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(SslSpacing.xs)) {
                Text(reply.authorName, style = SslType.headline)
                Text(reply.createdAt.take(10), style = SslType.label, color = SslColors.TextTertiary)
            }
            Text(reply.body, style = SslType.body)
        }
    }
}

// ── Avatar ────────────────────────────────────────────────────────────────────

@Composable
fun SkaterAvatar(name: String, avatarUrl: String?, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(SslColors.BlueDeep),
        contentAlignment = Alignment.Center,
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(),
                style = SslType.headline,
            )
        }
    }
}

// ── Empty Feed ────────────────────────────────────────────────────────────────

@Composable
private fun EmptyFeedCard(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, style = SslType.body, color = SslColors.TextTertiary)
    }
}

// ── Report Dialog ─────────────────────────────────────────────────────────────

@Composable
private fun ReportDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report Post") },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                placeholder = { Text("Reason…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(reason) }, enabled = reason.isNotBlank()) {
                Text("Submit")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
