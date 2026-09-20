package com.fabsimple.app.presentation.screens.worker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
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
import com.fabsimple.app.components.ButtonVariant
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.components.FilePicker
import com.fabsimple.app.components.PdfWebView
import com.fabsimple.app.theme.*
import com.fabsimple.shared.data.network.SignReadResponse
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.Drawing
import com.fabsimple.shared.domain.model.Part
import com.fabsimple.shared.domain.model.UserProfile
import io.ktor.http.encodeURLQueryComponent
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PartDetailScreen(val partId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val uriHandler = LocalUriHandler.current

        var part by remember { mutableStateOf<Part?>(null) }
        var usersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }
        var updating by remember { mutableStateOf(false) }
        var showPhotoPicker by remember { mutableStateOf(false) }
        var photoCount by remember { mutableStateOf(0) }
        var fetchedDrawings by remember { mutableStateOf<List<Drawing>>(emptyList()) }
        var resolvedDrawingUrl by remember { mutableStateOf<String?>(null) }
        var resolvedDrawingUrls by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
        var showFullPdfModal by remember { mutableStateOf(false) }
        var activeDrawingId by remember { mutableStateOf<String?>(null) }

        fun fetchPart() {
            loading = true
            coroutineScope.launch {
                try {
                    val loadedPart = AppContainer.partRepository.getPartById(partId)
                    part = loadedPart
                    usersList = AppContainer.adminRepository.getUsers()
                    try {
                        fetchedDrawings = AppContainer.drawingRepository.getDrawingsForPart(partId)
                    } catch (_: Exception) {}

                    val newResolvedUrls = mutableMapOf<String, String>()
                    val rawId = loadedPart.drawing_id

                    if (!rawId.isNullOrBlank()) {
                        if (rawId.startsWith("http")) {
                            resolvedDrawingUrl = rawId
                            newResolvedUrls[rawId] = rawId
                        } else {
                            try {
                                val signRes = AppContainer.apiClient.get<SignReadResponse>("/files/$rawId")
                                resolvedDrawingUrl = signRes.url
                                newResolvedUrls[rawId] = signRes.url
                            } catch (_: Exception) {
                                try {
                                    val signedUrl = AppContainer.fileRepository.getSignedReadUrl(rawId)
                                    resolvedDrawingUrl = signedUrl
                                    newResolvedUrls[rawId] = signedUrl
                                } catch (_: Exception) {}
                            }
                        }
                    } else {
                        resolvedDrawingUrl = null
                    }

                    for (drw in fetchedDrawings) {
                        val url = drw.url
                        if (!url.isNullOrBlank() && url.startsWith("http")) {
                            newResolvedUrls[drw.id] = url
                        } else if (drw.id.isNotBlank()) {
                            try {
                                val signRes = AppContainer.apiClient.get<SignReadResponse>("/files/${drw.id}")
                                newResolvedUrls[drw.id] = signRes.url
                            } catch (_: Exception) {
                                try {
                                    val signedUrl = AppContainer.fileRepository.getSignedReadUrl(drw.id)
                                    newResolvedUrls[drw.id] = signedUrl
                                } catch (_: Exception) {}
                            }
                        }
                    }

                    resolvedDrawingUrls = newResolvedUrls
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    loading = false
                }
            }
        }

        LaunchedEffect(partId) {
            fetchPart()
        }

        fun updateStage(payload: Map<String, String>) {
            updating = true
            coroutineScope.launch {
                try {
                    AppContainer.partRepository.updatePartStatus(partId, payload)
                    fetchPart()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    updating = false
                }
            }
        }

        fun resolveUserName(userId: String?, users: List<UserProfile>, fallbackName: String): String {
            if (userId.isNullOrBlank()) return fallbackName
            if (!userId.contains("-") && userId.contains(" ")) return userId
            val found = users.find { it.id == userId }
            if (found != null && found.full_name.isNotBlank()) return found.full_name
            return fallbackName
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1120))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF818CF8))
                }
            } else if (part == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Part not found", color = Color.White)
                }
            } else {
                val p = part!!
                val session = AppContainer.authRepository.getSession()
                val currentUserName = session?.name ?: "Roberto Torres"
                val currentUserId = session?.userId ?: "worker-1"

                val rawDrawingId = p.drawing_id
                println("Bharat_pdfview --rawDrawingId ${rawDrawingId}")
                val drawingId = if (!rawDrawingId.isNullOrBlank()) rawDrawingId else "drawing-${p.part_mark}"
                val rawName = p.name
                val rawProfile = p.profile
                val partTag = if (!rawName.isNullOrBlank()) rawName.uppercase().replace(" ", "_")
                    else if (!rawProfile.isNullOrBlank() && rawProfile.contains("ANGLE", ignoreCase = true)) "ANGLE"
                    else if (!rawProfile.isNullOrBlank() && (rawProfile.startsWith("L") || rawProfile.contains("L"))) "ANGLE"
                    else if (!rawProfile.isNullOrBlank()) rawProfile.uppercase().replace(" ", "_")
                    else "ANGLE"

                val defaultPdfUrl = (resolvedDrawingUrls[drawingId] ?: resolvedDrawingUrl)?.takeIf { url ->
                    url.isNotBlank() && url.startsWith("http") && url.lowercase().contains(".pdf") && !url.contains("698d7afd-c0e9-433e-9a90-3dd0b0202af6")
                } ?: "https://mteocbcpbdgfdysulmiv.supabase.co/storage/v1/object/public/drawings/${p.part_mark}_-_${partTag}_-_Rev_0.pdf"

                val defaultDrawings = remember(p, fetchedDrawings, resolvedDrawingUrls, resolvedDrawingUrl) {
                    val pdfOnlyDrawings = fetchedDrawings.filter { drw ->
                        val fn = drw.filename?.lowercase() ?: ""
                        val mime = drw.mime_type?.lowercase() ?: ""
                        fn.contains(".pdf") || mime.contains("pdf")
                    }

                    if (pdfOnlyDrawings.isNotEmpty()) {
                        pdfOnlyDrawings.mapIndexed { index, drw ->
                            val drawUrl = (drw.url?.takeIf { it.isNotBlank() && it.startsWith("http") } ?: resolvedDrawingUrls[drw.id])?.takeIf {
                                it.lowercase().contains(".pdf")
                            } ?: defaultPdfUrl

                            DrawingTabItem(
                                id = if (drw.id.isNotBlank()) drw.id else drawingId,
                                revLabel = if (!drw.revision.isNullOrBlank()) "R${drw.revision}" else "R${pdfOnlyDrawings.size - 1 - index}",
                                filename = drw.filename ?: "${p.part_mark}_-_${partTag}_-_Rev_0.pdf",
                                url = drawUrl,
                                isLatest = index == 0
                            )
                        }
                    } else {
                        listOf(
                            DrawingTabItem(
                                id = drawingId,
                                revLabel = "R0",
                                filename = "${p.part_mark}_-_${partTag}_-_Rev_0.pdf",
                                url = defaultPdfUrl,
                                isLatest = true
                            )
                        )
                    }
                }

                val activeDrawing = defaultDrawings.find { it.id == activeDrawingId } ?: defaultDrawings[0]

                fun openPdfDocument(targetUrl: String = activeDrawing.url) {
                    println("Bharat_pdfview --openPdfDocument targetUrl=$targetUrl activeDrawingUrl=${activeDrawing.url}")
                    showFullPdfModal = true

                    val isRealPdfUrl = targetUrl.isNotBlank() && targetUrl.startsWith("http") && targetUrl.lowercase().contains(".pdf") && !targetUrl.contains("698d7afd-c0e9-433e-9a90-3dd0b0202af6")

                    val finalUrl = if (isRealPdfUrl) {
                        targetUrl
                    } else if (!resolvedDrawingUrl.isNullOrBlank() && resolvedDrawingUrl!!.startsWith("http") && resolvedDrawingUrl!!.lowercase().contains(".pdf") && !resolvedDrawingUrl!!.contains("698d7afd-c0e9-433e-9a90-3dd0b0202af6")) {
                        resolvedDrawingUrl!!
                    } else {
                        "https://mteocbcpbdgfdysulmiv.supabase.co/storage/v1/object/public/drawings/${p.part_mark}_-_${partTag}_-_Rev_0.pdf"
                    }

                    println("Bharat_pdfview --finalUrl $finalUrl")
                    if (finalUrl.startsWith("http")) {
                        try {
                            uriHandler.openUri(finalUrl)
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ─── Top Bar Controls ───
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable { navigator.pop() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("‹ Queue", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                .clickable { /* Triggers print traveller */ }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("🖨 Fallback Traveller", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Online Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "📶 Online",
                            color = Color(0xFF34D399),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ─── Main Part Overview Card ───
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111C2E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = p.part_mark,
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    val totalQty = p.quantity.coerceAtLeast(1)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF312E81).copy(alpha = 0.6f))
                                            .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "QTY: $totalQty PCS",
                                            color = Color(0xFFA5B4FC),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Text(
                                        text = p.profile,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                val statusLabel = when (p.status.lowercase()) {
                                    "not_started" -> "Not Started"
                                    "fit_up" -> "In Progress (Cut Done)"
                                    "welding" -> "In Progress (Fit-Up Done)"
                                    "painting" -> "In Progress (Paint Done)"
                                    "complete", "completed" -> "Complete"
                                    "shipped" -> "Shipped"
                                    else -> p.status.replace("_", " ")
                                }
                                val isCompleteStatus = p.status.lowercase() == "complete" || p.status.lowercase() == "completed" || p.status.lowercase() == "shipped"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isCompleteStatus) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF472B6).copy(alpha = 0.15f))
                                        .border(1.dp, if (isCompleteStatus) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF472B6).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        color = if (isCompleteStatus) Color(0xFF34D399) else Color(0xFFF472B6),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // ─── Overall Batch Progress Box (Matching Screenshot) ───
                            val totalQty = p.quantity.coerceAtLeast(1)
                            val cutQty = if (p.cut_qty > 0) p.cut_qty else if (p.cut_completed_at != null) totalQty else 0
                            val fitQty = if (p.fit_qty > 0) p.fit_qty else if (p.fit_completed_at != null || p.fit_skipped == true) totalQty else 0
                            val weldQty = if (p.weld_qty > 0) p.weld_qty else if (p.weld_completed_at != null || p.weld_skipped == true) totalQty else 0
                            val weldQcQty = if (p.weld_qc_qty > 0) p.weld_qc_qty else 0
                            val finishQty = if (p.finish_qty > 0) p.finish_qty else if (p.finish_completed_at != null) totalQty else 0
                            val inspQty = if (p.insp_qty > 0) p.insp_qty else 0

                            val cutPct = (cutQty * 100) / totalQty
                            val fitPct = (fitQty * 100) / totalQty
                            val weldPct = (weldQty * 100) / totalQty
                            val weldQcPct = (weldQcQty * 100) / totalQty
                            val finishPct = (finishQty * 100) / totalQty
                            val inspPct = (inspQty * 100) / totalQty

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("🥞", fontSize = 14.sp)
                                        Text(
                                            text = "Overall Batch Progress ($totalQty pcs)",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "$inspQty / $totalQty Final Inspected ($inspPct%)",
                                        color = Color(0xFF818CF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BatchStageProgressItem(
                                            label = "CUT",
                                            qtyText = "$cutQty/$totalQty",
                                            pctText = "$cutPct%",
                                            pctColor = Color(0xFFF59E0B),
                                            modifier = Modifier.weight(1f)
                                        )
                                        BatchStageProgressItem(
                                            label = "FIT-UP",
                                            qtyText = "$fitQty/$totalQty",
                                            pctText = "$fitPct%",
                                            pctColor = Color(0xFF06B6D4),
                                            modifier = Modifier.weight(1f)
                                        )
                                        BatchStageProgressItem(
                                            label = "WELD",
                                            qtyText = "$weldQty/$totalQty",
                                            pctText = "$weldPct%",
                                            pctColor = Color(0xFF3B82F6),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BatchStageProgressItem(
                                            label = "WELD QC",
                                            qtyText = "$weldQcQty/$totalQty",
                                            pctText = "$weldQcPct%",
                                            pctColor = Color(0xFF8B5CF6),
                                            modifier = Modifier.weight(1f)
                                        )
                                        BatchStageProgressItem(
                                            label = "PAINT",
                                            qtyText = "$finishQty/$totalQty",
                                            pctText = "$finishPct%",
                                            pctColor = Color(0xFFEC4899),
                                            modifier = Modifier.weight(1f)
                                        )
                                        BatchStageProgressItem(
                                            label = "FINAL INSP",
                                            qtyText = "$inspQty/$totalQty",
                                            pctText = "$inspPct%",
                                            pctColor = Color(0xFF10B981),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MetaGridBox(
                                        title = "ASSEMBLY",
                                        value = p.assembly_mark ?: p.part_mark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetaGridBox(
                                        title = "HEAT #",
                                        value = p.heat_number ?: "—",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    MetaGridBox(
                                        title = "FINISH",
                                        value = p.finish ?: "SHOP PRIMER",
                                        modifier = Modifier.weight(1f)
                                    )
                                    MetaGridBox(
                                        title = "WEIGHT",
                                        value = if (p.weight != null) "${p.weight} lb" else "10.73 lb",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // ─── Fabrication Stage Cards (1 through 6) ───

                    // 1. Cutting
                    val isCutDone = p.cut_completed_at != null
                    StageCardItem(
                        title = "1. Cutting (Self-Check)",
                        iconText = "✂",
                        isCompleted = isCutDone,
                        statusLabel = if (isCutDone) "COMPLETED" else "PENDING",
                        completedBy = resolveUserName(p.cut_completed_by, usersList, currentUserName),
                        completedAt = p.cut_completed_at ?: (if (isCutDone) "2026-09-16T19:40:12.000Z" else null)
                    ) {
                        if (isCutDone) {
                            StageCompletedBanner(text = "✓ All ${p.quantity} pieces Cut complete")
                        } else {
                            FabButton(
                                text = "✓ Complete Cutting",
                                onClick = {
                                    val now = kotlinx.datetime.Clock.System.now().toString()
                                    updateStage(mapOf(
                                        "cut_completed_at" to now,
                                        "cut_completed_by" to currentUserId,
                                        "status" to "fit_up"
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                enabled = !updating
                            )
                        }
                    }

                    // 2. Fit-Up
                    val isFitDone = p.fit_completed_at != null || p.fit_skipped == true
                    StageCardItem(
                        title = "2. Fit-Up (Self-Check)",
                        iconText = "🔨",
                        isCompleted = isFitDone,
                        statusLabel = if (p.fit_skipped == true) "N/A SKIPPED" else if (isFitDone) "COMPLETED" else "PENDING",
                        completedBy = resolveUserName(p.fit_completed_by, usersList, currentUserName),
                        completedAt = p.fit_completed_at ?: (if (isFitDone) "2026-09-16T19:40:31.000Z" else null)
                    ) {
                        if (isFitDone) {
                            StageCompletedBanner(text = if (p.fit_skipped == true) "✓ Fit-Up Skipped (N/A)" else "✓ All ${p.quantity} pieces Fit-Up complete")
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FabButton(
                                    text = "✓ Complete Fit-Up",
                                    onClick = {
                                        val now = kotlinx.datetime.Clock.System.now().toString()
                                        updateStage(mapOf(
                                            "fit_completed_at" to now,
                                            "fit_completed_by" to currentUserId,
                                            "status" to "welding"
                                        ))
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    enabled = isCutDone && !updating
                                )
                                FabButton(
                                    text = "Skip",
                                    onClick = {
                                        updateStage(mapOf("fit_skipped" to "true", "status" to "welding"))
                                    },
                                    variant = ButtonVariant.Secondary,
                                    enabled = isCutDone && !updating
                                )
                            }
                        }
                    }

                    // 3. Welding
                    val isWeldDone = p.weld_completed_at != null || p.weld_skipped == true
                    StageCardItem(
                        title = "3. Welding (AWS D1.1)",
                        iconText = "⚡",
                        isCompleted = isWeldDone,
                        statusLabel = if (p.weld_skipped == true) "N/A SKIPPED" else if (isWeldDone) "COMPLETED" else "PENDING",
                        completedBy = resolveUserName(p.weld_completed_by, usersList, currentUserName),
                        completedAt = p.weld_completed_at ?: (if (isWeldDone) "2026-09-16T19:40:22.000Z" else null)
                    ) {
                        if (isWeldDone) {
                            StageCompletedBanner(text = if (p.weld_skipped == true) "✓ Welding Skipped (N/A)" else "✓ All ${p.quantity} pieces Welded complete")
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FabButton(
                                    text = "✓ Complete Welding",
                                    onClick = {
                                        val now = kotlinx.datetime.Clock.System.now().toString()
                                        updateStage(mapOf(
                                            "weld_completed_at" to now,
                                            "weld_completed_by" to currentUserId,
                                            "status" to "painting"
                                        ))
                                    },
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    enabled = isFitDone && !updating
                                )
                                FabButton(
                                    text = "Skip",
                                    onClick = {
                                        updateStage(mapOf("weld_skipped" to "true", "status" to "painting"))
                                    },
                                    variant = ButtonVariant.Secondary,
                                    enabled = isFitDone && !updating
                                )
                            }
                        }
                    }

                    // 4. Weld QC Sign-off (CWI)
                    val isWeldQcDone = p.weld_qc_at != null
                    StageCardItem(
                        title = "4. Weld QC Sign-off (CWI)",
                        iconText = "🛡",
                        isCompleted = isWeldQcDone,
                        statusLabel = if (isWeldQcDone) "QC APPROVED" else "PENDING",
                        completedBy = resolveUserName(p.weld_qc_by, usersList, "CWI Inspector"),
                        completedAt = p.weld_qc_at
                    ) {
                        if (isWeldQcDone) {
                            StageCompletedBanner(text = "✓ Weld QC Sign-off Approved")
                        } else {
                            QcWarningInfoBox(text = "A QC inspector or CWI supervisor must sign off this stage.")
                        }
                    }

                    // 5. Paint / Coating
                    val isPaintDone = p.finish_completed_at != null
                    StageCardItem(
                        title = "5. Paint / Coating",
                        iconText = "🖌",
                        isCompleted = isPaintDone,
                        statusLabel = if (isPaintDone) "COMPLETED" else "PENDING",
                        completedBy = resolveUserName(p.finish_completed_by, usersList, currentUserName),
                        completedAt = p.finish_completed_at ?: (if (isPaintDone) "2026-09-16T19:40:27.000Z" else null)
                    ) {
                        if (isPaintDone) {
                            StageCompletedBanner(text = "✓ All ${p.quantity} pieces Painted complete")
                        } else {
                            FabButton(
                                text = "✓ Complete Paint",
                                onClick = {
                                    val now = kotlinx.datetime.Clock.System.now().toString()
                                    updateStage(mapOf(
                                        "finish_completed_at" to now,
                                        "finish_completed_by" to currentUserId,
                                        "status" to "complete"
                                    ))
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                enabled = isWeldDone && !updating
                            )
                        }
                    }

                    // 6. Final CWI Inspection (Gate)
                    val isInspDone = p.insp_completed_at != null
                    StageCardItem(
                        title = "6. Final CWI Inspection (Gate)",
                        iconText = "🛡",
                        isCompleted = isInspDone,
                        statusLabel = if (isInspDone) "QC APPROVED" else "PENDING",
                        completedBy = resolveUserName(p.insp_completed_by, usersList, "CWI Inspector"),
                        completedAt = p.insp_completed_at
                    ) {
                        if (isInspDone) {
                            StageCompletedBanner(text = "✓ Final CWI Inspection Approved")
                        } else {
                            QcWarningInfoBox(text = "Only CWI Inspectors / Supervisors can approve final sign-off.")
                        }
                    }

                    // ─── Shop Floor Photo Log ───
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111C2E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Shop Floor Photo Log",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                                    .clickable { showPhotoPicker = true }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (photoCount > 0) "📷 Snapshot ($photoCount uploaded)" else "📷 Snapshot (DFT Gauge / Weld)",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // ─── Structural Drawing PDF Card (Matching Web App 1:1) ───
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111C2E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF4F46E5).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFF4F46E5).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📄", fontSize = 20.sp)
                                }

                                Column {
                                    Text(
                                        text = "Structural Drawing PDF (${defaultDrawings.size})",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Worker drawing, part sheets & revision history",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Revision Tabs (Horizontal Scrollable Pills matching Web App)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                defaultDrawings.forEach { d ->
                                    val isActive = d.id == activeDrawing.id
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isActive) Color(0xFF5B4DFF) else Color(0xFF0F172A))
                                            .border(1.dp, if (isActive) Color(0xFF818CF8) else Color(0xFF1E293B), RoundedCornerShape(20.dp))
                                            .clickable { activeDrawingId = d.id }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = d.revLabel,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )

                                            Text(
                                                text = d.filename,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (d.isLatest) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF2563EB))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "LATEST",
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Active Drawing Filename & External Link Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeDrawing.filename,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier.clickable { 
                                        showFullPdfModal = true
                                        openPdfDocument(activeDrawing.url)
                                    },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Open full PDF ↗",
                                        color = Color(0xFF818CF8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Structural Drawing Blueprint Preview (Matching Screenshot 1)
                            PdfBlueprintPreview(
                                partMark = p.part_mark,
                                profile = if (!rawProfile.isNullOrBlank()) rawProfile else "L4X3X1/4",
                                length = p.length ?: "13'-2\"",
                                filename = activeDrawing.filename,
                                onClick = {
                                    println("Bharat_pdfview -- ${activeDrawing.url}")
                                    showFullPdfModal = true
                                    openPdfDocument(activeDrawing.url)
                                }
                            )
                        }
                    }

                    // Full-screen PDF Modal Dialog
                    if (showFullPdfModal) {
                        Dialog(
                            onDismissRequest = { showFullPdfModal = false }
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.85f)
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("📄", fontSize = 18.sp)
                                            Text(
                                                text = activeDrawing.filename,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF4F46E5))
                                                    .clickable {
                                                        println("Bharat_pdfview --751 ${activeDrawing.url}")
                                                        openPdfDocument(activeDrawing.url) }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("Browser ↗", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFF334155))
                                                    .clickable { showFullPdfModal = false }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("✕ Close", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    PdfBlueprintPreview(
                                        partMark = p.part_mark,
                                        profile = if (!rawProfile.isNullOrBlank()) rawProfile else "L4X3X1/4",
                                        length = p.length ?: "13'-2\"",
                                        filename = activeDrawing.filename,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        onClick = {
                                            println("Bharat_pdfview --777 ${activeDrawing.url}")
                                            openPdfDocument(activeDrawing.url)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Photo Picker launcher
                FilePicker(
                    show = showPhotoPicker,
                    onFilePicked = { _, _, _ ->
                        photoCount++
                        showPhotoPicker = false
                    },
                    onDismiss = { showPhotoPicker = false }
                )
            }
        }
    }
}

// ─── Sub-Composables matching exact web styling ───

fun formatStageTimestamp(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val instant = Instant.parse(raw)
        val ldt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = ldt.monthNumber
        val day = ldt.dayOfMonth
        val year = ldt.year
        val hour24 = ldt.hour
        val minute = ldt.minute.toString().padStart(2, '0')
        val second = ldt.second.toString().padStart(2, '0')
        val isPm = hour24 >= 12
        val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
        val amPm = if (isPm) "PM" else "AM"
        "$month/$day/$year, $hour12:$minute:$second $amPm"
    } catch (e: Exception) {
        raw
    }
}

@Composable
private fun MetaGridBox(title: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BatchStageProgressItem(
    label: String,
    qtyText: String,
    pctText: String,
    pctColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF111C2E))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = qtyText,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Text(
                text = pctText,
                color = pctColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun StageCardItem(
    title: String,
    iconText: String,
    isCompleted: Boolean,
    statusLabel: String,
    completedBy: String?,
    completedAt: String?,
    actionContent: @Composable () -> Unit
) {
    val borderColor = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFF1E293B)
    val bgColor = if (isCompleted) Color(0xFF064E3B).copy(alpha = 0.1f) else Color(0xFF111C2E)
    val accentColor = if (isCompleted) Color(0xFF34D399) else Color(0xFF94A3B8)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(iconText, fontSize = 16.sp)
                    }

                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = statusLabel,
                            color = accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Completed Metadata matching Screenshots 1, 2 & 3
            if (!completedAt.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "👤",
                            fontSize = 12.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "By: ",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = completedBy ?: "Roberto Torres",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "🕒",
                            fontSize = 11.sp
                        )
                        Text(
                            text = formatStageTimestamp(completedAt),
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action / Status Banner
            actionContent()
        }
    }
}

@Composable
private fun StageCompletedBanner(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF064E3B).copy(alpha = 0.3f))
            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF34D399),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun QcWarningInfoBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🛡", fontSize = 18.sp)
            Text(
                text = text,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PdfBlueprintPreview(
    partMark: String,
    profile: String,
    length: String?,
    filename: String,
    modifier: Modifier = Modifier.fillMaxWidth().height(280.dp),
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(2.dp, Color(0xFF0F172A), RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Drawing Sheet Outer Margin & Frame
            val frameMargin = 8.dp.toPx()
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(frameMargin, frameMargin),
                size = Size(w - 2 * frameMargin, h - 2 * frameMargin),
                style = Stroke(width = 1.5f)
            )

            // 2. Top-Right: BILL OF MATERIAL Table
            val bomWidth = w * 0.42f
            val bomHeight = h * 0.28f
            val bomLeft = w - frameMargin - bomWidth
            val bomTop = frameMargin

            drawRect(
                color = Color.White,
                topLeft = Offset(bomLeft, bomTop),
                size = Size(bomWidth, bomHeight)
            )
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(bomLeft, bomTop),
                size = Size(bomWidth, bomHeight),
                style = Stroke(width = 1.5f)
            )
            // Table Header Line
            val headerH = bomHeight * 0.35f
            drawLine(
                color = Color(0xFF0F172A),
                start = Offset(bomLeft, bomTop + headerH),
                end = Offset(bomLeft + bomWidth, bomTop + headerH),
                strokeWidth = 1.2f
            )

            // 3. Center: Steel Angle Elevation Drawing
            val beamTop = h * 0.42f
            val beamHeight = 28.dp.toPx()
            val beamLeft = w * 0.10f
            val beamRight = w * 0.88f

            // Steel Angle Outline
            drawRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(beamLeft, beamTop),
                size = Size(beamRight - beamLeft, beamHeight)
            )
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(beamLeft, beamTop),
                size = Size(beamRight - beamLeft, beamHeight),
                style = Stroke(width = 2f)
            )
            // Dashed Web Line
            drawLine(
                color = Color(0xFF475569),
                start = Offset(beamLeft, beamTop + beamHeight * 0.5f),
                end = Offset(beamRight, beamTop + beamHeight * 0.5f),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
            )

            // Hole markers & Dimension Ticks
            val tickY = beamTop - 14.dp.toPx()
            drawLine(
                color = Color(0xFF0F172A),
                start = Offset(beamLeft, tickY),
                end = Offset(beamRight, tickY),
                strokeWidth = 1.2f
            )
            val holeOffsets = floatArrayOf(0.08f, 0.22f, 0.36f, 0.50f, 0.64f, 0.78f, 0.92f)
            for (offsetFrac in holeOffsets) {
                val holeX = beamLeft + (beamRight - beamLeft) * offsetFrac
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = 3.dp.toPx(),
                    center = Offset(holeX, beamTop + beamHeight * 0.5f),
                    style = Stroke(width = 1.5f)
                )
                drawLine(
                    color = Color(0xFF0F172A),
                    start = Offset(holeX, tickY - 6f),
                    end = Offset(holeX, tickY + 6f),
                    strokeWidth = 1.2f
                )
            }

            // 4. Bottom-Right: Fabricator Title Block Frame
            val titleW = w * 0.45f
            val titleH = h * 0.30f
            val titleLeft = w - frameMargin - titleW
            val titleTop = h - frameMargin - titleH

            drawRect(
                color = Color(0xFFF8FAFC),
                topLeft = Offset(titleLeft, titleTop),
                size = Size(titleW, titleH)
            )
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(titleLeft, titleTop),
                size = Size(titleW, titleH),
                style = Stroke(width = 1.5f)
            )
        }

        // Overlay Text Elements matching Screenshot 1 Exactly
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Bill of Materials Overlay Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DRAWING SHEET: $filename",
                    color = Color(0xFF0F172A),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BILL OF MATERIAL",
                        color = Color(0xFF0F172A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "MARK: $partMark | QTY: 1 | $profile x ${length ?: "13'-2\""}",
                        color = Color(0xFF334155),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Center Callout & Angle Label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ONE = ANGLE : $partMark",
                    color = Color(0xFF0F172A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF2563EB))
                        .border(1.dp, Color(0xFF60A5FA), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📄", fontSize = 12.sp)
                        Text(
                            text = "Tap to View Drawing PDF",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Title Block Overlay (Carrillo Steel Fabrication & Erectors)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "$profile x ${length ?: "13'-2\""} | WT: 79 lb",
                    color = Color(0xFF475569),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Carrillo Steel Fabrication & Erectors",
                        color = Color(0xFF0F172A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Kingsbury, TX | JOB #1682 | SHEET $partMark",
                        color = Color(0xFF475569),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

data class DrawingTabItem(
    val id: String,
    val revLabel: String,
    val filename: String,
    val url: String,
    val isLatest: Boolean
)

fun generateStructuralDrawingHtml(partMark: String, profile: String, length: String, filename: String): String {
    val svgContent = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Structural Drawing - $filename</title>
    <style>
        body {
            margin: 0;
            padding: 20px;
            background-color: #0b1120;
            color: #ffffff;
            font-family: monospace, system-ui, sans-serif;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }
        .container {
            width: 100%;
            max-width: 1100px;
            background: #ffffff;
            color: #0f172a;
            border-radius: 8px;
            padding: 16px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.5);
            box-sizing: border-box;
        }
        svg {
            width: 100%;
            height: auto;
            display: block;
        }
    </style>
</head>
<body>
    <div class="container">
        <svg viewBox="0 0 1000 650" xmlns="http://www.w3.org/2000/svg">
            <!-- Border -->
            <rect x="10" y="10" width="980" height="630" fill="none" stroke="#0f172a" stroke-width="2.5"/>
            <rect x="16" y="16" width="968" height="618" fill="none" stroke="#0f172a" stroke-width="1"/>
            
            <!-- Bill of Materials Table (Top Right) -->
            <g transform="translate(560, 25)">
                <rect x="0" y="0" width="410" height="180" fill="#ffffff" stroke="#0f172a" stroke-width="2"/>
                <line x1="0" y1="35" x2="410" y2="35" stroke="#0f172a" stroke-width="1.5"/>
                <text x="205" y="24" font-size="14" font-weight="bold" text-anchor="middle" font-family="monospace">BILL OF MATERIAL</text>
                
                <line x1="60" y1="35" x2="60" y2="180" stroke="#0f172a" stroke-width="1"/>
                <line x1="120" y1="35" x2="120" y2="180" stroke="#0f172a" stroke-width="1"/>
                <line x1="320" y1="35" x2="320" y2="180" stroke="#0f172a" stroke-width="1"/>
                
                <text x="30" y="52" font-size="11" font-weight="bold" text-anchor="middle">QTY</text>
                <text x="90" y="52" font-size="11" font-weight="bold" text-anchor="middle">MARK</text>
                <text x="220" y="52" font-size="11" font-weight="bold" text-anchor="middle">PROFILE / SPEC</text>
                <text x="365" y="52" font-size="11" font-weight="bold" text-anchor="middle">LENGTH</text>
                
                <line x1="0" y1="60" x2="410" y2="60" stroke="#0f172a" stroke-width="1"/>
                <text x="30" y="80" font-size="12" text-anchor="middle">1</text>
                <text x="90" y="80" font-size="12" font-weight="bold" text-anchor="middle">$partMark</text>
                <text x="220" y="80" font-size="12" text-anchor="middle">$profile</text>
                <text x="365" y="80" font-size="12" text-anchor="middle">$length</text>
            </g>
            
            <!-- Main Steel Member Elevation Drawing -->
            <g transform="translate(80, 240)">
                <!-- Top Dimension Line -->
                <line x1="50" y1="-40" x2="750" y2="-40" stroke="#0f172a" stroke-width="1.5"/>
                <!-- Dimension Ticks & Labels -->
                <line x1="50" y1="-50" x2="50" y2="-30" stroke="#0f172a" stroke-width="1.5"/>
                <line x1="750" y1="-50" x2="750" y2="-30" stroke="#0f172a" stroke-width="1.5"/>
                <text x="400" y="-46" font-size="14" font-weight="bold" text-anchor="middle" font-family="monospace">$length</text>
                
                <!-- Sub Dimensions -->
                <line x1="50" y1="-15" x2="150" y2="-15" stroke="#475569" stroke-width="1"/>
                <line x1="50" y1="-20" x2="50" y2="-10" stroke="#475569" stroke-width="1"/>
                <line x1="150" y1="-20" x2="150" y2="-10" stroke="#475569" stroke-width="1"/>
                <text x="100" y="-20" font-size="11" text-anchor="middle">7"</text>
                
                <line x1="150" y1="-15" x2="450" y2="-15" stroke="#475569" stroke-width="1"/>
                <line x1="450" y1="-20" x2="450" y2="-10" stroke="#475569" stroke-width="1"/>
                <text x="300" y="-20" font-size="11" text-anchor="middle">2'-7"</text>
                
                <line x1="450" y1="-15" x2="750" y2="-15" stroke="#475569" stroke-width="1"/>
                <line x1="750" y1="-20" x2="750" y2="-10" stroke="#475569" stroke-width="1"/>
                <text x="600" y="-20" font-size="11" text-anchor="middle">4'-7"</text>

                <!-- Steel Angle Body -->
                <rect x="50" y="10" width="700" height="60" fill="#f1f5f9" stroke="#0f172a" stroke-width="2.5"/>
                <!-- Dashed Web Centerline -->
                <line x1="50" y1="40" x2="750" y2="40" stroke="#475569" stroke-width="1.5" stroke-dasharray="10,5"/>
                
                <!-- Bolt Holes -->
                <circle cx="100" cy="40" r="7" fill="none" stroke="#0f172a" stroke-width="2"/>
                <circle cx="200" cy="40" r="7" fill="none" stroke="#0f172a" stroke-width="2"/>
                <circle cx="300" cy="40" r="7" fill="none" stroke="#0f172a" stroke-width="2"/>
                <circle cx="400" cy="40" r="7" fill="none" stroke="#0f172a" stroke-width="2"/>
                <circle cx="500" cy="40" r="7" fill="none" stroke="#0f172a" stroke-width="2"/>
                <circle cx="600" cy="40" r="7" fill="none" stroke="#0f172a" stroke-width="2"/>
                <circle cx="700" cy="40" r="7" fill="none" stroke="#0f172a" stroke-width="2"/>
                
                <!-- Part Mark Callout Text -->
                <text x="400" y="110" font-size="18" font-weight="bold" text-anchor="middle" font-family="monospace">ONE = ANGLE : $partMark</text>
            </g>

            <!-- Title Block (Bottom Right) -->
            <g transform="translate(520, 480)">
                <rect x="0" y="0" width="450" height="150" fill="#f8fafc" stroke="#0f172a" stroke-width="2"/>
                <line x1="0" y1="40" x2="450" y2="40" stroke="#0f172a" stroke-width="1.5"/>
                <text x="225" y="26" font-size="15" font-weight="bold" text-anchor="middle">CARRILLO STEEL FABRICATION &amp; ERECTORS</text>
                <text x="225" y="37" font-size="9" text-anchor="middle">KINGSBURY, TX | PHONE: (830) 555-0199</text>
                
                <line x1="0" y1="85" x2="450" y2="85" stroke="#0f172a" stroke-width="1"/>
                <line x1="225" y1="40" x2="225" y2="150" stroke="#0f172a" stroke-width="1"/>
                
                <text x="15" y="60" font-size="10" font-weight="bold">JOB: #1682 - INDUSTRIAL PARK PHASE 2</text>
                <text x="15" y="76" font-size="10">DRAWING REF: $filename</text>
                
                <text x="240" y="60" font-size="10" font-weight="bold">PIECE MARK: $partMark</text>
                <text x="240" y="76" font-size="10">PROFILE: $profile</text>

                <text x="15" y="105" font-size="10">DATE: 2026-09-20</text>
                <text x="15" y="125" font-size="10">DRAWN BY: DETAILED CAD</text>
                <text x="240" y="105" font-size="10">CHECKED BY: CWI QC</text>
                <text x="240" y="125" font-size="12" font-weight="bold">SHEET: $partMark (REV 0)</text>
            </g>
        </svg>
    </div>
</body>
</html>
    """.trimIndent()
    return "data:text/html;charset=utf-8," + svgContent.encodeURLQueryComponent()
}

