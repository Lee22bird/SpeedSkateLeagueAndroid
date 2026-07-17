package com.speedskateleague.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.speedskateleague.android.network.NotificationPreferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSecondaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard

/** Android equivalent of SettingsView.swift's notification preferences +
 *  Delete Account section. */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onSignOut: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SslSpacing.md),
            verticalArrangement = Arrangement.spacedBy(SslSpacing.md),
        ) {
            Text("NOTIFICATIONS", style = SslType.label)

            PreferenceToggle(
                title = "Team announcements",
                subtitle = "Posts from your team coach",
                checked = state.preferences.teamAnnouncements,
                onCheckedChange = { v -> viewModel.update { it.copy(teamAnnouncements = v) } },
            )
            PreferenceToggle(
                title = "League announcements",
                subtitle = "Updates from your league",
                checked = state.preferences.leagueAnnouncements,
                onCheckedChange = { v -> viewModel.update { it.copy(leagueAnnouncements = v) } },
            )
            PreferenceToggle(
                title = "Meet reminders",
                subtitle = "Reminders for upcoming meets",
                checked = state.preferences.meetReminders,
                onCheckedChange = { v -> viewModel.update { it.copy(meetReminders = v) } },
            )
            PreferenceToggle(
                title = "Coach updates",
                subtitle = "Direct messages from coaches",
                checked = state.preferences.coachUpdates,
                onCheckedChange = { v -> viewModel.update { it.copy(coachUpdates = v) } },
            )
            PreferenceToggle(
                title = "Push notifications",
                subtitle = "Alerts on this device",
                checked = state.preferences.pushNotifications,
                onCheckedChange = { v -> viewModel.update { it.copy(pushNotifications = v) } },
            )
            PreferenceToggle(
                title = "Email notifications",
                subtitle = "Summary emails",
                checked = state.preferences.emailNotifications,
                onCheckedChange = { v -> viewModel.update { it.copy(emailNotifications = v) } },
            )

            if (state.statusMessage != null) {
                Text(
                    state.statusMessage!!,
                    style = SslType.caption,
                    color = if (state.statusMessage == "Saved.") SslColors.Blue else SslColors.Urgent,
                )
            }

            SslPrimaryButton(text = "Save Preferences", loading = state.isSaving, onClick = viewModel::save)
            SslSecondaryButton(text = "Sign Out", onClick = onSignOut)

            Text("ACCOUNT", style = SslType.label, modifier = Modifier.padding(top = SslSpacing.md))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .sslGlassCard()
                    .padding(SslSpacing.md),
                verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
            ) {
                Text(
                    "Deleting your account is permanent: your login, profile, child profiles, and personal data are removed. This cannot be undone.",
                    style = SslType.caption,
                )
                SslSecondaryButton(
                    text = if (state.isDeletingAccount) "Deleting…" else "Delete Account",
                    onClick = { if (!state.isDeletingAccount) showDeleteConfirm = true },
                )
                if (state.deleteAccountError != null) {
                    Text(state.deleteAccountError!!, style = SslType.caption, color = SslColors.Urgent)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false; deleteConfirmText = "" },
            title = { Text("Delete your account?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(SslSpacing.sm)) {
                    Text(
                        "This permanently deletes your Speed Skate League account, your profile, any child skater profiles, and your personal data. League race records are kept but unlinked from your profile. This cannot be undone.",
                        style = SslType.caption,
                    )
                    OutlinedTextField(
                        value = deleteConfirmText,
                        onValueChange = { deleteConfirmText = it },
                        singleLine = true,
                        label = { Text("Type DELETE to confirm") },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = deleteConfirmText.trim().equals("DELETE", ignoreCase = true),
                    onClick = {
                        showDeleteConfirm = false
                        deleteConfirmText = ""
                        viewModel.deleteAccount(onDeleted = onSignOut)
                    },
                ) { Text("Delete Forever", color = SslColors.Urgent) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false; deleteConfirmText = "" }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PreferenceToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .padding(SslSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = SslType.body)
            Text(subtitle, style = SslType.caption)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = SslColors.Blue),
        )
    }
}
