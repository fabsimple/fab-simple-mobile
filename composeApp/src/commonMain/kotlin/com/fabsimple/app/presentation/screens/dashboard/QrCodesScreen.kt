package com.fabsimple.app.presentation.screens.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.*
import com.fabsimple.app.components.layout.DrawerScaffold
import com.fabsimple.app.presentation.screens.auth.LoginScreen
import com.fabsimple.app.presentation.screens.utilities.CutOptimizerScreen
import com.fabsimple.app.presentation.screens.utilities.CopilotScreen
import com.fabsimple.app.state.GlobalProjectState
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.FileAttachment
import com.fabsimple.shared.domain.model.Part
import com.fabsimple.shared.domain.model.Project
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.launch

/**
 * QR Codes management screen.
 * Replicates dashboard/qr-codes/page.tsx with full feature parity.
 */
class QrCodesScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        val activeProject by GlobalProjectState.currentProject.collectAsState()
        val selectedProjectId = activeProject.id

        var partsList by remember { mutableStateOf<List<Part>>(emptyList()) }
        var pdfCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
        var selectedParts by remember { mutableStateOf<Set<String>>(emptySet()) }
        var missingOnly by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var currentPage by remember { mutableStateOf(1) }
        var pageSize by remember { mutableStateOf(25) }

        var loading by remember { mutableStateOf(false) }
        var attachmentsLoading by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var attachmentsRev by remember { mutableStateOf(0) }

        // Drawing Attachment Detail Drawer state
        var attachTargetPart by remember { mutableStateOf<Part?>(null) }
        var attachTargetFiles by remember { mutableStateOf<List<FileAttachment>>(emptyList()) }
        var drawerLoading by remember { mutableStateOf(false) }
        var drawerError by remember { mutableStateOf<String?>(null) }
        var drawerUploading by remember { mutableStateOf(false) }
        var showFilePicker by remember { mutableStateOf(false) }

        // Fetch parts & attachments when project or rev changes
        LaunchedEffect(selectedProjectId, attachmentsRev) {
            loading = true
            error = null
            currentPage = 1
            try {
                // Fetch parts (returns ALL parts across pages)
                val parts = AppContainer.partRepository.getParts(selectedProjectId)
                partsList = parts

                // Fetch attachment counts in chunks of 50 to avoid URL query length caps
                if (parts.isNotEmpty()) {
                    attachmentsLoading = true
                    try {
                        val counts = mutableMapOf<String, Int>()
                        parts.chunked(50).forEach { chunk ->
                            val idsKey = chunk.joinToString(",") { it.id }
                            val attachments: List<FileAttachment> = try {
                                AppContainer.apiClient.get(
                                    "/file_attachments",
                                    mapOf(
                                        "entity_type" to "parts",
                                        "entity_id__in" to idsKey
                                    )
                                )
                            } catch (_: Exception) { emptyList() }
                            attachments.forEach { file ->
                                val pId = file.entity_id
                                if (pId != null) {
                                    counts[pId] = (counts[pId] ?: 0) + 1
                                }
                            }
                        }
                        pdfCounts = counts
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        attachmentsLoading = false
                    }
                } else {
                    pdfCounts = emptyMap()
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load parts"
            } finally {
                loading = false
            }
        }

        // Fetch target part's attachments for details drawer
        LaunchedEffect(attachTargetPart, attachmentsRev) {
            val part = attachTargetPart
            if (part != null) {
                drawerLoading = true
                drawerError = null
                try {
                    attachTargetFiles = AppContainer.fileRepository.getFilesForEntity("parts", part.id)
                } catch (e: Exception) {
                    drawerError = e.message ?: "Failed to load files"
                } finally {
                    drawerLoading = false
                }
            } else {
                attachTargetFiles = emptyList()
            }
        }

        val filteredParts = remember(partsList, missingOnly, pdfCounts, searchQuery) {
            var res = if (!missingOnly) partsList else partsList.filter { (pdfCounts[it.id] ?: 0) == 0 }
            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                res = res.filter { p ->
                    p.part_mark.lowercase().contains(q) ||
                    p.profile.lowercase().contains(q) ||
                    (p.assembly_mark ?: "").lowercase().contains(q) ||
                    (p.name ?: "").lowercase().contains(q)
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

        val missingCount = remember(partsList, pdfCounts) {
            partsList.count { (pdfCounts[it.id] ?: 0) == 0 }
        }

        fun togglePartSelection(id: String) {
            selectedParts = if (selectedParts.contains(id)) {
                selectedParts - id
            } else {
                selectedParts + id
            }
        }

        fun selectAllFiltered() {
            selectedParts = filteredParts.map { it.id }.toSet()
        }

        fun selectPage() {
            selectedParts = selectedParts + visibleParts.map { it.id }
        }

        fun clearSelection() {
            selectedParts = emptySet()
        }

        fun handlePrint() {
            val sheetParts = visibleParts.filter { selectedParts.contains(it.id) }
            if (sheetParts.isEmpty()) return

            val html = buildString {
                append("<html><head><title>FabSimple QR Sheet — ${sheetParts.size} parts</title>")
                append("<style>")
                append("@page { size: letter; margin: 10mm; }")
                append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 10mm; color: #0f172a; background: #fff; }")
                append("h2 { margin: 0 0 6mm 0; font-size: 13pt; font-weight: 700; color: #0f172a; border-bottom: 2px solid #e2e8f0; padding-bottom: 3mm; display: flex; align-items: center; justify-between; }")
                append(".grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 5mm; }")
                append(".label { border: 1px solid #cbd5e1; border-radius: 4mm; padding: 4mm; text-align: center; page-break-inside: avoid; background: #ffffff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); position: relative; }")
                append(".brand-hdr { display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #cbd5e1; padding-bottom: 2.5mm; margin-bottom: 2.5mm; }")
                append(".brand-logo { display: flex; align-items: center; gap: 4px; font-weight: 800; font-size: 8.5pt; color: #1e293b; text-transform: uppercase; letter-spacing: 0.5px; }")
                append(".proj-line { display: flex; align-items: center; justify-content: center; gap: 4px; margin-bottom: 2mm; font-family: ui-monospace, SFMono-Regular, monospace; }")
                append(".job-no { font-size: 8pt; font-weight: 700; color: #4f46e5; flex-shrink: 0; }")
                append(".proj-sep { font-size: 8pt; color: #94a3b8; }")
                append(".proj-title { font-size: 8pt; font-weight: 600; color: #64748b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; text-transform: uppercase; }")
                append(".qr { width: 35mm; height: 35mm; margin: 0 auto 2mm auto; display: block; }")
                append(".mark { font-size: 13pt; font-weight: 800; font-family: ui-monospace, SFMono-Regular, monospace; letter-spacing: 0.5px; color: #0f172a; margin-top: 1mm; }")
                append(".prof { font-size: 8.5pt; font-weight: 600; color: #334155; margin-top: 1mm; }")
                append(".heat { display: inline-block; background: #fff7ed; color: #c2410c; border: 1px solid #ffedd5; font-size: 7.5pt; font-weight: 700; font-family: ui-monospace, SFMono-Regular, monospace; padding: 1.5px 7px; border-radius: 3px; margin-top: 1.5mm; }")
                append(".shop { display: inline-block; background: #e0e7ff; color: #3730a3; border: 1px solid #c7d2fe; font-size: 7.5pt; font-weight: 700; font-family: ui-monospace, SFMono-Regular, monospace; padding: 1.5px 7px; border-radius: 3px; margin-top: 1.5mm; margin-left: 1mm; }")
                append(".meta { font-size: 7.5pt; color: #64748b; margin-top: 1mm; font-family: ui-monospace, SFMono-Regular, monospace; }")
                append(".nopdf { font-size: 7.5pt; color: #dc2626; font-weight: 700; margin-top: 1.5mm; background: #fef2f2; padding: 1px 4px; border-radius: 2px; }")
                append(".rev { font-size: 7.5pt; color: #16a34a; font-weight: 700; margin-top: 1.5mm; }")
                append("@media print { body { margin: 0; } h2 { display: none; } }")
                append("</style></head><body>")
                append("<h2>FabSimple QR Code Sheet — ${sheetParts.size} parts</h2>")
                append("<div class='grid'>")
                sheetParts.forEach { p ->
                    val count = pdfCounts[p.id] ?: 0
                    val noPdf = count == 0
                    val jobNoStr = p.project_number ?: activeProject.number ?: ""
                    val projNameStr = p.project_name ?: activeProject.name
                    append("<div class='label'>")
                    append("<div class='brand-hdr'><div class='brand-logo'><span>❖ FabSimple</span></div></div>")
                    if (jobNoStr.isNotEmpty() || projNameStr.isNotEmpty()) {
                        append("<div class='proj-line'>")
                        if (jobNoStr.isNotEmpty()) append("<span class='job-no'>JOB ${escapeHtml(jobNoStr)}</span>")
                        if (jobNoStr.isNotEmpty() && projNameStr.isNotEmpty()) append("<span class='proj-sep'>•</span>")
                        if (projNameStr.isNotEmpty()) append("<span class='proj-title'>${escapeHtml(projNameStr)}</span>")
                        append("</div>")
                    }
                    append("<img class='qr' src='https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=https://fabsimple.app/worker/parts/${p.id}' alt='QR' />")
                    append("<div class='mark'>${escapeHtml(p.part_mark)}</div>")
                    append("<div class='prof'>${escapeHtml(p.profile)}${if (p.grade != null) " · ${escapeHtml(p.grade)}" else ""}</div>")
                    append("<div class='heat'>HEAT #: ${escapeHtml(p.heat_number ?: "Unassigned")}</div>")
                    append("<div class='shop'>SHOP: ${escapeHtml(p.finish ?: "SHOP PRIMER")}</div>")
                    if (p.assembly_mark != null) {
                        append("<div class='meta'>Asm: ${escapeHtml(p.assembly_mark)}${if (p.quantity > 1) " · Qty: ${p.quantity}" else ""}</div>")
                    }
                    if (noPdf) {
                        append("<div class='nopdf'>⚠ NO DRAWING ATTACHED</div>")
                    } else if (count > 1) {
                        append("<div class='rev'>DRAWING REV $count</div>")
                    }
                    append("</div>")
                }
                append("</div>")
                append("<script>window.addEventListener('load', function(){ setTimeout(function(){ window.print(); }, 250); });</script>")
                append("</body></html>")
            }

            printHtml(html, "FabSimple_QR_${sheetParts.size}_parts")
        }

        DrawerScaffold(
            title = "QR Codes",
            currentRoute = "qr-codes",
            onNavigate = { route ->
                if (route == "dashboard") {
                    navigator.replaceAll(DashboardScreen())
                } else if (route == "parts") {
                    navigator.replaceAll(PartsListScreen())
                } else if (route == "cut-list") {
                    navigator.replaceAll(CutOptimizerScreen())
                } else if (route == "copilot") {
                    navigator.replaceAll(CopilotScreen())
                } else if (route != "qr-codes") {
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
                title = "QR Codes",
                scrollable = true
            ) {
                // Header with counts info
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "QR Code Sheets",
                            style = FabType.sectionTitle
                        )
                        Text(
                            text = "${partsList.size} parts total · workers scan to load the assigned part view + attached drawings.",
                            style = FabType.cardSub,
                            color = FabColors.TextMuted
                        )
                    }

                    // Action Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Missing PDF toggle button
                        Box(
                            modifier = Modifier
                                .clip(FabShapes.Button)
                                .background(
                                    if (missingOnly) Color(0xFFDC2626).copy(alpha = 0.08f) else Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (missingOnly) Color(0xFFDC2626) else FabColors.Border,
                                    shape = FabShapes.Button
                                )
                                .clickable {
                                    missingOnly = !missingOnly
                                    selectedParts = emptySet()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "⚠️ Missing PDF",
                                    style = FabType.buttonSmall.copy(
                                        color = if (missingOnly) Color(0xFFDC2626) else FabColors.TextPrimary
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (missingCount > 0) Color(0xFFDC2626) else FabColors.Border)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = missingCount.toString(),
                                        style = FabType.buttonSmall.copy(
                                            color = Color.White,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        FabButton(
                            text = "Select Page (${visibleParts.size})",
                            onClick = { selectPage() },
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Small
                        )

                        FabButton(
                            text = "Select All (${filteredParts.size})",
                            onClick = { selectAllFiltered() },
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Small
                        )

                        FabButton(
                            text = "Clear",
                            onClick = { clearSelection() },
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Small
                        )

                        FabButton(
                            text = "Print ${if (selectedParts.isNotEmpty()) "(${selectedParts.size})" else ""}",
                            onClick = { handlePrint() },
                            variant = ButtonVariant.Primary,
                            size = ButtonSize.Small,
                            enabled = selectedParts.isNotEmpty()
                        )
                    }
                }

                // Search & Pagination Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(260.dp)) {
                        FabTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it; currentPage = 1 },
                            placeholder = "Search parts..."
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page $currentPageClamped of $totalPages (${filteredParts.size} total)",
                            style = FabType.cardSub,
                            color = FabColors.TextMuted
                        )

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

                if (loading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FabColors.Primary)
                    }
                } else if (error != null) {
                    AlertBanner(message = error!!, variant = AlertVariant.Danger)
                } else if (visibleParts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FabTheme.extendedColors.mutedBackground.copy(alpha = 0.5f), FabShapes.InfoCell)
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (missingOnly) "All visible parts already have a drawing PDF attached. Clear the filter to see everything."
                            else "No parts found for the selected project.",
                            style = FabType.cardSub,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Cards Grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 2000.dp)
                    ) {
                        items(visibleParts) { part ->
                            val isSelected = selectedParts.contains(part.id)
                            val count = pdfCounts[part.id] ?: 0
                            val noPdf = count == 0

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { togglePartSelection(part.id) }
                                    .border(
                                        width = if (noPdf) 1.5.dp else if (isSelected) 1.5.dp else 1.dp,
                                        color = if (noPdf) Color(0xFFDC2626).copy(alpha = 0.5f)
                                        else if (isSelected) FabColors.Primary
                                        else FabColors.Border,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) FabColors.PrimaryBg.copy(alpha = 0.4f)
                                    else FabTheme.extendedColors.cardBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Card Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left logo
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("❖", color = FabColors.Primary, fontSize = 12.sp)
                                            Text(
                                                text = "FabSimple",
                                                style = FabType.infoCellLabel,
                                                fontWeight = FontWeight.Bold,
                                                color = FabColors.TextPrimary
                                            )
                                        }

                                        // Right attachments badge
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .background(FabColors.Primary, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            // Attachments paperclip action
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (noPdf) Color(0xFFDC2626).copy(alpha = 0.15f)
                                                        else Color(0xFF16A34A).copy(alpha = 0.15f)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (noPdf) Color(0xFFDC2626).copy(alpha = 0.3f)
                                                        else Color(0xFF16A34A).copy(alpha = 0.3f),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .clickable {
                                                        attachTargetPart = part
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Text("📎", fontSize = 10.sp)
                                                    Text(
                                                        text = if (noPdf) "Missing" else "v$count",
                                                        style = FabType.buttonSmall.copy(
                                                            color = if (noPdf) Color(0xFFEF4444) else Color(0xFF22C55E),
                                                            fontSize = 9.sp
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Divider(color = FabColors.Border, modifier = Modifier.padding(bottom = 8.dp))

                                    // Project Line
                                    val jobNo = part.project_number ?: activeProject.number
                                    val projName = part.project_name ?: activeProject.name
                                    if (jobNo != null || projName.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        ) {
                                            if (jobNo != null) {
                                                Text(
                                                    text = "Job $jobNo",
                                                    style = FabType.monoCell.copy(
                                                        color = FabColors.Primary,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                            if (jobNo != null && projName.isNotEmpty()) {
                                                Text("•", style = FabType.cardSub)
                                            }
                                            if (projName.isNotEmpty()) {
                                                Text(
                                                    text = projName,
                                                    style = FabType.cardTitle.copy(
                                                        color = FabColors.TextMuted,
                                                        fontSize = 10.sp
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }

                                    // QR Code Image
                                    QrCodeImage(partId = part.id)

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Part Mark
                                    Text(
                                        text = part.part_mark,
                                        style = FabType.monoCell.copy(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    )

                                    // Profile & Grade
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = part.profile,
                                            style = FabType.cardSub.copy(fontSize = 11.sp)
                                        )
                                        val grade = part.grade
                                        if (grade != null) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                                            ) {
                                                Text(
                                                    text = grade,
                                                    style = FabType.cardSub.copy(fontSize = 9.sp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Badges row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Heat number
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (part.heat_number != null) Color(0xFFF97316).copy(alpha = 0.15f)
                                                    else Color.White.copy(alpha = 0.04f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (part.heat_number != null) Color(0xFFF97316).copy(alpha = 0.3f)
                                                    else FabColors.Border,
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "🔥 Heat: ${part.heat_number ?: "Unassigned"}",
                                                style = FabType.buttonSmall.copy(
                                                    color = if (part.heat_number != null) Color(0xFFFB923C) else FabColors.TextMuted,
                                                    fontSize = 9.sp
                                                )
                                            )
                                        }

                                        // Shop finish
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF6366F1).copy(alpha = 0.15f))
                                                .border(
                                                    1.dp,
                                                    Color(0xFF6366F1).copy(alpha = 0.3f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "🖌️ Shop: ${part.finish ?: "SHOP PRIMER"}",
                                                style = FabType.buttonSmall.copy(
                                                    color = Color(0xFF818CF8),
                                                    fontSize = 9.sp
                                                )
                                            )
                                        }
                                    }

                                    // Assembly & quantity details
                                    if (part.assembly_mark != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Asm: ${part.assembly_mark} · Qty: ${part.quantity}",
                                            style = FabType.cardSub.copy(
                                                fontFamily = MonoFontFamily,
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

        // Drawing attachments Modal drawer
        val target = attachTargetPart
        if (target != null) {
            ResourceModal(
                title = "Attach drawing — ${target.part_mark}",
                onClose = { attachTargetPart = null },
                onSubmit = {
                    showFilePicker = true
                },
                submitLabel = "Add File",
                submitting = drawerUploading,
                error = drawerError
            ) {
                FileUploader(
                    files = attachTargetFiles,
                    onAddFileClick = {
                        showFilePicker = true
                    },
                    onOpenFile = { file ->
                        // Open file in browser / print
                        coroutineScope.launch {
                            try {
                                val url = AppContainer.fileRepository.getSignedReadUrl(file.id)
                                // Trigger open
                                openBrowserUrl(url)
                            } catch (e: Exception) {
                                drawerError = e.message ?: "Failed to open file link"
                            }
                        }
                    },
                    onDeleteFile = { file ->
                        // Delete attachment
                        coroutineScope.launch {
                            drawerUploading = true
                            try {
                                AppContainer.apiClient.delete<Unit>("/file_attachments/${file.id}")
                                attachmentsRev++
                            } catch (e: Exception) {
                                drawerError = e.message ?: "Failed to delete file"
                            } finally {
                                drawerUploading = false
                            }
                        }
                    },
                    loading = drawerLoading,
                    uploading = drawerUploading
                )
            }
        }

        FilePicker(
            show = showFilePicker,
            onFilePicked = { filename, bytes, mime ->
                showFilePicker = false
                val part = attachTargetPart
                if (part != null) {
                    coroutineScope.launch {
                        drawerUploading = true
                        drawerError = null
                        try {
                            AppContainer.fileRepository.uploadFile(
                                entityType = "parts",
                                entityId = part.id,
                                filename = filename,
                                bytes = bytes,
                                contentType = mime,
                                bucket = "drawings"
                            )
                            attachmentsRev++
                        } catch (e: Exception) {
                            drawerError = e.message ?: "Failed to upload drawing"
                        } finally {
                            drawerUploading = false
                        }
                    }
                }
            },
            onDismiss = {
                showFilePicker = false
            }
        )
    }

    private fun escapeHtml(str: String?): String {
        if (str == null) return ""
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }

    private fun openBrowserUrl(url: String) {
        // Safe cross-platform wrapper to print link or mock open
        println("Opening signed read URL: $url")
    }
}

@Composable
fun QrCodeImage(
    partId: String,
    modifier: Modifier = Modifier
) {
    var bitmap by remember(partId) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(partId) { mutableStateOf(true) }
    var loadError by remember(partId) { mutableStateOf(false) }

    LaunchedEffect(partId) {
        loading = true
        loadError = false
        try {
            val url = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=https://fabsimple.app/worker/parts/$partId"
            val responseBytes = AppContainer.apiClient.httpClient.get(url).body<ByteArray>()
            bitmap = responseBytes.toImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            loadError = true
        } finally {
            loading = false
        }
    }

    Box(
        modifier = modifier
            .size(96.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(color = FabColors.Primary, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else if (loadError || bitmap == null) {
            Text("⚠️", fontSize = 18.sp)
        } else {
            Image(
                bitmap = bitmap!!,
                contentDescription = "QR Code",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
