package com.speedskateleague.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SslPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(SslRadius.md))
            .background(if (enabled) SslPrimaryButtonBrush else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)))
            .clickable(enabled = enabled && !loading) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
        } else {
            Text(text, style = SslType.body, color = Color.White)
        }
    }
}

@Composable
fun SslSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(SslRadius.md))
            .background(SslColors.GlassFill)
            .border(1.dp, SslColors.GlassBorder, RoundedCornerShape(SslRadius.md))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = SslType.body, color = SslColors.TextPrimary.copy(alpha = 0.85f))
    }
}
