package com.fabsimple.app.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Popup
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.components.FabTextField
import com.fabsimple.app.state.GlobalProjectState
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.Project
import kotlinx.coroutines.launch

/**
 * Global project picker dropdown.
 * Replicates ProjectPicker.tsx in the layout folder.
 */
@Composable
fun ProjectPicker(
    modifier: Modifier = Modifier
) {
    val activeProject by GlobalProjectState.currentProject.collectAsState()
    var open by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var projectsList by remember { mutableStateOf<List<Project>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch projects when dropdown is opened
    LaunchedEffect(open) {
        if (open) {
            loading = true
            try {
                projectsList = AppContainer.projectRepository.getProjects()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                loading = false
            }
        }
    }

    val filtered = projectsList
        .filter { p -> p.status != "awarded_setup" }
        .filter { p ->
            search.isEmpty() ||
            p.name.contains(search, ignoreCase = true) ||
            (p.number ?: "").contains(search, ignoreCase = true)
        }

    val activeLabel = if (activeProject.id != null) {
        "${if (activeProject.number != null) "${activeProject.number} · " else ""}${activeProject.name}"
    } else {
        "All Projects"
    }

    Box(modifier = modifier.wrapContentSize()) {
        // Trigger button
        Row(
            modifier = Modifier
                .clip(FabShapes.Button)
                .background(
                    if (activeProject.id != null) FabColors.PrimaryBg else FabTheme.extendedColors.mutedBackground.copy(alpha = 0.5f)
                )
                .border(
                    width = 1.dp,
                    color = if (activeProject.id != null) FabColors.PrimaryBorder else FabColors.Border,
                    shape = FabShapes.Button
                )
                .clickable { open = !open }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("📁", fontSize = 12.sp)

            Text(
                text = activeLabel,
                style = FabType.buttonSmall.copy(
                    color = if (activeProject.id != null) FabColors.Primary else FabColors.TextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 160.dp)
            )

            if (activeProject.id != null) {
                Text(
                    text = "✕",
                    style = FabType.buttonSmall.copy(
                        color = FabColors.Primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable {
                        GlobalProjectState.clearProject()
                    }
                )
            } else {
                Text("▼", fontSize = 10.sp, color = FabColors.TextMuted)
            }
        }

        // Popup Dropdown
        if (open) {
            Popup(
                alignment = Alignment.BottomStart,
                onDismissRequest = {
                    open = false
                    search = ""
                }
            ) {
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .padding(top = 8.dp)
                        .border(1.dp, FabColors.Border, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Search bar inside dropdown
                        Box(modifier = Modifier.padding(10.dp)) {
                            FabTextField(
                                value = search,
                                onValueChange = { search = it },
                                placeholder = "Search projects…"
                            )
                        }

                        Divider(color = FabColors.Border)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                        ) {
                            // "All Projects" Option
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (activeProject.id == null) FabColors.PrimaryBg else Color.Transparent
                                        )
                                        .clickable {
                                            GlobalProjectState.clearProject()
                                            open = false
                                            search = ""
                                        }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("📁", fontSize = 13.sp)
                                    Text(
                                        text = "All Projects",
                                        style = FabType.buttonSmall.copy(
                                            color = if (activeProject.id == null) FabColors.Primary else FabColors.TextSecondary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (activeProject.id == null) {
                                        Text("✓", color = FabColors.Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                                Divider(color = FabColors.Border)
                            }

                            if (loading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = FabColors.Primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else if (filtered.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No projects found", style = FabType.cardSub)
                                    }
                                }
                            } else {
                                items(filtered) { p ->
                                    val isSelected = p.id == activeProject.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                if (isSelected) FabColors.PrimaryBg else Color.Transparent
                                            )
                                            .clickable {
                                                GlobalProjectState.selectProject(p.id, p.name, p.number)
                                                open = false
                                                search = ""
                                            }
                                            .padding(vertical = 8.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = p.name,
                                                style = FabType.buttonSmall.copy(
                                                    color = if (isSelected) FabColors.Primary else FabColors.TextPrimary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (p.number != null) {
                                                Text(
                                                    text = "#${p.number} · ${p.status.lowercase()}",
                                                    style = FabType.cardSub
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Text("✓", color = FabColors.Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                    Divider(color = FabColors.Border)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
