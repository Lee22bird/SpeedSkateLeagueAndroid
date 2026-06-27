package com.speedskateleague.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Ported 1:1 from AppTheme.swift's SSLColor/SSLSpacing/SSLRadius scales. */
object SslColors {
    val Navy = Color(0xFF030A14)
    val NavyDeep = Color(0xFF02050F)
    val Panel = Color(0xFF081A2B)
    val Blue = Color(0xFF0DA3F2)
    val BlueDeep = Color(0xFF0357D6)
    val Orange = Color(0xFFFF731F)
    val Urgent = Color(0xFFF52A40)
    val TextPrimary = Color.White
    val TextSecondary = Color.White.copy(alpha = 0.66f)
    val TextTertiary = Color.White.copy(alpha = 0.48f)

    val GlassFill = Color.White.copy(alpha = 0.06f)
    val GlassBorder = Color.White.copy(alpha = 0.12f)
}

object SslSpacing {
    val xs = 6.dp
    val sm = 10.dp
    val md = 16.dp
    val lg = 22.dp
    val xl = 30.dp
}

object SslRadius {
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 26.dp
}

object SslType {
    val display = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Black, color = SslColors.TextPrimary)
    val title = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black, color = SslColors.TextPrimary)
    val headline = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SslColors.TextPrimary)
    val body = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = SslColors.TextPrimary)
    val caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, color = SslColors.TextSecondary)
    val label = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Black, color = SslColors.TextTertiary)
}

private val SslDarkScheme = darkColorScheme(
    primary = SslColors.Blue,
    secondary = SslColors.Orange,
    background = SslColors.Navy,
    surface = SslColors.Panel,
    onPrimary = Color.White,
    onBackground = SslColors.TextPrimary,
    onSurface = SslColors.TextPrimary,
    error = SslColors.Urgent,
)

@Composable
fun SpeedSkateLeagueTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SslDarkScheme,
        typography = Typography(),
        content = content,
    )
}

/** Full-bleed dark navy background wash, equivalent to SSLBackground() in SwiftUI. */
@Composable
fun SslBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(SslColors.Navy, SslColors.NavyDeep, SslColors.Navy),
            ),
        ),
    ) {
        content()
    }
}

/** Equivalent to the .sslGlassCard() ViewModifier. */
fun Modifier.sslGlassCard(radius: androidx.compose.ui.unit.Dp = SslRadius.md): Modifier = this
    .clip(RoundedCornerShape(radius))
    .background(SslColors.GlassFill)
    .border(1.dp, SslColors.GlassBorder, RoundedCornerShape(radius))

val SslPrimaryButtonBrush = Brush.linearGradient(listOf(SslColors.Blue, SslColors.BlueDeep))
