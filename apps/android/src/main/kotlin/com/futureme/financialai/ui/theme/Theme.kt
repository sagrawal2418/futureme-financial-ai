package com.futureme.financialai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.futureme.shared.design.FutureMeDesignTokens

private val SharedColors = FutureMeDesignTokens.current.colors

val Forest = SharedColors.brand.toComposeColor()
val Mint = SharedColors.accent.toComposeColor()
val Positive = SharedColors.positive.toComposeColor()
val Warning = SharedColors.warning.toComposeColor()

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    secondary = Positive,
    onSecondary = Color.White,
    background = SharedColors.canvasLight.toComposeColor(),
    onBackground = Color(0xFF11251F),
    surface = SharedColors.surfaceLight.toComposeColor(),
    onSurface = Color(0xFF17211D),
    surfaceVariant = Color(0xFFEDF6F1),
    onSurfaceVariant = Color(0xFF65736C),
    outline = Color(0xFFDDE5DF),
    error = Warning,
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Color(0xFF10251E),
    secondary = Color(0xFF61C99E),
    onSecondary = Color(0xFF0B2118),
    background = SharedColors.canvasDark.toComposeColor(),
    onBackground = Color(0xFFEDF7F1),
    surface = SharedColors.surfaceDark.toComposeColor(),
    onSurface = Color(0xFFEDF7F1),
    surfaceVariant = Color(0xFF1A2A23),
    onSurfaceVariant = Color(0xFFB5C4BD),
    outline = Color(0xFF2A3B34),
    error = Color(0xFFFFB4A4),
)

private val FutureMeTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.1.sp,
    ),
)

@Composable
fun FutureMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = FutureMeTypography,
        content = content,
    )
}

private fun String.toComposeColor(): Color {
    val rgb = removePrefix("#").toLong(16)
    return Color(0xFF000000 or rgb)
}
