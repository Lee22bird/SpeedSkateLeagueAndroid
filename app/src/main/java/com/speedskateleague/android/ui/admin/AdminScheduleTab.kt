package com.speedskateleague.android.ui.admin

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.speedskateleague.android.network.ScheduleEventDto
import com.speedskateleague.android.network.ScheduleEventRequest
import com.speedskateleague.android.ui.coach.coachFieldColors
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslPrimaryButton
import com.speedskateleague.android.ui.theme.SslSecondaryButton
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.sslGlassCard
import java.util.Calendar

@Composable
internal fun AdminScheduleTab(state: AdminUiState, viewModel: AdminViewModel) {
    if (state.editingScheduleEvent != null) {
        ScheduleEditor(
            event = state.editingScheduleEvent,
            onCancel = viewModel::closeScheduleEditor,
            onSave = viewModel::saveScheduleEvent,
        )
        return
    }

    if (state.isLoading && state.scheduleEvents.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SslColors.Blue)
        }
        return
    }

    LazyColumn(
        contentPadding = adminContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            SslPrimaryButton(text = "Add Event", onClick = { viewModel.openScheduleEditor(null) })
        }
        if (state.scheduleEvents.isEmpty()) {
            item { Text("No schedule events.", style = SslType.body) }
        } else {
            items(state.scheduleEvents, key = { it.id ?: it.hashCode() }) { event ->
                ScheduleRow(
                    event = event,
                    onClick = { viewModel.openScheduleEditor(event) },
                    onDelete = { viewModel.deleteScheduleEvent(event) },
                )
            }
        }
    }
}

@Composable
private fun ScheduleRow(event: ScheduleEventDto, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .clickable { onClick() }
            .padding(SslSpacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(event.title ?: "Event", style = SslType.body)
            Text(
                listOfNotNull(event.eventDate, event.league, event.location).joinToString(" • "),
                style = SslType.caption,
            )
            if (!event.published) Text("Unpublished", style = SslType.label, color = SslColors.Orange)
        }
        Text(
            "Delete",
            style = SslType.label,
            color = SslColors.Urgent,
            modifier = Modifier.clickable { onDelete() },
        )
    }
}

@Composable
private fun ScheduleEditor(
    event: ScheduleEventDto,
    onCancel: () -> Unit,
    onSave: (ScheduleEventRequest) -> Unit,
) {
    val context = LocalContext.current
    var league by remember(event.id) { mutableStateOf(event.league ?: "") }
    var title by remember(event.id) { mutableStateOf(event.title ?: "") }
    var eventDate by remember(event.id) { mutableStateOf(event.eventDate ?: "") }
    var location by remember(event.id) { mutableStateOf(event.location ?: "") }
    var venue by remember(event.id) { mutableStateOf(event.venue ?: "") }
    var ssmUrl by remember(event.id) { mutableStateOf(event.ssmUrl ?: "") }
    var published by remember(event.id) { mutableStateOf(event.published) }

    LazyColumn(
        contentPadding = adminContentPadding,
        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth().sslGlassCard().padding(SslSpacing.md)) {
                Text(if (event.id == null) "ADD EVENT" else "EDIT EVENT", style = SslType.label)

                Field("League", league) { league = it }
                Field("Title", title) { title = it }

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = {},
                    label = { Text("Date") },
                    placeholder = { Text("Tap to pick a date") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = SslSpacing.sm).clickable {
                        val now = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day -> eventDate = "%04d-%02d-%02d".format(year, month + 1, day) },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    },
                    colors = coachFieldColors(),
                )

                Field("Location", location) { location = it }
                Field("Venue", venue) { venue = it }
                Field("SSM URL", ssmUrl) { ssmUrl = it }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = SslSpacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Published", style = SslType.body)
                    Switch(checked = published, onCheckedChange = { published = it })
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(SslSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(SslSpacing.sm)) {
                    SslSecondaryButton(text = "Cancel", modifier = Modifier.weight(1f), onClick = onCancel)
                    SslPrimaryButton(
                        text = "Save",
                        modifier = Modifier.weight(1f),
                        enabled = league.isNotBlank() && title.isNotBlank() && eventDate.isNotBlank(),
                        onClick = {
                            onSave(
                                ScheduleEventRequest(
                                    league = league,
                                    title = title,
                                    eventDate = eventDate,
                                    location = location.ifBlank { null },
                                    venue = venue.ifBlank { null },
                                    ssmUrl = ssmUrl.ifBlank { null },
                                    published = published,
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
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(top = SslSpacing.sm),
        colors = coachFieldColors(),
    )
}
