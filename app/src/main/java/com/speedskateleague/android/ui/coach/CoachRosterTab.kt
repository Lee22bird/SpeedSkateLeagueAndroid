package com.speedskateleague.android.ui.coach

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.speedskateleague.android.network.CoachRosterMemberDto
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
internal fun CoachRosterTab(state: CoachToolsUiState) {
    LazyColumn(
        contentPadding = tabContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        items(state.roster, key = { it.id }) { member ->
            CoachRosterRow(member)
        }
    }
}

@Composable
internal fun CoachRosterRow(member: CoachRosterMemberDto, trailing: @Composable () -> Unit = { DuesBadge(member.duesStatus) }) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoachAvatar(member.resolvedAvatarUrl, member.fullName)
        Column(modifier = Modifier.weight(1f)) {
            Text(member.fullName, style = SslType.body)
            Text(
                listOfNotNull(
                    member.sslSkaterId ?: "SSL ID pending",
                    member.ageGroup ?: "No division",
                    "Helmet ${member.helmetNumber?.ifBlank { "-" } ?: "-"}",
                ).joinToString(" • "),
                style = SslType.caption,
            )
        }
        trailing()
    }
}

@Composable
internal fun CoachAvatar(url: String?, name: String) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(SslColors.BlueDeep),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            AsyncImage(model = url, contentDescription = name, modifier = Modifier.size(44.dp).clip(CircleShape))
        } else {
            Text(name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""), style = SslType.caption)
        }
    }
}

@Composable
internal fun DuesBadge(status: String) {
    val isPaid = status == "paid"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(SslSpacing.xs))
            .background(if (isPaid) SslColors.Blue.copy(alpha = 0.2f) else SslColors.Urgent.copy(alpha = 0.2f))
            .padding(horizontal = SslSpacing.sm, vertical = 4.dp),
    ) {
        Text(
            if (isPaid) "PAID" else "OWES",
            style = SslType.label,
            color = if (isPaid) SslColors.Blue else SslColors.Urgent,
        )
    }
}
