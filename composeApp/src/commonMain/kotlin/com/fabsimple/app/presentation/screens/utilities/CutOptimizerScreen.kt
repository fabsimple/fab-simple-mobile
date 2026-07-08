package com.fabsimple.app.presentation.screens.utilities

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.fabsimple.app.components.FabTextField
import com.fabsimple.app.components.ButtonVariant
import com.fabsimple.app.components.ButtonSize
import com.fabsimple.app.theme.*
import com.fabsimple.shared.data.network.CutInputItem
import com.fabsimple.shared.data.network.CutOptimizeRequest
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.CutPlan
import kotlinx.coroutines.launch

class CutOptimizerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        var stockLength by remember { mutableStateOf("240") }
        var kerf by remember { mutableStateOf("0.125") }
        var cutLength by remember { mutableStateOf("") }
        var cutQty by remember { mutableStateOf("1") }
        val cutsList = remember { mutableStateListOf<CutInputItem>() }

        var plan by remember { mutableStateOf<CutPlan?>(null) }
        var loading by remember { mutableStateOf(false) }

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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "1D Cut List Optimizer",
                        color = Color.White,
                        style = FabType.sectionTitle.copy(color = Color.White)
                    )
                }

                // Inputs
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FabTextField(
                                value = stockLength,
                                onValueChange = { stockLength = it },
                                label = "Stock Length (in)",
                                modifier = Modifier.weight(1f)
                            )
                            FabTextField(
                                value = kerf,
                                onValueChange = { kerf = it },
                                label = "Kerf Width (in)",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Add Cut Item form
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            FabTextField(
                                value = cutLength,
                                onValueChange = { cutLength = it },
                                label = "Cut Length (in)",
                                modifier = Modifier.weight(1f)
                            )
                            FabTextField(
                                value = cutQty,
                                onValueChange = { cutQty = it },
                                label = "Qty",
                                modifier = Modifier.width(60.dp)
                            )
                            FabButton(
                                text = "+ Add",
                                onClick = {
                                    val len = cutLength.toDoubleOrNull()
                                    val qty = cutQty.toIntOrNull() ?: 1
                                    if (len != null) {
                                        cutsList.add(CutInputItem(len, qty))
                                        cutLength = ""
                                        cutQty = "1"
                                    }
                                },
                                size = ButtonSize.Medium
                            )
                        }
                    }
                }

                // Added cuts list
                if (cutsList.isNotEmpty()) {
                    Text("Cuts Requested:", color = Color.White, style = FabType.cardTitle.copy(color = Color.White))

                    cutsList.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.qty} pcs @ ${item.length} in",
                                color = Color.White,
                                style = FabType.body
                            )
                            TextButton(onClick = { cutsList.removeAt(index) }) {
                                Text("Delete", color = FabColors.Red)
                            }
                        }
                    }

                    FabButton(
                        text = if (loading) "Optimizing…" else "Run Optimizer",
                        onClick = {
                            loading = true
                            coroutineScope.launch {
                                try {
                                    val req = CutOptimizeRequest(
                                        profile = "HSS",
                                        stock_length = stockLength.toDoubleOrNull() ?: 240.0,
                                        kerf = kerf.toDoubleOrNull() ?: 0.125,
                                        cuts = cutsList.toList()
                                    )
                                    plan = AppContainer.cutOptimizerRepository.optimize(req)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading
                    )
                }

                // Results view
                plan?.let { p ->
                    Text("Optimization Result:", color = Color.White, style = FabType.sectionTitle.copy(color = Color.White))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Yield", style = FabType.cardSub)
                                Text("${p.yield_percentage}%", style = FabType.sectionTitle.copy(color = Color.White))
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Bars Needed", style = FabType.cardSub)
                                Text("${p.total_bars}", style = FabType.sectionTitle.copy(color = Color.White))
                            }
                        }
                    }

                    p.bars.forEach { bar ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Bar #${bar.bar_index + 1} - used ${bar.used_length} in", style = FabType.cardTitle.copy(color = Color.White))
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    bar.cuts.forEach { cut ->
                                        Box(
                                            modifier = Modifier
                                                .weight(cut.length.toFloat())
                                                .height(24.dp)
                                                .background(Color(0xFF4F46E5), RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${cut.length}\"", color = Color.White, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
