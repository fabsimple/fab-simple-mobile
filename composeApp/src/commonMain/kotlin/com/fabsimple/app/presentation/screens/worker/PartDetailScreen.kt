package com.fabsimple.app.presentation.screens.worker

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.components.StatusPill
import com.fabsimple.app.components.ButtonVariant
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

        fun updateStatus(newStatus: String, timestampField: String) {
            updating = true
            coroutineScope.launch {
                try {
                    val payload = mapOf(
                        "status" to newStatus,
                        timestampField to kotlinx.datetime.Clock.System.now().toString()
                    )
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { navigator.pop() }) {
                            Text("← Back", color = Color(0xFF818CF8), fontSize = 16.sp)
                        }

                        Text(
                            text = "Part Details",
                            color = Color.White,
                            style = FabType.sectionTitle.copy(color = Color.White)
                        )
                    }

                    // Card Meta
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = p.part_mark,
                                color = Color.White,
                                style = FabType.pageTitle.copy(color = Color.White)
                            )

                            CompositionLocalProvider(LocalStatusColors provides DarkStatusColors) {
                                StatusPill(status = p.status)
                            }

                            Divider(color = Color.White.copy(alpha = 0.05f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Profile", color = Color(0xFF64748B), style = FabType.cardSub)
                                    Text(p.profile, color = Color.White, style = FabType.button)
                                }
                                Column {
                                    Text("Grade", color = Color(0xFF64748B), style = FabType.cardSub)
                                    Text(p.grade ?: "—", color = Color.White, style = FabType.button)
                                }
                                Column {
                                    Text("Length", color = Color(0xFF64748B), style = FabType.cardSub)
                                    Text(p.length?.toString() ?: "—", color = Color.White, style = FabType.button)
                                }
                            }
                        }
                    }

                    // Operations
                    Text(
                        text = "ACTIONS",
                        color = Color(0xFF475569),
                        style = FabType.infoCellLabel
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (p.cut_completed_at == null) {
                            FabButton(
                                text = "Complete Cutting",
                                onClick = { updateStatus("fit_up", "cut_completed_at") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !updating
                            )
                        } else if (p.fit_completed_at == null && p.fit_skipped != true) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FabButton(
                                    text = "Complete Fit-Up",
                                    onClick = { updateStatus("welding", "fit_completed_at") },
                                    modifier = Modifier.weight(1f),
                                    enabled = !updating
                                )
                                FabButton(
                                    text = "Skip",
                                    onClick = { updateStatus("welding", "fit_skipped") },
                                    variant = ButtonVariant.Secondary,
                                    enabled = !updating
                                )
                            }
                        } else if (p.weld_completed_at == null && p.weld_skipped != true) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FabButton(
                                    text = "Complete Welding",
                                    onClick = { updateStatus("painting", "weld_completed_at") },
                                    modifier = Modifier.weight(1f),
                                    enabled = !updating
                                )
                                FabButton(
                                    text = "Skip",
                                    onClick = { updateStatus("painting", "weld_skipped") },
                                    variant = ButtonVariant.Secondary,
                                    enabled = !updating
                                )
                            }
                        } else if (p.finish_completed_at == null) {
                            FabButton(
                                text = "Complete Painting",
                                onClick = { updateStatus("inspection", "finish_completed_at") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !updating
                            )
                        } else if (p.insp_completed_at == null) {
                            FabButton(
                                text = "Complete QC Inspection",
                                onClick = { updateStatus("complete", "insp_completed_at") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !updating
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF064E3B), RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Part production completed", color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
