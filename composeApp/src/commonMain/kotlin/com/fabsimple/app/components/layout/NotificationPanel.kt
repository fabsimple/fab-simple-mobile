package com.fabsimple.app.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Popup
import com.fabsimple.app.components.AlertBanner
import com.fabsimple.app.components.AlertVariant
import com.fabsimple.app.components.StatusPill
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.Notification
import kotlinx.coroutines.launch

/**
 * Notification Panel dropdown menu.
 * Replicates NotifPanel.tsx in the layout folder.
 */
@Composable
fun NotificationPanel(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsList by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun loadNotifications() {
        loading = true
        coroutineScope.launch {
            try {
                // Fetch notifications from the backend
                val list: List<Notification> = AppContainer.apiClient.get("/notifications")
                notificationsList = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    val unreadCount = notificationsList.count { !it.read }

    Card(
        modifier = modifier
            .width(320.dp)
            .padding(top = 8.dp)
            .border(1.dp, FabColors.Border, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = FabColors.Border)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Notifications",
                        style = FabType.cardTitle
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = "  ($unreadCount unread)",
                            style = FabType.cardSub,
                            fontFamily = MonoFontFamily
                        )
                    }
                }

                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                AppContainer.apiClient.post<Unit>("/notifications/read-all")
                                loadNotifications()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    enabled = unreadCount > 0,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Mark all read",
                        style = FabType.buttonSmall.copy(color = FabColors.Primary)
                    )
                }
            }

            // Body List
            Box(modifier = Modifier.heightIn(max = 380.dp)) {
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FabColors.Primary, modifier = Modifier.size(20.dp))
                    }
                } else if (notificationsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "You're all caught up.",
                            style = FabType.cardSub
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(notificationsList) { n ->
                            val indicatorColor = when {
                                n.title.contains("NCR", ignoreCase = true) || n.title.contains("QC", ignoreCase = true) -> FabColors.Red
                                n.title.contains("Expiry", ignoreCase = true) || n.title.contains("Low", ignoreCase = true) -> FabColors.Amber
                                else -> FabColors.Primary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (n.read) Color.Transparent else FabColors.PrimaryBg.copy(alpha = 0.3f)
                                    )
                                    .clickable {
                                        // Mark single read
                                        coroutineScope.launch {
                                            try {
                                                AppContainer.apiClient.patch<Unit>("/notifications/${n.id}", mapOf("read" to true))
                                                loadNotifications()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                    .border(width = 1.dp, color = FabColors.Border)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Side color indicator
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(40.dp)
                                        .background(indicatorColor)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = n.title,
                                        style = FabType.buttonSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = n.body ?: "",
                                        style = FabType.cardSub,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
