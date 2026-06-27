package com.speedskateleague.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/** Android equivalent of SettingsView.swift's 6 notification preference toggles. */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onSignOut: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
        }
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
