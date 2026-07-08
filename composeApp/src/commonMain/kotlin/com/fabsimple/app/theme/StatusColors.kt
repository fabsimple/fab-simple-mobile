package com.fabsimple.app.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * StatusColorToken maps a semantic status string to its three-color family
 * (main color, background, border) — matching the 16 `.pill-*` CSS classes.
 */
data class StatusColorToken(
    val main: Color,
    val bg: Color,
    val border: Color
)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

/**
 * Light-mode status color mappings.
 * Keys are lowercase status strings that the API returns.
 */
val LightStatusColors: Map<String, StatusColorToken> = mapOf(
    // Green family — complete, approved, won, success, pass, ok, closed
    "complete" to StatusColorToken(FabColors.Green, FabColors.GreenBg, FabColors.GreenBorder),
    "completed" to StatusColorToken(FabColors.Green, FabColors.GreenBg, FabColors.GreenBorder),
    "approved" to StatusColorToken(FabColors.Green, FabColors.GreenBg, FabColors.GreenBorder),
    "won" to StatusColorToken(FabColors.Green, FabColors.GreenBg, FabColors.GreenBorder),
    "success" to StatusColorToken(FabColors.Green, FabColors.GreenBg, FabColors.GreenBorder),
    "pass" to StatusColorToken(FabColors.Green, FabColors.GreenBg, FabColors.GreenBorder),
    "ok" to StatusColorToken(FabColors.Green, FabColors.GreenBg, FabColors.GreenBorder),
    "closed" to StatusColorToken(FabColors.Green, FabColors.GreenBg, FabColors.GreenBorder),

    // Amber family — pending, warning, on_hold, hold, review
    "pending" to StatusColorToken(FabColors.Amber, FabColors.AmberBg, FabColors.AmberBorder),
    "warning" to StatusColorToken(FabColors.Amber, FabColors.AmberBg, FabColors.AmberBorder),
    "on_hold" to StatusColorToken(FabColors.Amber, FabColors.AmberBg, FabColors.AmberBorder),
    "hold" to StatusColorToken(FabColors.Amber, FabColors.AmberBg, FabColors.AmberBorder),
    "review" to StatusColorToken(FabColors.Amber, FabColors.AmberBg, FabColors.AmberBorder),

    // Red family — error, fail, overdue, rejected, cancelled
    "error" to StatusColorToken(FabColors.Red, FabColors.RedBg, FabColors.RedBorder),
    "fail" to StatusColorToken(FabColors.Red, FabColors.RedBg, FabColors.RedBorder),
    "overdue" to StatusColorToken(FabColors.Red, FabColors.RedBg, FabColors.RedBorder),
    "rejected" to StatusColorToken(FabColors.Red, FabColors.RedBg, FabColors.RedBorder),
    "cancelled" to StatusColorToken(FabColors.Red, FabColors.RedBg, FabColors.RedBorder),

    // Blue family — active, info, in_progress, welding, fit_up
    "active" to StatusColorToken(FabColors.Blue, FabColors.BlueBg, FabColors.BlueBorder),
    "in_progress" to StatusColorToken(FabColors.Blue, FabColors.BlueBg, FabColors.BlueBorder),
    "info" to StatusColorToken(FabColors.Blue, FabColors.BlueBg, FabColors.BlueBorder),
    "welding" to StatusColorToken(FabColors.Blue, FabColors.BlueBg, FabColors.BlueBorder),
    "fit_up" to StatusColorToken(FabColors.Blue, FabColors.BlueBg, FabColors.BlueBorder),

    // Violet family — painting
    "painting" to StatusColorToken(FabColors.Violet, FabColors.VioletBg, FabColors.VioletBorder),

    // Teal family — shipped, delivered
    "shipped" to StatusColorToken(FabColors.Teal, FabColors.TealBg, FabColors.TealBorder),
    "delivered" to StatusColorToken(FabColors.Teal, FabColors.TealBg, FabColors.TealBorder),

    // Orange family — cutting, open
    "cutting" to StatusColorToken(FabColors.Orange, FabColors.OrangeBg, FabColors.OrangeBorder),
    "open" to StatusColorToken(FabColors.Orange, FabColors.OrangeBg, FabColors.OrangeBorder),

    // Slate family — not_started, queued, draft, planning
    "not_started" to StatusColorToken(FabColors.Slate, FabColors.SlateBg, FabColors.SlateBorder),
    "queued" to StatusColorToken(FabColors.Slate, FabColors.SlateBg, FabColors.SlateBorder),
    "draft" to StatusColorToken(FabColors.Slate, FabColors.SlateBg, FabColors.SlateBorder),
    "planning" to StatusColorToken(FabColors.Slate, FabColors.SlateBg, FabColors.SlateBorder),

    // Primary family — primary, bidding
    "bidding" to StatusColorToken(FabColors.Primary, FabColors.PrimaryBg, FabColors.PrimaryBorder),
)

