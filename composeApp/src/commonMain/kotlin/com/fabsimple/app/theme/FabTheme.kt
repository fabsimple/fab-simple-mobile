package com.fabsimple.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Extended Colors (beyond Material3 scheme) ───
data class FabExtendedColors(
    val cardBackground: Color,
    val mutedBackground: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textFaint: Color,
    val border: Color,
    val borderStrong: Color,
    val sidebar: Color,
    val sidebarDark: Color,
)

val LocalFabExtendedColors = staticCompositionLocalOf {
    FabExtendedColors(
        cardBackground = FabColors.CardBackground,
        mutedBackground = FabColors.MutedBackground,
        textSecondary = FabColors.TextSecondary,
        textMuted = FabColors.TextMuted,
        textFaint = FabColors.TextFaint,
        border = FabColors.Border,
        borderStrong = FabColors.BorderStrong,
        sidebar = FabColors.Sidebar,
        sidebarDark = FabColors.SidebarDark,
    )
}

private val LightColorScheme = lightColorScheme(
    primary = FabColors.Primary,
    onPrimary = Color.White,
    primaryContainer = FabColors.PrimaryBg,
    onPrimaryContainer = FabColors.Primary,
    secondary = FabColors.Blue,
    onSecondary = Color.White,
    background = FabColors.Background,
    onBackground = FabColors.TextPrimary,
    surface = FabColors.CardBackground,
    onSurface = FabColors.TextPrimary,
    surfaceVariant = FabColors.MutedBackground,
    onSurfaceVariant = FabColors.TextSecondary,
    outline = FabColors.Border,
    outlineVariant = FabColors.BorderStrong,
    error = FabColors.Red,
    onError = Color.White,
    errorContainer = FabColors.RedBg,
    onErrorContainer = FabColors.Red,
)

private val DarkColorScheme = darkColorScheme(
    primary = FabColors.Primary,
    onPrimary = Color.White,
    primaryContainer = FabColors.Indigo,
    onPrimaryContainer = FabColors.IndigoLight,
    secondary = Color(0xFF60A5FA),
    onSecondary = Color.White,
    background = FabColors.WorkerBg,
    onBackground = FabColors.WorkerText,
    surface = FabColors.WorkerCard,
    onSurface = FabColors.WorkerText,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = FabColors.WorkerMuted,
    outline = FabColors.WorkerBorder,
    outlineVariant = Color(0xFF475569),
    error = Color(0xFFF87171),
    onError = Color.White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFF87171),
)

private val LightExtendedColors = FabExtendedColors(
    cardBackground = FabColors.CardBackground,
    mutedBackground = FabColors.MutedBackground,
    textSecondary = FabColors.TextSecondary,
    textMuted = FabColors.TextMuted,
    textFaint = FabColors.TextFaint,
    border = FabColors.Border,
    borderStrong = FabColors.BorderStrong,
    sidebar = FabColors.Sidebar,
    sidebarDark = FabColors.SidebarDark,
)

private val DarkExtendedColors = FabExtendedColors(
    cardBackground = FabColors.WorkerCard,
    mutedBackground = Color(0xFF1E293B),
    textSecondary = FabColors.WorkerMuted,
    textMuted = Color(0xFF64748B),
    textFaint = Color(0xFF475569),
    border = FabColors.WorkerBorder,
    borderStrong = Color(0xFF475569),
    sidebar = FabColors.WorkerBg,
    sidebarDark = Color(0xFF020617),
)

/**
 * FabSimple application theme.
 *
 * @param darkTheme `true` for worker dark mode, `false` for dashboard light mode.
 */
@Composable
fun FabSimpleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors
    val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors

    CompositionLocalProvider(
        LocalFabExtendedColors provides extendedColors,
        LocalStatusColors provides statusColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FabTypography,
            shapes = FabMaterialShapes,
            content = content
        )
    }
}

/** Convenience accessor for extended colors inside @Composable */
object FabTheme {
    val extendedColors: FabExtendedColors
        @Composable get() = LocalFabExtendedColors.current

    val statusColors: Map<String, StatusColorToken>
        @Composable get() = LocalStatusColors.current
}
