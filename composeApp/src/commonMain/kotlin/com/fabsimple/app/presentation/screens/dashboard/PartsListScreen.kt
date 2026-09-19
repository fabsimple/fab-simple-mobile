package com.fabsimple.app.presentation.screens.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.*
import com.fabsimple.app.components.layout.DrawerScaffold
import com.fabsimple.app.presentation.screens.auth.LoginScreen
import com.fabsimple.app.presentation.screens.utilities.CopilotScreen
import com.fabsimple.app.presentation.screens.utilities.CutOptimizerScreen
import com.fabsimple.app.state.GlobalProjectState
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.Part
import com.fabsimple.shared.domain.model.Project
import kotlinx.coroutines.launch

val STATUS_OPTIONS = listOf("not_started", "in_progress", "complete", "shipped", "on_hold")

class PartsListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        val activeProject by GlobalProjectState.currentProject.collectAsState()
        val selectedProjectId = activeProject.id

        var partsList by remember { mutableStateOf<List<Part>>(emptyList()) }
        var projectsList by remember { mutableStateOf<List<Project>>(emptyList()) }
        var selectedParts by remember { mutableStateOf<Set<String>>(emptySet()) }

        var searchInput by remember { mutableStateOf("") }
        var statusFilter by remember { mutableStateOf("") }
        var currentPage by remember { mutableStateOf(1) }
        var pageSize by remember { mutableStateOf(25) }

        var loading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var refreshTrigger by remember { mutableStateOf(0) }
        var bulkBusy by remember { mutableStateOf(false) }

        // Modal states
        var showNewPartModal by remember { mutableStateOf(false) }
        var editingPart by remember { mutableStateOf<Part?>(null) }
        var showCreatePoModal by remember { mutableStateOf(false) }
        var showImportBomModal by remember { mutableStateOf(false) }

        // Fetch parts & projects
        LaunchedEffect(selectedProjectId, refreshTrigger) {
            loading = true
            error = null
            currentPage = 1
            selectedParts = emptySet()
            try {
                partsList = AppContainer.partRepository.getParts(selectedProjectId)
                projectsList = AppContainer.projectRepository.getProjects()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load parts"
            } finally {
                loading = false
            }
        }

        // Filter parts
        val filteredParts = remember(partsList, statusFilter, searchInput) {
            var res = partsList
            if (statusFilter.isNotBlank()) {
                res = res.filter { it.status.lowercase() == statusFilter.lowercase() }
            }
            val q = searchInput.trim().lowercase()
            if (q.isNotBlank()) {
                res = res.filter { p ->
                    p.part_mark.lowercase().contains(q) ||
                    p.profile.lowercase().contains(q) ||
                    (p.assembly_mark ?: "").lowercase().contains(q) ||
                    (p.name ?: "").lowercase().contains(q) ||
                    (p.grade ?: "").lowercase().contains(q) ||
                    (p.heat_number ?: "").lowercase().contains(q)
                }
            }
            res
        }

        val totalPages = remember(filteredParts, pageSize) {
            if (pageSize <= 0) 1
            else (filteredParts.size + pageSize - 1) / pageSize.coerceAtLeast(1)
        }

        val currentPageClamped = currentPage.coerceIn(1, totalPages.coerceAtLeast(1))

        val visibleParts = remember(filteredParts, currentPageClamped, pageSize) {
            if (pageSize <= 0) filteredParts
            else {
                val start = (currentPageClamped - 1) * pageSize
                filteredParts.drop(start).take(pageSize)
            }
        }

        val isAllVisibleSelected = remember(visibleParts, selectedParts) {
            visibleParts.isNotEmpty() && visibleParts.all { selectedParts.contains(it.id) }
        }

        fun toggleSelectAllVisible() {
            selectedParts = if (isAllVisibleSelected) {
                selectedParts - visibleParts.map { it.id }.toSet()
            } else {
                selectedParts + visibleParts.map { it.id }.toSet()
            }
        }

        fun togglePartSelection(id: String) {
            selectedParts = if (selectedParts.contains(id)) selectedParts - id else selectedParts + id
        }

        fun bulkUpdateStatus(newStatus: String) {
            if (selectedParts.isEmpty() || bulkBusy) return
            bulkBusy = true
            coroutineScope.launch {
                try {
                    selectedParts.forEach { id ->
                        try {
                            AppContainer.partRepository.updatePartStatus(id, mapOf("status" to newStatus))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    selectedParts = emptySet()
                    refreshTrigger++
                } finally {
                    bulkBusy = false
                }
            }
        }

        DrawerScaffold(
            title = "Parts List",
            currentRoute = "parts",
            onNavigate = { route ->
                if (route == "dashboard") {
                    navigator.replaceAll(DashboardScreen())
                } else if (route == "cut-list") {
                    navigator.replaceAll(CutOptimizerScreen())
                } else if (route == "copilot") {
                    navigator.replaceAll(CopilotScreen())
                } else if (route == "qr-codes") {
                    navigator.replaceAll(QrCodesScreen())
                } else if (route != "parts") {
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
                title = "Parts",
                scrollable = true
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Parts List",
                            style = FabType.sectionTitle
                        )
                        Text(
                            text = "${filteredParts.size} part${if (filteredParts.size == 1) "" else "s"}${if (statusFilter.isNotBlank() || searchInput.isNotBlank() || selectedProjectId != null) " (filtered)" else ""}",
                            style = FabType.cardSub,
                            color = FabColors.TextMuted
                        )
                    }

                    // Right Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Input Box
                        Box(modifier = Modifier.width(200.dp)) {
                            FabTextField(
                                value = searchInput,
                                onValueChange = { searchInput = it; currentPage = 1 },
                                placeholder = "Search part marks…"
                            )
                        }

                        // Status Filter Dropdown button / selector
                        StatusFilterSelector(
                            selectedStatus = statusFilter,
                            onStatusSelected = { statusFilter = it; currentPage = 1 }
                        )

                        FabButton(
                            text = "🛒 Create PO",
                            onClick = { showCreatePoModal = true },
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Small,
                            enabled = selectedProjectId != null
                        )

                        FabButton(
                            text = "📄 Import BOM",
                            onClick = { showImportBomModal = true },
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Small
                        )

                        FabButton(
                            text = "+ New Part",
                            onClick = { showNewPartModal = true },
                            variant = ButtonVariant.Primary,
                            size = ButtonSize.Small
                        )
                    }
                }

                // Bulk Action Bar (shows when items are checked)
                if (selectedParts.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, FabColors.PrimaryBorder, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = FabColors.PrimaryBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedParts.size} part(s) selected",
                                style = FabType.buttonSmall.copy(color = FabColors.Primary, fontWeight = FontWeight.Bold)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Set Status:", style = FabType.cardSub, color = FabColors.TextMuted)

                                STATUS_OPTIONS.forEach { st ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(FabColors.CardBackground)
                                            .border(1.dp, FabColors.Border, RoundedCornerShape(4.dp))
                                            .clickable { bulkUpdateStatus(st) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = st.replace("_", " "),
                                            style = FabType.buttonSmall.copy(fontSize = 11.sp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Clear",
                                    style = FabType.buttonSmall.copy(color = FabColors.Primary),
                                    modifier = Modifier.clickable { selectedParts = emptySet() }.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FabColors.Primary)
                    }
                } else if (error != null) {
                    AlertBanner(message = error!!, variant = AlertVariant.Danger)
                } else if (visibleParts.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .border(1.dp, FabColors.Border, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = FabColors.CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(FabColors.PrimaryBg.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(26.dp)) {
                                    val w = size.width
                                    val h = size.height
                                    val strokeWidth = 2.dp.toPx()
                                    val strokeColor = Color(0xFF1E3A8A).copy(alpha = 0.7f)

                                    val path = androidx.compose.ui.graphics.Path().apply {
                                        moveTo(w * 0.15f, h * 0.35f)
                                        lineTo(w * 0.85f, h * 0.35f)
                                        lineTo(w * 0.85f, h * 0.75f)
                                        lineTo(w * 0.65f, h * 0.75f)
                                        lineTo(w * 0.60f, h * 0.88f)
                                        lineTo(w * 0.40f, h * 0.88f)
                                        lineTo(w * 0.35f, h * 0.75f)
                                        lineTo(w * 0.15f, h * 0.75f)
                                        close()
                                    }
                                    drawPath(
                                        path = path,
                                        color = strokeColor,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = strokeWidth
                                        )
                                    )
                                    drawLine(
                                        color = strokeColor,
                                        start = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.20f),
                                        end = androidx.compose.ui.geometry.Offset(w * 0.70f, h * 0.20f),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (partsList.isNotEmpty()) "No matching parts found" else "No parts yet",
                                style = FabType.cardTitle.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FabColors.TextPrimary
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (partsList.isNotEmpty())
                                    "Try adjusting your search query or status filter."
                                else
                                    "Import a Tekla / SDS2 BOM (KISS, CSV, XLSX), or add manually.",
                                style = FabType.body.copy(
                                    fontSize = 13.sp,
                                    color = FabColors.TextMuted
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Data Table Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, FabColors.Border, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Table Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FabTheme.extendedColors.mutedBackground.copy(alpha = 0.7f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isAllVisibleSelected,
                                    onCheckedChange = { toggleSelectAllVisible() },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                TableHeaderCell("QTY", modifier = Modifier.width(50.dp), align = TextAlign.Right)
                                TableHeaderCell("MARK", modifier = Modifier.weight(1.2f))
                                TableHeaderCell("PROFILE", modifier = Modifier.weight(1.2f))
                                TableHeaderCell("NAME", modifier = Modifier.weight(1f))
                                TableHeaderCell("LENGTH", modifier = Modifier.weight(1.1f), align = TextAlign.Right)
                                TableHeaderCell("GRADE", modifier = Modifier.width(70.dp))
                                TableHeaderCell("PART WEIGHT", modifier = Modifier.weight(1.1f), align = TextAlign.Right)
                                TableHeaderCell("HEAT #", modifier = Modifier.weight(1f))
                                TableHeaderCell("STATUS", modifier = Modifier.width(110.dp))
                            }

                            HorizontalDivider(color = FabColors.Border)

                            // Table Rows
                            visibleParts.forEachIndexed { index, part ->
                                val isChecked = selectedParts.contains(part.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isChecked) FabColors.PrimaryBg.copy(alpha = 0.4f) else Color.Transparent)
                                        .clickable { editingPart = part }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { togglePartSelection(part.id) },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = part.quantity.toString(),
                                        style = FabType.body.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                        modifier = Modifier.width(50.dp),
                                        textAlign = TextAlign.Right
                                    )
                                    Text(
                                        text = part.part_mark,
                                        style = FabType.body.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = FabColors.TextPrimary),
                                        modifier = Modifier.weight(1.2f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = part.profile,
                                        style = FabType.body.copy(fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.weight(1.2f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = part.name ?: "—",
                                        style = FabType.body.copy(color = FabColors.TextMuted),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = part.length ?: "—",
                                        style = FabType.body.copy(fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.weight(1.1f),
                                        textAlign = TextAlign.Right,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = part.grade ?: "—",
                                        style = FabType.body,
                                        modifier = Modifier.width(70.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (part.weight != null) "${part.weight} lb" else "—",
                                        style = FabType.body.copy(fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.weight(1.1f),
                                        textAlign = TextAlign.Right,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = part.heat_number ?: "—",
                                        style = FabType.body.copy(fontFamily = FontFamily.Monospace),
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Box(modifier = Modifier.width(110.dp)) {
                                        StatusPill(status = part.status, size = PillSize.Small)
                                    }
                                }

                                if (index < visibleParts.size - 1) {
                                    HorizontalDivider(color = FabColors.Border.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }

                // Footer Pagination Controls
                if (filteredParts.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page $currentPageClamped of $totalPages (${filteredParts.size} parts total)",
                            style = FabType.cardSub,
                            color = FabColors.TextMuted
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FabButton(
                                text = "‹ Prev",
                                onClick = { if (currentPageClamped > 1) currentPage-- },
                                variant = ButtonVariant.Secondary,
                                size = ButtonSize.Small,
                                enabled = currentPageClamped > 1
                            )

                            FabButton(
                                text = "Next ›",
                                onClick = { if (currentPageClamped < totalPages) currentPage++ },
                                variant = ButtonVariant.Secondary,
                                size = ButtonSize.Small,
                                enabled = currentPageClamped < totalPages
                            )

                            // Page size selector
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(25, 50, 100, -1).forEach { size ->
                                    val label = if (size == -1) "All" else size.toString()
                                    val isSel = pageSize == size
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSel) FabColors.Primary else FabTheme.extendedColors.mutedBackground)
                                            .clickable { pageSize = size; currentPage = 1 }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = FabType.buttonSmall.copy(
                                                color = if (isSel) Color.White else FabColors.TextPrimary,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Modals
        if (showNewPartModal) {
            PartFormDialog(
                title = "New Part",
                initialPart = null,
                projects = projectsList,
                defaultProjectId = selectedProjectId,
                onDismiss = { showNewPartModal = false },
                onSave = { payload ->
                    coroutineScope.launch {
                        try {
                            AppContainer.apiClient.post<Unit>("/parts", payload)
                            showNewPartModal = false
                            refreshTrigger++
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }

        editingPart?.let { partToEdit ->
            PartFormDialog(
                title = "Edit Part — ${partToEdit.part_mark}",
                initialPart = partToEdit,
                projects = projectsList,
                defaultProjectId = selectedProjectId,
                onDismiss = { editingPart = null },
                onSave = { payload ->
                    coroutineScope.launch {
                        try {
                            AppContainer.apiClient.patch<Unit>("/parts/${partToEdit.id}", payload)
                            editingPart = null
                            refreshTrigger++
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }

        if (showCreatePoModal) {
            CreatePoDialog(
                projectId = selectedProjectId ?: projectsList.firstOrNull()?.id ?: "",
                parts = partsList,
                onDismiss = { showCreatePoModal = false },
                onCreated = {
                    showCreatePoModal = false
                    refreshTrigger++
                }
            )
        }

        if (showImportBomModal) {
            ImportBomDialog(
                projectId = selectedProjectId ?: projectsList.firstOrNull()?.id ?: "",
                onDismiss = { showImportBomModal = false },
                onImported = {
                    showImportBomModal = false
                    refreshTrigger++
                }
            )
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Left
) {
    Text(
        text = text,
        style = FabType.buttonSmall.copy(
            color = FabColors.TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        ),
        modifier = modifier,
        textAlign = align
    )
}

@Composable
private fun StatusFilterSelector(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .clip(FabShapes.Button)
                .background(FabColors.CardBackground)
                .border(1.dp, FabColors.Border, FabShapes.Button)
                .clickable { open = !open }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (selectedStatus.isBlank()) "All statuses ▼" else "${selectedStatus.replace("_", " ")} ▼",
                style = FabType.buttonSmall.copy(color = FabColors.TextPrimary)
            )
        }

        DropdownMenu(
            expanded = open,
            onDismissRequest = { open = false }
        ) {
            DropdownMenuItem(
                text = { Text("All statuses") },
                onClick = { onStatusSelected(""); open = false }
            )
            STATUS_OPTIONS.forEach { st ->
                DropdownMenuItem(
                    text = { Text(st.replace("_", " ")) },
                    onClick = { onStatusSelected(st); open = false }
                )
            }
        }
    }
}

// ─── Modals ───

@Composable
private fun PartFormDialog(
    title: String,
    initialPart: Part?,
    projects: List<Project>,
    defaultProjectId: String?,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any?>) -> Unit
) {
    var projectId by remember { mutableStateOf(initialPart?.project_id ?: defaultProjectId ?: projects.firstOrNull()?.id ?: "") }
    var partMark by remember { mutableStateOf(initialPart?.part_mark ?: "") }
    var assemblyMark by remember { mutableStateOf(initialPart?.assembly_mark ?: "") }
    var name by remember { mutableStateOf(initialPart?.name ?: "") }
    var profile by remember { mutableStateOf(initialPart?.profile ?: "") }
    var grade by remember { mutableStateOf(initialPart?.grade ?: "A992") }
    var length by remember { mutableStateOf(initialPart?.length ?: "") }
    var weight by remember { mutableStateOf(initialPart?.weight?.toString() ?: "") }
    var quantity by remember { mutableStateOf(initialPart?.quantity?.toString() ?: "1") }
    var heatNumber by remember { mutableStateOf(initialPart?.heat_number ?: "") }
    var phase by remember { mutableStateOf(initialPart?.phase ?: "") }
    var status by remember { mutableStateOf(initialPart?.status ?: "not_started") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(480.dp).padding(16.dp),
            shape = FabShapes.Modal,
            colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, style = FabType.cardTitle)
                    Text("✕", style = FabType.buttonSmall, modifier = Modifier.clickable { onDismiss() })
                }

                HorizontalDivider(color = FabColors.Border)

                FabTextField(
                    value = partMark,
                    onValueChange = { partMark = it },
                    label = "Part Mark *"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FabTextField(value = profile, onValueChange = { profile = it }, label = "Profile *")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FabTextField(value = name, onValueChange = { name = it }, label = "Name")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FabTextField(value = grade, onValueChange = { grade = it }, label = "Grade")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FabTextField(value = length, onValueChange = { length = it }, label = "Length")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FabTextField(value = quantity, onValueChange = { quantity = it }, label = "QTY *")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FabTextField(value = weight, onValueChange = { weight = it }, label = "Weight (lb)")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FabTextField(value = assemblyMark, onValueChange = { assemblyMark = it }, label = "Assembly Mark")
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FabTextField(value = heatNumber, onValueChange = { heatNumber = it }, label = "Heat #")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FabButton("Cancel", onClick = onDismiss, variant = ButtonVariant.Secondary, size = ButtonSize.Small)
                    Spacer(modifier = Modifier.width(8.dp))
                    FabButton(
                        text = "Save Part",
                        onClick = {
                            if (partMark.isNotBlank() && profile.isNotBlank()) {
                                val payload = mutableMapOf<String, Any?>()
                                if (projectId.isNotBlank()) payload["project_id"] = projectId
                                payload["part_mark"] = partMark
                                if (assemblyMark.isNotBlank()) payload["assembly_mark"] = assemblyMark
                                if (name.isNotBlank()) payload["name"] = name
                                payload["profile"] = profile
                                if (grade.isNotBlank()) payload["grade"] = grade
                                if (length.isNotBlank()) payload["length"] = length
                                weight.toDoubleOrNull()?.let { payload["weight"] = it }
                                payload["quantity"] = quantity.toIntOrNull() ?: 1
                                if (heatNumber.isNotBlank()) payload["heat_number"] = heatNumber
                                if (phase.isNotBlank()) payload["phase"] = phase
                                payload["status"] = status
                                onSave(payload)
                            }
                        },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Small
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatePoDialog(
    projectId: String,
    parts: List<Part>,
    onDismiss: () -> Unit,
    onCreated: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var vendor by remember { mutableStateOf("") }
    var expectedDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    val notStartedParts = remember(parts) { parts.filter { it.status == "not_started" } }
    val totalWeight = remember(notStartedParts) { notStartedParts.sumOf { it.weight ?: 0.0 } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(480.dp).padding(16.dp),
            shape = FabShapes.Modal,
            colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Create Purchase Order from Parts", style = FabType.cardTitle)
                Text(
                    text = "Generates a PO from ${notStartedParts.size} not-started parts (${totalWeight.toInt()} lb total).",
                    style = FabType.cardSub,
                    color = FabColors.TextMuted
                )

                HorizontalDivider(color = FabColors.Border)

                FabTextField(value = vendor, onValueChange = { vendor = it }, label = "Vendor Name *", placeholder = "Triple S Steel")
                FabTextField(value = expectedDate, onValueChange = { expectedDate = it }, label = "Expected Delivery Date", placeholder = "YYYY-MM-DD")
                FabTextField(value = notes, onValueChange = { notes = it }, label = "Notes")

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FabButton("Cancel", onClick = onDismiss, variant = ButtonVariant.Secondary, size = ButtonSize.Small)
                    Spacer(modifier = Modifier.width(8.dp))
                    FabButton(
                        text = if (submitting) "Creating…" else "Create PO",
                        onClick = {
                            if (vendor.isNotBlank() && !submitting) {
                                submitting = true
                                coroutineScope.launch {
                                    try {
                                        AppContainer.apiClient.post<Unit>(
                                            "/purchase_orders",
                                            mapOf(
                                                "project_id" to projectId,
                                                "vendor_name" to vendor,
                                                "expected_date" to expectedDate.ifBlank { null },
                                                "notes" to notes.ifBlank { null }
                                            )
                                        )
                                        onCreated()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        submitting = false
                                    }
                                }
                            }
                        },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Small,
                        enabled = vendor.isNotBlank() && !submitting
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportBomDialog(
    projectId: String,
    onDismiss: () -> Unit,
    onImported: () -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(520.dp).padding(16.dp),
            shape = FabShapes.Modal,
            colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Import BOM / Tekla Parts", style = FabType.cardTitle)
                Text(
                    text = "Paste CSV / KISS / BOM rows (Mark, Profile, QTY, Length, Grade).",
                    style = FabType.cardSub,
                    color = FabColors.TextMuted
                )

                HorizontalDivider(color = FabColors.Border)

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    placeholder = { Text("2000BR17, 1/2, 1, 23'-9 7/8\", A36\n2000C20, W8X24, 2, 1'-8 5/8\", A992") }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FabButton("Cancel", onClick = onDismiss, variant = ButtonVariant.Secondary, size = ButtonSize.Small)
                    Spacer(modifier = Modifier.width(8.dp))
                    FabButton(
                        text = if (submitting) "Importing…" else "Import BOM",
                        onClick = {
                            if (rawText.isNotBlank() && !submitting) {
                                submitting = true
                                coroutineScope.launch {
                                    try {
                                        // Process pasted CSV rows
                                        val lines = rawText.lines().filter { it.isNotBlank() }
                                        lines.forEach { line ->
                                            val parts = line.split(",").map { it.trim() }
                                            if (parts.size >= 2) {
                                                val payload = mapOf(
                                                    "project_id" to projectId,
                                                    "part_mark" to parts[0],
                                                    "profile" to parts[1],
                                                    "quantity" to (parts.getOrNull(2)?.toIntOrNull() ?: 1),
                                                    "length" to parts.getOrNull(3),
                                                    "grade" to parts.getOrNull(4)
                                                )
                                                try {
                                                    AppContainer.apiClient.post<Unit>("/parts", payload)
                                                } catch (_: Exception) {}
                                            }
                                        }
                                        onImported()
                                    } finally {
                                        submitting = false
                                    }
                                }
                            }
                        },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Small,
                        enabled = rawText.isNotBlank() && !submitting
                    )
                }
            }
        }
    }
}
