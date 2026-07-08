package com.fabsimple.app.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.components.ButtonVariant
import com.fabsimple.app.components.ButtonSize
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import kotlinx.coroutines.launch

/**
 * TopBar navigation header.
 * Replicates Topbar.tsx in the layout folder.
 */
@Composable
fun TopBar(
    title: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notifOpen by remember { mutableStateOf(false) }
    var unreadCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Poll unread count
    LaunchedEffect(notifOpen) {
        if (!notifOpen) {
            try {
                val list = AppContainer.apiClient.get<List<com.fabsimple.shared.domain.model.Notification>>("/notifications")
                unreadCount = list.count { !it.read }
            } catch (_: Exception) {}
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(FabTheme.extendedColors.cardBackground)
            .border(width = 1.dp, color = FabColors.Border)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mobile Hamburger Trigger
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(36.dp)
                .clip(FabShapes.Button)
                .border(1.dp, FabColors.Border, FabShapes.Button)
        ) {
            Text("☰", fontSize = 16.sp, color = FabColors.TextSecondary)
        }

        // Title
        Text(
            text = title,
            style = FabType.cardTitle,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.wrapContentWidth()
        )

        // Global Project Picker
        ProjectPicker(
            modifier = Modifier.weight(1f)
        )

        // Search Trigger
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .size(36.dp)
                .clip(FabShapes.Button)
                .border(1.dp, FabColors.Border, FabShapes.Button)
        ) {
            Text("🔍", fontSize = 14.sp, color = FabColors.TextMuted)
        }

        // Notification Bell & Panel Dropdown
        Box {
            IconButton(
                onClick = { notifOpen = !notifOpen },
                modifier = Modifier
                    .size(36.dp)
                    .clip(FabShapes.Button)
                    .border(1.dp, FabColors.Border, FabShapes.Button)
            ) {
                Box {
                    Text("🔔", fontSize = 14.sp, color = FabColors.TextMuted)
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(FabColors.Red, RoundedCornerShape(4.dp))
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }

            if (notifOpen) {
                Popup(
                    alignment = Alignment.BottomEnd,
                    onDismissRequest = { notifOpen = false }
                ) {
                    NotificationPanel(
                        onDismissRequest = { notifOpen = false }
                    )
                }
            }
        }
    }
}
