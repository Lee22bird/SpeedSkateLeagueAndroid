package com.speedskateleague.android.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.speedskateleague.android.network.CoachPendingMemberDto
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
internal fun CoachRequestsTab(state: CoachToolsUiState, viewModel: CoachToolsViewModel) {
    LazyColumn(
        contentPadding = tabContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        items(state.pending, key = { it.id }) { member ->
            CoachPendingCard(
                member = member,
                onApprove = { viewModel.approve(member) },
                onDeny = { viewModel.deny(member) },
            )
        }
    }
}

@Composable
private fun CoachPendingCard(member: CoachPendingMemberDto, onApprove: () -> Unit, onDeny: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoachAvatar(member.resolvedAvatarUrl, member.fullName)
        Column(modifier = Modifier.weight(1f)) {
            Text(member.fullName, style = SslType.body)
            Text(
                listOfNotNull(member.sslSkaterId ?: "SSL ID pending", member.ageGroup).joinToString(" • "),
                style = SslType.caption,
            )
        }
        OutlinedButton(onClick = onDeny) { Text("Deny") }
        Button(onClick = onApprove, colors = ButtonDefaults.buttonColors(containerColor = SslColors.Blue)) {
            Text("Approve")
        }
    }
}
