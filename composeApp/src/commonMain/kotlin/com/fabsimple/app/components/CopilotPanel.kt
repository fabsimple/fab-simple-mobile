package com.fabsimple.app.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabsimple.app.theme.*
import com.fabsimple.shared.domain.model.ChatMessage

val SUGGESTIONS = listOf(
    "Show me parts that are blocking shipping this week",
    "Summarize open NCRs and their root causes",
    "Which projects are at risk of missing their deadline?",
    "What's the AWS weld inspection rate this month?"
)

/**
 * CopilotPanel — Side drawer for AI Copilot chat.
 * Replicates components/CopilotPanel.tsx
 */
@Composable
fun CopilotPanel(
    open: Boolean,
    messages: List<ChatMessage>,
    input: String,
    onInputChange: (String) -> Unit,
    onSend: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    error: String? = null
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, busy) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Overlay backdrop click to close
        if (open) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onClose() }
            )
        }

        // Sliding Drawer panel
        AnimatedVisibility(
            visible = open,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Card(
                modifier = modifier
                    .fillMaxHeight()
                    .width(420.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .border(1.dp, FabColors.Border, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = FabColors.Border)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED)))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✨", color = Color.White, fontSize = 16.sp)
                            }

                            Column {
                                Text(
                                    text = "FabSimple Copilot",
                                    style = FabType.cardTitle
                                )
                                Text(
                                    text = "AISC · AWS · OSHA aware",
                                    style = FabType.cardSub
                                )
                            }
                        }

                        IconButton(onClick = onClose) {
                            Text("✕", style = FabType.modalTitle, color = FabColors.TextMuted)
                        }
                    }

                    // Messages Scrollable Feed
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(FabColors.Background)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Try asking…",
                                        style = FabType.sectionTitle,
                                        fontSize = 14.sp
                                    )

                                    SUGGESTIONS.forEach { suggest ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onSend(suggest) }
                                                .border(1.dp, FabColors.Border, RoundedCornerShape(8.dp)),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground)
                                        ) {
                                            Text(
                                                text = suggest,
                                                style = FabType.bodySmall,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            items(messages) { msg ->
                                val isUser = msg.role == "user"
                                val alignment = if (isUser) Alignment.End else Alignment.Start
                                val bg = if (isUser) FabColors.Primary else FabTheme.extendedColors.cardBackground
                                val textColor = if (isUser) Color.White else FabColors.TextPrimary

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = alignment
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .then(
                                                if (!isUser) Modifier.border(1.dp, FabColors.Border, RoundedCornerShape(8.dp))
                                                else Modifier
                                            ),
                                        colors = CardDefaults.cardColors(containerColor = bg),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = msg.content,
                                            style = FabType.bodySmall.copy(color = textColor),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (busy) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .background(FabTheme.extendedColors.cardBackground, RoundedCornerShape(8.dp))
                                        .border(1.dp, FabColors.Border, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    CircularProgressIndicator(color = FabColors.Primary, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                    Text("Thinking…", style = FabType.bodySmall, color = FabColors.TextMuted)
                                }
                            }
                        }

                        if (error != null) {
                            item {
                                AlertBanner(message = error, variant = AlertVariant.Danger)
                            }
                        }
                    }

                    // Input Form
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = FabColors.Border)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FabTextField(
                            value = input,
                            onValueChange = onInputChange,
                            placeholder = "Ask anything about your shop…",
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(
                                    onClick = { onSend(input) },
                                    enabled = !busy && input.isNotBlank()
                                ) {
                                    Text(
                                        text = "▶",
                                        color = if (input.isNotBlank() && !busy) FabColors.Primary else FabColors.TextFaint,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Floating button to open Copilot launcher in standard dashboards.
 */
@Composable
fun CopilotLauncher(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = Color.Transparent,
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨",
                fontSize = 22.sp,
                color = Color.White
            )
        }
    }
}
