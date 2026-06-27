package com.speedskateleague.android.ui.meets

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speedskateleague.android.network.SslMeet
import com.speedskateleague.android.ui.theme.SslBackground
import com.speedskateleague.android.ui.theme.SslColors
import com.speedskateleague.android.ui.theme.SslSpacing
import com.speedskateleague.android.ui.theme.SslType
import com.speedskateleague.android.ui.theme.monthDayParts
import com.speedskateleague.android.ui.theme.sslGlassCard

/** Android equivalent of DiscoverMeetsView in UpcomingMeets.swift. */
@Composable
fun DiscoverMeetsScreen(viewModel: DiscoverMeetsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SslBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = state.searchText,
                onValueChange = viewModel::setSearchText,
                placeholder = { Text("Search meets") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(SslSpacing.md),
                shape = RoundedCornerShape(SslSpacing.sm),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = SslColors.GlassBorder,
                    unfocusedBorderColor = SslColors.GlassBorder,
                ),
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = SslSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(SslSpacing.xs),
            ) {
                items(DiscoverMeetsFilter.entries) { filter ->
                    DiscoverFilterChip(
                        label = filterLabel(filter),
                        selected = state.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(SslSpacing.sm))

            when {
                state.isLoading && state.meets.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SslColors.Blue)
                    }
                }
                state.filtered.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.errorMessage ?: "No meets found.", style = SslType.body)
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = SslSpacing.md, vertical = SslSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(SslSpacing.sm),
                    ) {
                        items(state.filtered, key = { it.id }) { meet ->
                            DiscoverMeetCard(meet = meet, onToggleFavorite = { viewModel.toggleFavorite(meet) })
                        }
                    }
                }
            }
        }
    }
}

private fun filterLabel(filter: DiscoverMeetsFilter) = when (filter) {
    DiscoverMeetsFilter.ALL -> "All"
    DiscoverMeetsFilter.UPCOMING -> "Upcoming"
    DiscoverMeetsFilter.NATIONAL -> "National"
    DiscoverMeetsFilter.REGIONAL -> "Regional"
    DiscoverMeetsFilter.LEAGUE -> "League"
    DiscoverMeetsFilter.FAVORITES -> "Favorites"
}

@Composable
private fun DiscoverFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(SslSpacing.sm))
            .background(if (selected) SslColors.Blue else SslColors.GlassFill)
            .clickable { onClick() }
            .padding(horizontal = SslSpacing.md, vertical = SslSpacing.sm),
    ) {
        Text(label, style = SslType.caption, color = if (selected) Color.White else SslColors.TextSecondary)
    }
}

@Composable
private fun DiscoverMeetCard(meet: SslMeet, onToggleFavorite: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sslGlassCard()
            .padding(SslSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(SslSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (month, day) = monthDayParts(meet.dateMillis)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(month, style = SslType.label, color = SslColors.Blue)
            Text(day, style = SslType.title)
        }
        Column(modifier = Modifier.weight(1f)) {
            meet.league?.let { Text(it.uppercase(), style = SslType.label, color = SslColors.Blue) }
            Text(meet.title, style = SslType.body)
            if (meet.displayLocation.isNotBlank()) {
                Text(meet.displayLocation, style = SslType.caption)
            }
            meet.registrationStatus?.let { Text(it, style = SslType.label) }
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                if (meet.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (meet.isFavorite) SslColors.Orange else SslColors.TextTertiary,
            )
        }
    }
}
