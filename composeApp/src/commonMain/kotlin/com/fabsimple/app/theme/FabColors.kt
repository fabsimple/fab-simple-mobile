package com.fabsimple.app.theme

import androidx.compose.ui.graphics.Color

/**
 * FabSimple design tokens — faithful port of globals.css :root variables.
 * Every color is mapped 1:1 from the CSS custom properties.
 */
object FabColors {
    // ─── Brand (Steel Blue) ───
    val Primary = Color(0xFF185FA5)          // --primary
    val PrimaryDark = Color(0xFF14508A)      // --primary-2
    val PrimaryBg = Color(0xFFE8F0F7)        // --primary-bg
    val PrimaryBorder = Color(0xFFC1D6EA)    // --primary-bd

    // ─── Surfaces ───
    val Background = Color(0xFFF0F4F8)       // --bg (Fog Blue)
    val CardBackground = Color(0xFFFFFFFF)   // --bg-card
    val MutedBackground = Color(0xFFE2E8F0)  // --bg-muted

    // ─── Text ───
    val TextPrimary = Color(0xFF1A3A5C)      // --text (Navy)
    val TextSecondary = Color(0xFF374151)     // --text-2
    val TextMuted = Color(0xFF6B7280)        // --muted
    val TextFaint = Color(0xFF9CA3AF)        // --faint

    // ─── Borders ───
    val Border = Color(0xFFE5E7EB)           // --border
    val BorderStrong = Color(0xFFD1D5DB)     // --border-2

    // ─── Sidebar / Navigation (Navy) ───
    val Sidebar = Color(0xFF1A3A5C)          // --sidebar
    val SidebarDark = Color(0xFF11263C)      // --sidebar-2
    val SidebarBorder = Color(0x1AFFFFFF)    // --sidebar-border (white 10%)
    val SidebarItem = Color(0xA6FFFFFF)      // --sidebar-item (white 65%)
    val SidebarItemHover = Color(0x1AFFFFFF) // --sidebar-item-hover (white 10%)
    val SidebarItemActive = Color(0x26FFFFFF)// --sidebar-item-active (white 15%)

    // ─── Worker Dark Mode ───
    val WorkerBg = Color(0xFF0B1120)
    val WorkerCard = Color(0xFF1E293B)
    val WorkerText = Color(0xFFF8FAFC)
    val WorkerMuted = Color(0xFF94A3B8)
    val WorkerBorder = Color(0xFF334155)
    val WorkerToolbar = Color(0xFF0F172A)

    // ─── Status: Green (Success / Complete / Won / Approved) ───
    val Green = Color(0xFF10B981)
    val GreenBg = Color(0xFFECFDF5)
    val GreenBorder = Color(0xFFA7F3D0)

    // ─── Status: Amber (Warning / Pending) ───
    val Amber = Color(0xFFF59E0B)
    val AmberBg = Color(0xFFFFFBEB)
    val AmberBorder = Color(0xFFFDE68A)
    val AmberDark = Color(0xFFD97706)

    // ─── Status: Red (Error / Hold / Fail / Overdue) ───
    val Red = Color(0xFFEF4444)
    val RedBg = Color(0xFFFEF2F2)
    val RedBorder = Color(0xFFFECACA)

    // ─── Status: Blue (Info / Welding) ───
    val Blue = Color(0xFF3B82F6)
    val BlueBg = Color(0xFFEFF6FF)
    val BlueBorder = Color(0xFFBFDBFE)

    // ─── Status: Violet (Painting) ───
    val Violet = Color(0xFF8B5CF6)
    val VioletBg = Color(0xFFF5F3FF)
    val VioletBorder = Color(0xFFDDD6FE)

    // ─── Status: Teal (Shipped / Delivered) ───
    val Teal = Color(0xFF14B8A6)
    val TealBg = Color(0xFFF0FDFA)
    val TealBorder = Color(0xFF99F6E4)

    // ─── Status: Orange (Cutting) ───
    val Orange = Color(0xFFF97316)
    val OrangeBg = Color(0xFFFFF7ED)
    val OrangeBorder = Color(0xFFFED7AA)

    // ─── Status: Slate (Not Started / Queued) ───
    val Slate = Color(0xFF475569)
    val SlateBg = Color(0xFFF8FAFC)
    val SlateBorder = Color(0xFFCBD5E1)

    // ─── Indigo (Accent / Worker CTA) ───
    val Indigo = Color(0xFF4F46E5)
    val IndigoLight = Color(0xFF818CF8)
    val IndigoBg = Color(0x266366F1)   // 15% opacity

    // ─── Shadows (approximated as elevation in Compose) ───
    // shadow-sm → 1dp, shadow → 2dp, shadow-md → 4dp, shadow-lg → 8dp
}