/** Dark-mode status colors for worker view */
val DarkStatusColors: Map<String, StatusColorToken> = mapOf(
    "complete" to StatusColorToken(Color(0xFF34D399), Color(0xFF064E3B), Color(0xFF047857)),
    "completed" to StatusColorToken(Color(0xFF34D399), Color(0xFF064E3B), Color(0xFF047857)),
    "approved" to StatusColorToken(Color(0xFF34D399), Color(0xFF064E3B), Color(0xFF047857)),
    "won" to StatusColorToken(Color(0xFF34D399), Color(0xFF064E3B), Color(0xFF047857)),
    "success" to StatusColorToken(Color(0xFF34D399), Color(0xFF064E3B), Color(0xFF047857)),
    "pass" to StatusColorToken(Color(0xFF34D399), Color(0xFF064E3B), Color(0xFF047857)),
    "ok" to StatusColorToken(Color(0xFF34D399), Color(0xFF064E3B), Color(0xFF047857)),
    "closed" to StatusColorToken(Color(0xFF34D399), Color(0xFF064E3B), Color(0xFF047857)),

    "pending" to StatusColorToken(Color(0xFFFBBF24), Color(0xFF78350F), Color(0xFF92400E)),
    "warning" to StatusColorToken(Color(0xFFFBBF24), Color(0xFF78350F), Color(0xFF92400E)),
    "on_hold" to StatusColorToken(Color(0xFFFBBF24), Color(0xFF78350F), Color(0xFF92400E)),
    "hold" to StatusColorToken(Color(0xFFFBBF24), Color(0xFF78350F), Color(0xFF92400E)),
    "review" to StatusColorToken(Color(0xFFFBBF24), Color(0xFF78350F), Color(0xFF92400E)),

    "error" to StatusColorToken(Color(0xFFF87171), Color(0xFF7F1D1D), Color(0xFFB91C1C)),
    "fail" to StatusColorToken(Color(0xFFF87171), Color(0xFF7F1D1D), Color(0xFFB91C1C)),
    "overdue" to StatusColorToken(Color(0xFFF87171), Color(0xFF7F1D1D), Color(0xFFB91C1C)),
    "rejected" to StatusColorToken(Color(0xFFF87171), Color(0xFF7F1D1D), Color(0xFFB91C1C)),
    "cancelled" to StatusColorToken(Color(0xFFF87171), Color(0xFF7F1D1D), Color(0xFFB91C1C)),

    "active" to StatusColorToken(Color(0xFF60A5FA), Color(0xFF1E3A8A), Color(0xFF1D4ED8)),
    "in_progress" to StatusColorToken(Color(0xFF60A5FA), Color(0xFF1E3A8A), Color(0xFF1D4ED8)),
    "info" to StatusColorToken(Color(0xFF60A5FA), Color(0xFF1E3A8A), Color(0xFF1D4ED8)),
    "welding" to StatusColorToken(Color(0xFF60A5FA), Color(0xFF1E3A8A), Color(0xFF1D4ED8)),
    "fit_up" to StatusColorToken(Color(0xFF60A5FA), Color(0xFF1E3A8A), Color(0xFF1D4ED8)),

    "painting" to StatusColorToken(Color(0xFFA78BFA), Color(0xFF4C1D95), Color(0xFF6D28D9)),
    "shipped" to StatusColorToken(Color(0xFF2DD4BF), Color(0xFF115E59), Color(0xFF0F766E)),
    "delivered" to StatusColorToken(Color(0xFF2DD4BF), Color(0xFF115E59), Color(0xFF0F766E)),
    "cutting" to StatusColorToken(Color(0xFFFB923C), Color(0xFF7C2D12), Color(0xFF9A3412)),
    "open" to StatusColorToken(Color(0xFFFB923C), Color(0xFF7C2D12), Color(0xFF9A3412)),

    "not_started" to StatusColorToken(Color(0xFF94A3B8), Color(0xFF1E293B), Color(0xFF334155)),
    "queued" to StatusColorToken(Color(0xFF94A3B8), Color(0xFF1E293B), Color(0xFF334155)),
    "draft" to StatusColorToken(Color(0xFF94A3B8), Color(0xFF1E293B), Color(0xFF334155)),
    "planning" to StatusColorToken(Color(0xFF94A3B8), Color(0xFF1E293B), Color(0xFF334155)),

    "bidding" to StatusColorToken(Color(0xFF818CF8), Color(0xFF312E81), Color(0xFF4338CA)),
)

/** Resolve a status string to its color token. Falls back to Slate. */
fun Map<String, StatusColorToken>.resolve(status: String): StatusColorToken {
    return this[status.lowercase()]
        ?: this[status.lowercase().replace(" ", "_")]
        ?: StatusColorToken(FabColors.Slate, FabColors.SlateBg, FabColors.SlateBorder)
}
