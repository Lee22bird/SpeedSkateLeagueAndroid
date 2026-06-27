package com.speedskateleague.android.ui.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
internal fun CoachDuesTab(state: CoachToolsUiState, viewModel: CoachToolsViewModel) {
    val paidCount = state.roster.count { it.duesStatus == "paid" }
    val owesCount = state.roster.size - paidCount

    LazyColumn(
        contentPadding = tabContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text("Paid: $paidCount", style = SslType.body)
                Text("Owes: $owesCount", style = SslType.body)
            }
        }
        items(state.roster, key = { it.id }) { member ->
            CoachRosterRow(member) {
                val nextStatus = if (member.duesStatus == "paid") "owes" else "paid"
                androidx.compose.material3.TextButton(onClick = { viewModel.setDues(member, nextStatus) }) {
                    Text(if (member.duesStatus == "paid") "Mark Owes" else "Mark Paid", style = SslType.label)
                }
            }
        }
    }
}
