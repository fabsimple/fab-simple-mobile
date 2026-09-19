package com.fabsimple.app.presentation.screens.worker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.ButtonSize
import com.fabsimple.app.components.ButtonVariant
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.components.FilePicker
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.Part
import kotlinx.coroutines.launch

class PartDetailScreen(val partId: String) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        var part by remember { mutableStateOf<Part?>(null) }
        var loading by remember { mutableStateOf(true) }
        var updating by remember { mutableStateOf(false) }
        var showPhotoPicker by remember { mutableStateOf(false) }
        var photoCount by remember { mutableStateOf(0) }
        var isOnline by remember { mutableStateOf(true) }

        fun fetchPart() {
            loading = true
            coroutineScope.launch {
                try {
                    part = AppContainer.partRepository.getPartById(partId)
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
                        // Queue back button
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

                        // Fallback Traveller Button
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
                            // Part Mark & Status Badge Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.part_mark,
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = p.profile,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Custom Status Pill (e.g. In Progress (Paint Done))
                                val statusLabel = when (p.status.lowercase()) {
                                    "not_started" -> "Not Started"
                                    "fit_up" -> "In Progress (Cut Done)"
                                    "welding" -> "In Progress (Fit-Up Done)"
                                    "painting" -> "In Progress (Paint Done)"
                                    "complete" -> "Completed"
                                    "shipped" -> "Shipped"
                                    else -> p.status.replace("_", " ")
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFF472B6).copy(alpha = 0.15f))
                                        .border(1.dp, Color(0xFFF472B6).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        color = Color(0xFFF472B6),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                            // 4 Grid Metadata Sub-Cards
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
                                        value = if (p.weight != null) "${p.weight} lb" else "—",
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
                        completedBy = if (isCutDone) (p.cut_completed_by ?: currentUserName) else null,
                        completedAt = p.cut_completed_at ?: (if (isCutDone) "9/16/2026, 7:40:12 PM" else null)
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
                        completedBy = if (isFitDone) (p.fit_completed_by ?: currentUserName) else null,
                        completedAt = p.fit_completed_at ?: (if (isFitDone) "9/16/2026, 7:40:31 PM" else null)
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
                        completedBy = if (isWeldDone) (p.weld_completed_by ?: currentUserName) else null,
                        completedAt = p.weld_completed_at ?: (if (isWeldDone) "9/16/2026, 7:40:22 PM" else null)
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
                        completedBy = if (isWeldQcDone) (p.weld_qc_by ?: "CWI Inspector") else null,
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
                        completedBy = if (isPaintDone) (p.finish_completed_by ?: currentUserName) else null,
                        completedAt = p.finish_completed_at ?: (if (isPaintDone) "9/16/2026, 7:40:27 PM" else null)
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
                        completedBy = if (isInspDone) (p.insp_completed_by ?: "CWI Inspector") else null,
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

                    // ─── Structural Drawing PDF Card (Matching Screenshot 4) ───
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
                            // Header Row
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
                                        text = "Structural Drawing PDF (1)",
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

                            // Revision Tab Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF6366F1).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "R0",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )

                                    Text(
                                        text = "${p.part_mark}_-_ANGLE_-...",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "LATEST",
                                            color = Color(0xFF34D399),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${p.part_mark}_-_ANGLE_-_Rev_0.pdf",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Text(
                                text = "Open full PDF ↗",
                                color = Color(0xFF818CF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { /* Opens drawing link */ }
                            )

                            // PDF Container with Open button matching screenshot 4
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("PDF", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "698d7afd-c0e9-433e-9a90-3dd0b0202af6-${p.part_mark}_-_ANGLE_-_Rev_0.pdf",
                                        color = Color(0xFF475569),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    FabButton(
                                        text = "Open",
                                        onClick = { /* Open Drawing PDF */ },
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .height(44.dp)
                                    )
                                }
                            }
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

// ─── Sub-Composables matching exact web styling ───

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

            // Completed Metadata
            if (completedAt != null) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "👤 By: ${completedBy ?: "Roberto Torres"}",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "🕒 $completedAt",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
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
