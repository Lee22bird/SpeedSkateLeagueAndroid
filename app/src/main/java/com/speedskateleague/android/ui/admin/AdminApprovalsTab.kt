package com.speedskateleague.android.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.speedskateleague.android.network.PendingPersonDto
import com.speedskateleague.android.ui.coach.CoachAvatar
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
internal fun AdminApprovalsTab(state: AdminUiState, viewModel: AdminViewModel) {
    if (state.isLoading && state.pendingRoles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SslColors.Blue)
        }
        return
    }

    LazyColumn(
        contentPadding = adminContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        if (state.pendingRoles.isEmpty()) {
            item { Text("No pending role or league requests.", style = SslType.body) }
        } else {
            items(state.pendingRoles, key = { it.id }) { person ->
                ApprovalCard(
                    person = person,
                    onApprove = { scope -> viewModel.approveRole(person, scope) },
                    onDeny = { scope -> viewModel.denyRole(person, scope) },
                )
            }
        }
    }
}

@Composable
private fun ApprovalCard(
    person: PendingPersonDto,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit,
) {
    val scope = when {
        person.pendingRole != null && person.pendingLeague != null -> "both"
        person.pendingLeague != null -> "league"
        else -> "role"
    }

    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        Row(horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm)) {
            CoachAvatar(person.resolvedAvatarUrl, person.fullName ?: "?")
            Column {
                Text(person.fullName ?: "Unknown", style = SslType.body)
                Text(
                    listOfNotNull(person.team, person.league).joinToString(" • "),
                    style = SslType.caption,
                )
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = SslSpacing.xs))
        Column(modifier = Modifier.padding(top = SslSpacing.sm)) {
            person.pendingRole?.let { Text("Requesting role: ${it.replace("_", " ")}", style = SslType.caption, color = SslColors.Blue) }
            person.pendingLeague?.let { Text("Requesting league: $it", style = SslType.caption, color = SslColors.Orange) }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = SslSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        ) {
            OutlinedButton(onClick = { onDeny(scope) }, modifier = Modifier.weight(1f)) { Text("Deny") }
            SslPrimaryButton(text = "Approve", modifier = Modifier.weight(1f), onClick = { onApprove(scope) })
        }
    }
}
