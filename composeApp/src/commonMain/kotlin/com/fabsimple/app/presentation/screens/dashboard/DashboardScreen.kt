package com.fabsimple.app.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import cafe.adriel.voyager.core.screen.Screen
import com.fabsimple.app.presentation.screens.auth.LoginScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.*
import com.fabsimple.app.components.layout.DrawerScaffold
import com.fabsimple.app.theme.*
import com.fabsimple.shared.data.network.DashboardData
import com.fabsimple.shared.di.AppContainer
import kotlinx.coroutines.launch

class DashboardScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        var dashboardData by remember { mutableStateOf<DashboardData?>(null) }
        var loading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        var copilotOpen by remember { mutableStateOf(false) }
        var copilotInput by remember { mutableStateOf("") }
        var copilotMessages by remember { mutableStateOf<List<com.fabsimple.shared.domain.model.ChatMessage>>(emptyList()) }
        var copilotBusy by remember { mutableStateOf(false) }
        var copilotError by remember { mutableStateOf<String?>(null) }

        fun loadDashboard() {
            loading = true
            coroutineScope.launch {
                try {
                    dashboardData = AppContainer.dashboardRepository.getDashboardData()
                } catch (e: Exception) {
                    error = e.message ?: "Failed to load dashboard data"
                } finally {
                    loading = false
                }
            }
        }

        LaunchedEffect(Unit) {
            loadDashboard()
        }

        fun sendCopilotQuery(content: String) {
            if (content.isBlank() || copilotBusy) return
            val next = copilotMessages + com.fabsimple.shared.domain.model.ChatMessage(role = "user", content = content)
            copilotMessages = next
            copilotInput = ""
            copilotBusy = true
            copilotError = null

            coroutineScope.launch {
                try {
                    val reply = AppContainer.copilotRepository.query(next, null)
                    copilotMessages = next + com.fabsimple.shared.domain.model.ChatMessage(role = "assistant", content = reply)
                } catch (e: Exception) {
                    copilotError = e.message ?: "Copilot query failed"
                } finally {
                    copilotBusy = false
                }
            }
        }

        DrawerScaffold(
            title = "Dashboard",
            currentRoute = "dashboard",
            onNavigate = { route ->
                if (route == "parts") {
                    navigator.replaceAll(com.fabsimple.app.presentation.screens.dashboard.PartsListScreen())
                } else if (route == "cut-list") {
                    navigator.replaceAll(com.fabsimple.app.presentation.screens.utilities.CutOptimizerScreen())
                } else if (route == "copilot") {
                    navigator.replaceAll(com.fabsimple.app.presentation.screens.utilities.CopilotScreen())
                } else if (route == "qr-codes") {
                    navigator.replaceAll(com.fabsimple.app.presentation.screens.dashboard.QrCodesScreen())
                } else if (route != "dashboard") {
                    navigator.replaceAll(com.fabsimple.app.presentation.screens.dashboard.PlaceholderScreen(route))
                }
            },
            onSignOut = {
                AppContainer.authRepository.signOut()
                navigator.replaceAll(LoginScreen())
            },
            onSearchClick = {
                // Open search dialog
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                PageWrapper(
                    title = "Dashboard",
                    scrollable = true
                ) {
                    if (loading) {
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = FabColors.Primary)
                        }
                    } else if (error != null) {
                        AlertBanner(message = error!!, variant = AlertVariant.Danger)
                    } else if (dashboardData == null) {
                        AlertBanner(message = "No dashboard data loaded", variant = AlertVariant.Warning)
                    } else {
                        val d = dashboardData!!

                        val completedCount = (d.parts_by_status["complete"] ?: 0) + (d.parts_by_status["shipped"] ?: 0)
                        val inProgressCount = (d.parts_by_status["in_progress"] ?: 0) + (d.parts_by_status["not_started"] ?: 0)
                        val shippedCount = d.parts_by_status["shipped"] ?: 0

                        // ─── 4 Top Stat Cards ───
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                title = "Total Parts",
                                value = d.total_parts.toString(),
                                subtext = "across ${d.projects.size} active projects",
                                borderColor = FabColors.Primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Completed Parts",
                                value = completedCount.toString(),
                                subtext = "${(d.total_weight / 2000.0).toInt()} tons total",
                                borderColor = FabColors.Green,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "In Production",
                                value = inProgressCount.toString(),
                                subtext = "${d.parts_by_status["in_progress"] ?: 0} active · ${d.parts_by_status["not_started"] ?: 0} queued",
                                borderColor = FabColors.Blue,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Shipped",
                                value = shippedCount.toString(),
                                subtext = "${d.open_change_orders.size} open change orders",
                                borderColor = FabColors.Violet,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ─── Project Progress & Feed Grid ───
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Left column: Project Progress Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, FabColors.Border, FabShapes.Card),
                                colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
                                shape = FabShapes.Card
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                    Text(
                                        text = "Project Progress",
                                        style = FabType.cardTitle
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    d.projects.take(5).forEach { p ->
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = p.name,
                                                    style = FabType.buttonSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${p.progress.toInt()}%",
                                                    style = FabType.monoCell,
                                                    color = FabColors.Primary
                                                )
                                            }

                                            ProgressBar(
                                                progress = (p.progress / 100.0).toFloat()
                                            )
                                        }
                                    }
                                }
                            }

                            // Right column: Live Activity Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, FabColors.Border, FabShapes.Card),
                                colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
                                shape = FabShapes.Card
                            ) {
                                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Live Activity",
                                            style = FabType.cardTitle
                                        )
                                        StatusPill(status = "live")
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))

                                    d.activity.take(5).forEach { act ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(Color(0xFF2563EB), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = (act.user_name ?: "?").take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${act.user_name ?: ""} ${act.action}",
                                                    style = FabType.bodySmall
                                                )
                                                val label = act.entity_label
                                                if (label != null) {
                                                    Text(
                                                        text = label,
                                                        style = FabType.cardSub,
                                                        color = FabColors.Primary,
                                                        fontFamily = MonoFontFamily
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ─── 4 Bottom Stat Alert Cards ───
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatCard(
                                title = "Open Change Orders",
                                value = d.open_change_orders.size.toString(),
                                borderColor = FabColors.Amber,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Open NCRs",
                                value = d.open_ncrs.size.toString(),
                                subtext = "QC sign-off required",
                                borderColor = FabColors.Red,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Open RFIs",
                                value = d.open_rfis.size.toString(),
                                subtext = "Awaiting response",
                                borderColor = FabColors.Amber,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Contract Value",
                                value = "$${(d.financial?.backlog ?: 0.0).toInt().toLocaleString()}",
                                borderColor = FabColors.Teal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // AI Copilot slide drawer overlays
                CopilotPanel(
                    open = copilotOpen,
                    messages = copilotMessages,
                    input = copilotInput,
                    onInputChange = { copilotInput = it },
                    onSend = { sendCopilotQuery(it) },
                    onClose = { copilotOpen = false },
                    busy = copilotBusy,
                    error = copilotError
                )

                // Floating Copilot Trigger
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    CopilotLauncher(
                        onClick = { copilotOpen = true }
                    )
                }
            }
        }
    }

    private fun Int.toLocaleString(): String {
        return this.toString().reversed().chunked(3).joinToString(",").reversed()
    }
}
