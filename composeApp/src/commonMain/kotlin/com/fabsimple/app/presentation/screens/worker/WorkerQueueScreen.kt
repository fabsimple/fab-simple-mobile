package com.fabsimple.app.presentation.screens.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.fabsimple.app.components.StatusPill
import com.fabsimple.app.presentation.screens.auth.LoginScreen
import com.fabsimple.app.presentation.screens.utilities.CutOptimizerScreen
import com.fabsimple.app.presentation.screens.utilities.CopilotScreen
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.Part
import kotlinx.coroutines.launch

class WorkerQueueScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        val session = remember { AppContainer.authRepository.getSession() }
        val currentUserId = session?.userId ?: ""

        var parts by remember { mutableStateOf<List<Part>>(emptyList()) }
        var loading by remember { mutableStateOf(true) }
        var activeStation by remember { mutableStateOf("assigned") }
        var isOnline by remember { mutableStateOf(AppContainer.partRepository.isOnline()) }

        val stations = listOf(
            "assigned" to "My Assigned",
            "cut" to "Cutting",
            "fit" to "Fit-Up",
            "weld" to "Welding",
            "paint" to "Painting",
            "insp" to "Inspection"
        )

        fun fetchQueue() {
            loading = true
            coroutineScope.launch {
                try {
                    parts = AppContainer.partRepository.getParts()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    loading = false
                }
            }
        }

        LaunchedEffect(Unit) {
            fetchQueue()
        }

        val filteredParts = parts.filter { p ->
            when (activeStation) {
                "assigned" -> p.assigned_user_id == currentUserId
                "cut" -> p.cut_completed_at == null
                "fit" -> p.cut_completed_at != null && p.fit_completed_at == null && p.fit_skipped != true
                "weld" -> (p.fit_completed_at != null || p.fit_skipped == true) && p.weld_completed_at == null && p.weld_skipped != true
                "paint" -> (p.weld_completed_at != null || p.weld_skipped == true) && p.finish_completed_at == null
                "insp" -> p.finish_completed_at != null && p.insp_completed_at == null
                else -> true
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1120))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 76.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Shop Floor Queue",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${session?.name ?: "Worker"} · ${session?.role ?: "worker"}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOnline) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(
                                text = if (isOnline) "Online" else "Offline",
                                color = if (isOnline) Color(0xFF34D399) else Color(0xFFFBBF24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        isOnline = !isOnline
                                        AppContainer.partRepository.setOnline(isOnline)
                                        fetchQueue()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                AppContainer.authRepository.signOut()
                                navigator.replaceAll(LoginScreen())
                            },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp))
                        ) {
                            Text("🚪", fontSize = 14.sp)
                        }
                    }
                }

                // Large Scan button
                Button(
                    onClick = {
                        navigator.push(ScannerScreen())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text("🔳", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan Part QR Code",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Station selects scroll bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stations.forEach { (id, label) ->
                        val isSelected = activeStation == id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFF6366F1).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f),
                            border = ButtonDefaults.outlinedButtonBorder,
                            modifier = Modifier
                                .clickable { activeStation = id }
                                .height(46.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color(0xFF818CF8) else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }
                    }
                }

                // Parts title
                Text(
                    text = "Parts (${filteredParts.size})",
                    color = Color(0xFF475569),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF818CF8))
                    }
                } else if (filteredParts.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth()
                        ) {
                            Text("✓", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                            Text("All caught up", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                            Text("No parts pending at this station.", color = Color(0xFF64748B), fontSize = 12.5.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredParts) { part ->
                            PartRowItem(part = part) {
                                navigator.push(PartDetailScreen(part.id))
                            }
                        }
                    }
                }
            }

            // Bottom Utilities Bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { navigator.push(CutOptimizerScreen()) }) {
                    Text("1D Cut Optimizer", color = Color(0xFF818CF8), fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
                TextButton(onClick = { navigator.push(CopilotScreen()) }) {
                    Text("AI Copilot", color = Color(0xFF818CF8), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PartRowItem(part: Part, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = part.part_mark,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = MonoFontFamily
                )
                Text(
                    text = "${part.profile} · ${part.heat_number ?: "no heat"}",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.5.sp
                )
                // Use dark status colors in worker dark theme
                CompositionLocalProvider(LocalStatusColors provides DarkStatusColors) {
                    StatusPill(status = part.status)
                }
            }
            Text("→", color = Color(0xFF64748B), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}
