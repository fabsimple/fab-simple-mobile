package com.fabsimple.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fabsimple.app.theme.*

data class DataColumn<T>(
    val header: String,
    val width: Dp,
    val cellContent: @Composable (T) -> Unit
)

/**
 * DataTable — Standard paginated and sortable table component matching dashboard requirements.
 * Replicates the table layout, border-bottom style, and head styles in globals.css.
 */
@Composable
fun <T> DataTable(
    columns: List<DataColumn<T>>,
    items: List<T>,
    modifier: Modifier = Modifier,
    pageSize: Int = 10,
    onRowClick: ((T) -> Unit)? = null
) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = (items.size + pageSize - 1) / pageSize.coerceAtLeast(1)

    val pageStart = currentPage * pageSize
    val pageEnd = (pageStart + pageSize).coerceAtMost(items.size)
    val displayedItems = if (items.isNotEmpty()) items.subList(pageStart, pageEnd) else emptyList()

    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(FabShapes.Card)
            .border(1.dp, FabColors.Border, FabShapes.Card),
        colors = CardDefaults.cardColors(containerColor = FabTheme.extendedColors.cardBackground),
        shape = FabShapes.Card
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Horizontal scroll container for the table contents
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .background(FabTheme.extendedColors.mutedBackground.copy(alpha = 0.5f))
                            .border(width = 1.dp, color = FabColors.Border)
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        columns.forEach { col ->
                            Box(
                                modifier = Modifier.width(col.width),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = col.header.uppercase(),
                                    style = FabType.tableHeader
                                )
                            }
                        }
                    }

                    // Table Body Rows
                    Column {
                        if (displayedItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No items found",
                                    style = FabType.body,
                                    color = FabColors.TextMuted
                                )
                            }
                        } else {
                            displayedItems.forEachIndexed { index, item ->
                                val borderModifier = if (index < displayedItems.lastIndex) {
                                    Modifier.border(width = 1.dp, color = FabColors.Border)
                                } else Modifier

                                val rowClickModifier = if (onRowClick != null) {
                                    Modifier.clickable { onRowClick(item) }
                                } else Modifier

                                Row(
                                    modifier = Modifier
                                        .then(rowClickModifier)
                                        .then(borderModifier)
                                        .padding(vertical = 14.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    columns.forEach { col ->
                                        Box(
                                            modifier = Modifier.width(col.width),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            col.cellContent(item)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pagination Controls at the bottom
            if (totalPages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = FabColors.Border)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${pageStart + 1} to $pageEnd of ${items.size} entries",
                        style = FabType.cardSub
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FabButton(
                            text = "Previous",
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0,
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Small
                        )

                        Text(
                            text = "${currentPage + 1} of $totalPages",
                            style = FabType.tableCell,
                            fontWeight = FontWeight.Bold
                        )

                        FabButton(
                            text = "Next",
                            onClick = { if (currentPage < totalPages - 1) currentPage++ },
                            enabled = currentPage < totalPages - 1,
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Small
                        )
                    }
                }
            }
        }
    }
}
