package com.speedskateleague.android.ui.notifications

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
import androidx.compose.foundation.lazy.items
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
import com.speedskateleague.android.network.SslNotification
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.relativeTimeLabel
import com.speedskateleague.android.ui.theme.sslGlassCard

/** Android equivalent of the notification inbox in SSLNotificationCenter.swift. */
@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(SslSpacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (state.unreadCount > 0) "${state.unreadCount} unread" else "All caught up",
                    style = SslType.title,
                )
                if (state.unreadCount > 0) {
                    Text(
                        "Mark all read",
                        style = SslType.caption,
                        color = SslColors.Blue,
                        modifier = Modifier.clickable { viewModel.markAllRead() },
                    )
                }
            }

            when {
                state.isLoading && state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SslColors.Blue)
                    }
                }
                state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.errorMessage ?: "No notifications yet.", style = SslType.body)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = SslSpacing.md, vertical = SslSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            NotificationRow(item = item, onClick = { viewModel.markRead(item.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: SslNotification, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .clickable { onClick() }
            .padding(SslSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!item.isRead) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(SslColors.Blue),
            )
        } else {
            Box(modifier = Modifier.size(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, style = SslType.body)
            if (item.body.isNotBlank()) {
                Text(item.body, style = SslType.caption, maxLines = 2)
            }
        }
        Text(relativeTimeLabel(item.createdAtMillis), style = SslType.label)
    }
}
