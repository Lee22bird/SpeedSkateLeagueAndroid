package com.speedskateleague.android.ui.coach

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.speedskateleague.android.network.CoachPracticeDto
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard
import java.util.Calendar

@Composable
internal fun CoachPracticesTab(state: CoachToolsUiState, viewModel: CoachToolsViewModel) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = tabContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                Text("ADD PRACTICE", style = SslType.label)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Date") },
                    placeholder = { Text("Tap to pick a date") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable {
                        val now = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day -> date = "%04d-%02d-%02d".format(year, month + 1, day) },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    },
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = time,
                    onValueChange = {},
                    label = { Text("Time") },
                    placeholder = { Text("Tap to pick a time") },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable {
                        val now = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hour, minute -> time = "%02d:%02d".format(hour, minute) },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true,
                        ).show()
                    },
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = coachFieldColors(),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                SslPrimaryButton(
                    text = "Add Practice",
                    enabled = date.isNotBlank() && time.isNotBlank(),
                    onClick = {
                        viewModel.createPractice(title, date, time, location, notes)
                        title = ""; date = ""; time = ""; location = ""; notes = ""
                    },
                )
            }
        }
        items(state.practices, key = { it.id }) { practice ->
            CoachPracticeCard(practice = practice, onDelete = { viewModel.deletePractice(practice) })
        }
    }
}

@Composable
private fun CoachPracticeCard(practice: CoachPracticeDto, onDelete: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
        Text(practice.title ?: "Team Practice", style = SslType.body)
        val detail = listOfNotNull(practice.practiceDate, practice.startTime ?: practice.practiceTime, practice.location)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
        if (detail.isNotBlank()) Text(detail, style = SslType.caption)
        if (!practice.notes.isNullOrBlank()) Text(practice.notes, style = SslType.label)
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Delete",
                style = SslType.label,
                color = SslColors.Urgent,
                modifier = Modifier.clickable { onDelete() },
            )
        }
    }
}
