package com.fabsimple.app.components.layout

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabsimple.app.navigation.NAV_SECTIONS
import com.fabsimple.app.navigation.NavItem
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer

/**
 * Sidebar drawer content displaying user role actions and profile footer.
 * Replicates Sidebar.tsx.
 */
@Composable
fun SidebarContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val session = remember { AppContainer.authRepository.getSession() }
    val userRole = session?.role ?: "worker"
    val userName = session?.name ?: "Worker"
    val initials = userName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(256.dp)
            .background(FabColors.Sidebar)
            .border(width = 1.dp, color = FabColors.SidebarBorder)
    ) {
        // Logo Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // Subtle square grid svg fallback logo
                Text("❖", color = Color.White, fontSize = 18.sp)
            }

            Text(
                text = "FabSimple",
                color = Color.White,
                style = FabType.cardTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Divider(color = FabColors.SidebarBorder)

        // Scrollable Nav Tree
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            NAV_SECTIONS.forEach { section ->
                // Filter items by role
                val visibleItems = section.items.filter { it.roles == null || it.roles.contains(userRole) }

                if (visibleItems.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Section Header
                        if (section.label != "Overview") {
                            Text(
                                text = section.label.uppercase(),
                                style = FabType.navSection,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        // Navigation links
                        visibleItems.forEach { item ->
                            val isSelected = currentRoute == item.key
                            val bg = if (isSelected) FabColors.SidebarItemActive else Color.Transparent
                            val textColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.65f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .clickable { onNavigate(item.key) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = mapIconToEmoji(item.icon),
                                    fontSize = 14.sp
                                )

                                Text(
                                    text = item.label,
                                    color = textColor,
                                    style = FabType.navItem,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (item.badge != null) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.badge,
                                            color = Color.White,
                                            style = FabType.buttonSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider(color = FabColors.SidebarBorder)

        // Footer User Profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4F46E5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    color = Color.White,
                    style = FabType.buttonSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = userRole,
                    color = Color.White.copy(alpha = 0.5f),
                    style = FabType.cardSub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onSignOut,
                modifier = Modifier.size(32.dp)
            ) {
                Text("🚪", fontSize = 14.sp)
            }
        }
    }
}

private fun mapIconToEmoji(icon: String): String = when (icon) {
    "LayoutDashboard" -> "📊"
    "Activity" -> "📈"
    "Calculator" -> "🧮"
    "FolderKanban" -> "📂"
    "FileText" -> "📄"
    "FileDiff" -> "🔀"
    "MessageSquare" -> "💬"
    "Wrench" -> "🔧"
    "Boxes" -> "📦"
    "ClipboardList" -> "📋"
    "Scissors" -> "✂️"
    "Upload" -> "📤"
    "Shield" -> "🛡️"
    "Flame" -> "🔥"
    "Paintbrush" -> "🖌️"
    "HardHat" -> "👷"
    "Award" -> "🏆"
    "AlertTriangle" -> "⚠️"
    "ShoppingCart" -> "🛒"
    "PackageCheck" -> "📥"
    "Warehouse" -> "🏢"
    "Thermometer" -> "🌡️"
    "Building" -> "🏗️"
    "Truck" -> "🚚"
    "QrCode" -> "🔳"
    "Users" -> "👥"
    "DollarSign" -> "💵"
    "Receipt" -> "🧾"
    "ScrollText" -> "📜"
    "Plug" -> "🔌"
    "CreditCard" -> "💳"
    else -> "📄"
}
