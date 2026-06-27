package com.speedskateleague.android.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.speedskateleague.android.network.AdminProfileDto
import com.speedskateleague.android.network.AdminProfileUpdateRequest
import com.speedskateleague.android.network.AdminRoleOptions
import com.speedskateleague.android.network.PendingPersonDto
import com.speedskateleague.android.ui.coach.CoachAvatar
import com.speedskateleague.android.ui.coach.coachFieldColors
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSecondaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

@Composable
internal fun AdminUsersTab(state: AdminUiState, viewModel: AdminViewModel) {
    if (state.editingProfile != null) {
        ProfileEditor(
            profile = state.editingProfile,
            onCancel = viewModel::closeProfileEditor,
            onSave = viewModel::saveProfile,
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            label = { Text("Search by name, team, league, role…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(SslSpacing.md),
            colors = coachFieldColors(),
        )

        LazyColumn(
            contentPadding = adminContentPadding,
            verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        ) {
            if (state.searchQuery.trim().length < 2) {
                item { Text("Type at least 2 characters to search.", style = SslType.caption) }
            } else if (state.searchResults.isEmpty()) {
                item { Text("No matching users.", style = SslType.caption) }
            } else {
                items(state.searchResults, key = { it.id }) { person ->
                    UserRow(person, onClick = { viewModel.openProfileEditor(person.id) })
                }
            }
        }
    }
}

@Composable
private fun UserRow(person: PendingPersonDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .clickable { onClick() }
            .padding(SslSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoachAvatar(person.resolvedAvatarUrl, person.fullName ?: "?")
        Column(modifier = Modifier.weight(1f)) {
            Text(person.fullName ?: "Unknown", style = SslType.body)
            Text(
                listOfNotNull(person.team, person.league, person.role).joinToString(" • "),
                style = SslType.caption,
            )
        }
    }
}

@Composable
private fun ProfileEditor(
    profile: AdminProfileDto,
    onCancel: () -> Unit,
    onSave: (AdminProfileUpdateRequest) -> Unit,
) {
    var fullName by remember(profile.id) { mutableStateOf(profile.fullName ?: "") }
    var team by remember(profile.id) { mutableStateOf(profile.team ?: "") }
    var league by remember(profile.id) { mutableStateOf(profile.league ?: "") }
    var role by remember(profile.id) { mutableStateOf(profile.role ?: "skater") }
    var secondaryRole by remember(profile.id) { mutableStateOf(profile.secondaryRole) }
    var pendingRole by remember(profile.id) { mutableStateOf(profile.pendingRole) }
    var teamStatus by remember(profile.id) { mutableStateOf(profile.teamStatus ?: "active") }

    LazyColumn(
        contentPadding = adminContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                Text("EDIT PROFILE", style = SslType.label)
                profile.sslSkaterId?.let { Text("SSL ID: $it", style = SslType.caption) }

                LabeledField("Full Name", fullName) { fullName = it }
                LabeledField("Team", team) { team = it }
                LabeledField("League", league) { league = it }

                RoleSelector("Role", AdminRoleOptions.PRIMARY_ROLES, role) { role = it }
                RoleSelector(
                    "Secondary Role",
                    listOf("none") + AdminRoleOptions.SECONDARY_ROLES,
                    secondaryRole ?: "none",
                ) { secondaryRole = if (it == "none") null else it }
                RoleSelector(
                    "Pending Role",
                    listOf("none") + AdminRoleOptions.PENDING_ROLES,
                    pendingRole ?: "none",
                ) { pendingRole = if (it == "none") null else it }
                RoleSelector("Team Status", AdminRoleOptions.TEAM_STATUSES, teamStatus) { teamStatus = it }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm)) {
                    SslSecondaryButton(text = "Cancel", modifier = Modifier.weight(1f), onClick = onCancel)
                    SslPrimaryButton(
                        text = "Save",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onSave(
                                AdminProfileUpdateRequest(
                                    fullName = fullName,
                                    gender = profile.gender,
                                    birthdate = profile.birthdate,
                                    team = team.ifBlank { null },
                                    league = league.ifBlank { null },
                                    role = role,
                                    secondaryRole = secondaryRole,
                                    pendingRole = pendingRole,
                                    pendingLeague = profile.pendingLeague,
                                    teamStatus = teamStatus,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = SslSpacing.sm)) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = coachFieldColors(),
        )
    }
}

@Composable
private fun RoleSelector(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(top = SslSpacing.sm)) {
        Text(label, style = SslType.label)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.xs))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SslSpacing.xs)) {
            items(options) { option ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(SslSpacing.sm))
                        .background(if (option == selected) SslColors.Blue else SslColors.GlassFill)
                        .clickable { onSelect(option) }
                        .padding(horizontal = SslSpacing.sm, vertical = 6.dp),
                ) {
                    Text(
                        option.replace("_", " "),
                        style = SslType.caption,
                        color = if (option == selected) Color.White else SslColors.TextSecondary,
                    )
                }
            }
        }
    }
}
