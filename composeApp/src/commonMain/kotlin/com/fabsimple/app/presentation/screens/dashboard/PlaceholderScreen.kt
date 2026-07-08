package com.fabsimple.app.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.*
import com.fabsimple.app.components.layout.DrawerScaffold
import com.fabsimple.app.presentation.screens.auth.LoginScreen
import com.fabsimple.app.presentation.screens.utilities.CutOptimizerScreen
import com.fabsimple.app.presentation.screens.utilities.CopilotScreen
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer

/**
 * A beautiful, dynamic screen that serves as a high-fidelity mock/placeholder
 * for any dashboard drawer routes that don't have a dedicated implementation yet.
 * It displays category-appropriate metrics and datatables.
 */
class PlaceholderScreen(private val routeKey: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val (title, columns, rows, metrics) = getMockData(routeKey)

        DrawerScaffold(
            title = title,
            currentRoute = routeKey,
            onNavigate = { route ->
                if (route == "dashboard") {
                    navigator.replaceAll(DashboardScreen())
                } else if (route == "cut-list") {
                    navigator.replaceAll(CutOptimizerScreen())
                } else if (route == "copilot") {
                    navigator.replaceAll(CopilotScreen())
                } else if (route == "qr-codes") {
                    navigator.replaceAll(com.fabsimple.app.presentation.screens.dashboard.QrCodesScreen())
                } else if (route != routeKey) {
                    navigator.replaceAll(PlaceholderScreen(route))
                }
            },
            onSignOut = {
                AppContainer.authRepository.signOut()
                navigator.replaceAll(LoginScreen())
            },
            onSearchClick = {}
        ) {
            PageWrapper(
                title = title,
                scrollable = true
            ) {
                // Render custom category metrics
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    metrics.forEach { metric ->
                        StatCard(
                            title = metric.title,
                            value = metric.value,
                            subtext = metric.change,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Render section description alert
                AlertBanner(
                    message = "You are viewing the simulated live production data for $title. Local changes will be queued for offline synchronization.",
                    variant = AlertVariant.Info,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Render dynamic DataTable
                Text(
                    text = "Active Logs",
                    style = FabType.cardTitle,
                    color = FabColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                val dataColumns = columns.mapIndexed { idx, colTitle ->
                    DataColumn<List<String>>(
                        header = colTitle,
                        width = 120.dp,
                        cellContent = { rowData ->
                            val cellText = rowData.getOrNull(idx) ?: ""
                            if (colTitle == "Status") {
                                StatusPill(status = cellText)
                            } else {
                                Text(
                                    text = cellText,
                                    style = FabType.bodySmall,
                                    color = FabColors.TextSecondary
                                )
                            }
                        }
                    )
                }

                DataTable(
                    columns = dataColumns,
                    items = rows
                )
            }
        }
    }

    private data class MockData(
        val title: String,
        val columns: List<String>,
        val rows: List<List<String>>,
        val metrics: List<MetricItem>
    )

    private data class MetricItem(
        val title: String,
        val value: String,
        val change: String
    )

    private fun getMockData(key: String): MockData {
        return when (key) {
            "projects" -> MockData(
                title = "Projects",
                columns = listOf("Code", "Name", "Location", "Client", "Status"),
                rows = listOf(
                    listOf("PRJ-401", "Gateway Center", "Oakland, CA", "Swinerton", "active"),
                    listOf("PRJ-402", "Intel Fab D1X", "Hillsboro, OR", "Hoffman", "welding"),
                    listOf("PRJ-403", "Apex Warehouse", "Reno, NV", "Clayco", "painting"),
                    listOf("PRJ-404", "BioMed Lab Suite", "San Diego, CA", "DPR", "hold")
                ),
                metrics = listOf(
                    MetricItem("Active Bids", "4 Projects", "+2 this month"),
                    MetricItem("Total Volume", "1,240 Tons", "AISC Standard"),
                    MetricItem("Avg Progress", "76%", "+5.4% improvement")
                )
            )
            "drawings" -> MockData(
                title = "Drawings",
                columns = listOf("Sheet No", "Title", "Rev", "Status", "Last Updated"),
                rows = listOf(
                    listOf("S-101", "Anchor Bolt Plan", "Rev 2", "active", "2 hours ago"),
                    listOf("S-102", "Foundation Details", "Rev 1", "active", "1 day ago"),
                    listOf("S-201", "Framing Plan Level 1", "Rev 3", "hold", "3 hours ago"),
                    listOf("S-301", "Column Schedule", "Rev 0", "won", "4 days ago")
                ),
                metrics = listOf(
                    MetricItem("Total Drawings", "342 Sheets", "AISC Rev Checked"),
                    MetricItem("Pending Signoff", "12 Sheets", "Awaiting Eng Approval"),
                    MetricItem("Superseded Sheets", "45 Sheets", "Auto-archived")
                )
            )
            "parts" -> MockData(
                title = "Parts & Materials",
                columns = listOf("Mark", "Description", "Weight", "Qty", "Status"),
                rows = listOf(
                    listOf("1B1", "W16x31 x 24'-6\"", "760 lbs", "12", "active"),
                    listOf("2C3", "W14x90 x 32'-0\"", "2,880 lbs", "8", "welding"),
                    listOf("P104", "PL 1/2\" x 8\" x 8\"", "9 lbs", "120", "painting"),
                    listOf("G-1A", "WT6x15 x 12'-4\"", "185 lbs", "24", "active")
                ),
                metrics = listOf(
                    MetricItem("Estimated Parts", "8,420 Items", "98.4% Accuracy"),
                    MetricItem("Processed Parts", "3,240 Items", "By Shop Shift"),
                    MetricItem("Passed inspection", "4,180 Items", "AISC QC compliant")
                )
            )
            "daily-log" -> MockData(
                title = "Daily Production Log",
                columns = listOf("Date", "Shift", "Station", "Operator", "Status"),
                rows = listOf(
                    listOf("2026-07-04", "Day Shift", "Welding Cell 2", "Vinay Patel", "active"),
                    listOf("2026-07-04", "Night Shift", "Fitter Station 1", "Linda Chen", "active"),
                    listOf("2026-07-03", "Day Shift", "Paint Shop Bay A", "Sarah Mitchell", "won"),
                    listOf("2026-07-03", "Night Shift", "Detail Station 4", "Rachel Kim", "won")
                ),
                metrics = listOf(
                    MetricItem("Today's Output", "45.2 Tons", "92% Efficiency"),
                    MetricItem("Active Welder Cells", "6 stations", "Max capacity"),
                    MetricItem("Pending Log Submits", "1 log", "Needs Review")
                )
            )
            "billing" -> MockData(
                title = "Billing Applications",
                columns = listOf("App No", "Period Ending", "Scheduled Value", "Total Completed", "Status"),
                rows = listOf(
                    listOf("App 05", "2026-07-31", "$124,500.00", "$98,400.00", "active"),
                    listOf("App 04", "2026-06-30", "$152,000.00", "$152,000.00", "won"),
                    listOf("App 03", "2026-05-31", "$95,000.00", "$95,000.00", "won")
                ),
                metrics = listOf(
                    MetricItem("Total Billed", "$371,500.00", "AIA G702 standard"),
                    MetricItem("Retainage Held", "$37,150.00", "10.0% constant"),
                    MetricItem("Pending Approval", "$98,400.00", "Review period")
                )
            )
            else -> MockData(
                title = key.replace("-", " ").replaceFirstChar { it.uppercase() },
                columns = listOf("ID", "Name", "Created By", "Timestamp", "Status"),
                rows = listOf(
                    listOf("LOG-001", "$key Record 1", "System", "10 min ago", "active"),
                    listOf("LOG-002", "$key Record 2", "System", "2 hours ago", "active"),
                    listOf("LOG-003", "$key Record 3", "System", "1 day ago", "won")
                ),
                metrics = listOf(
                    MetricItem("Active entries", "24 entries", "+2 today"),
                    MetricItem("System Health", "Connected", "All synced"),
                    MetricItem("Sync status", "Up-to-date", "Zero latency")
                )
            )
        }
    }
}
