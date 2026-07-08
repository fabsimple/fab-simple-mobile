package com.fabsimple.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: Load Inter and JetBrains Mono from Compose Resources once font files are added.
// For now, use system defaults which are close enough for development.
val InterFontFamily = FontFamily.Default
val MonoFontFamily = FontFamily.Monospace

/**
 * FabSimple typography scale — matching the web app's font sizes and weights.
 * Body: Inter (system sans-serif fallback)
 * Mono: JetBrains Mono (system monospace fallback)
 */
object FabType {
    /** Page title — 22sp ExtraBold */
    val pageTitle = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        color = FabColors.TextPrimary
    )

    /** Section title — 16sp ExtraBold */
    val sectionTitle = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = FabColors.TextPrimary
    )

    /** Card title — 13sp Bold (matching .card-title) */
    val cardTitle = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = FabColors.TextPrimary
    )

    /** Card subtitle — 11sp Normal (matching .card-sub) */
    val cardSub = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = FabColors.TextMuted
    )

    /** Stat label — 11sp SemiBold uppercase (matching .stat-label) */
    val statLabel = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = FabColors.TextMuted
    )

    /** Stat value — 28sp ExtraBold mono (matching .stat-value) */
    val statValue = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 28.sp,
        color = FabColors.TextPrimary
    )

    /** Stat sub — 11sp Normal (matching .stat-sub) */
    val statSub = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        color = FabColors.TextMuted
    )

    /** Body text — 14sp Normal (matching body font-size) */
    val body = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        color = FabColors.TextSecondary
    )

    /** Small body — 13sp Normal */
    val bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.5.sp,
        color = FabColors.TextSecondary
    )

    /** Table header — 10sp Bold uppercase (matching thead th) */
    val tableHeader = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.6.sp,
        color = FabColors.TextMuted
    )

    /** Table cell — 13sp Normal (matching tbody td) */
    val tableCell = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = FabColors.TextSecondary
    )

    /** Mono cell — 12sp SemiBold (matching .td-mono) */
    val monoCell = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = FabColors.TextPrimary
    )

    /** Mono link — 12sp Bold primary (matching .td-link) */
    val monoLink = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = FabColors.Primary
    )

    /** Pill text — 11sp SemiBold (matching .pill) */
    val pill = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 17.6.sp,
    )

    /** Button text — 13sp Medium (matching .btn) */
    val button = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )

    /** Button small — 12sp SemiBold (matching .btn-sm) */
    val buttonSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )

    /** Label / field label — 12sp SemiBold (matching .fld label) */
    val label = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = FabColors.TextSecondary
    )

    /** Input text — 13sp Normal (matching .fld input) */
    val input = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = FabColors.TextPrimary
    )

    /** Modal title — 16sp ExtraBold (matching .modal-title) */
    val modalTitle = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = FabColors.TextPrimary
    )

    /** Nav section header — 11sp SemiBold uppercase */
    val navSection = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.sp,
        color = FabColors.TextMuted
    )

    /** Nav item — 13sp Medium */
    val navItem = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )

    /** Info cell label — 10sp Bold uppercase (matching .info-cell-label) */
    val infoCellLabel = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
        color = FabColors.TextMuted
    )

    /** Info cell value — 13sp SemiBold (matching .info-cell-value) */
    val infoCellValue = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = FabColors.TextPrimary
    )
}

/** Material3 Typography configured with FabSimple's font family */
val FabTypography = Typography(
    displayLarge = FabType.pageTitle,
    titleLarge = FabType.sectionTitle,
    titleMedium = FabType.cardTitle,
    titleSmall = FabType.cardSub,
    bodyLarge = FabType.body,
    bodyMedium = FabType.bodySmall,
    bodySmall = FabType.tableCell,
    labelLarge = FabType.button,
    labelMedium = FabType.label,
    labelSmall = FabType.pill,
)
