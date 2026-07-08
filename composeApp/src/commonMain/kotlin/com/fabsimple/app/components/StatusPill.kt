package com.fabsimple.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabsimple.app.theme.*

/**
 * StatusPill — renders a colored pill badge for any status string.
 * Matches the `.pill` CSS class from globals.css with 16+ color variants.
 *
 * Usage: `StatusPill(status = "complete")`
 */
@Composable
fun StatusPill(
    status: String,
    modifier: Modifier = Modifier,
    size: PillSize = PillSize.Medium
) {
    val colors = LocalStatusColors.current
    val token = colors.resolve(status)

    val (hPad, vPad) = when (size) {
        PillSize.Small -> 6.dp to 2.dp
        PillSize.Medium -> 8.dp to 3.dp
        PillSize.Large -> 10.dp to 4.dp
    }

    Box(
        modifier = modifier
            .background(token.bg, FabShapes.Pill)
            .border(1.dp, token.border, FabShapes.Pill)
            .padding(horizontal = hPad, vertical = vPad),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.uppercase().replace("_", " "),
            style = FabType.pill.copy(
                color = token.main,
                fontSize = when (size) {
                    PillSize.Small -> 9.sp
                    PillSize.Medium -> 11.sp
                    PillSize.Large -> 12.sp
                }
            ),
            maxLines = 1
        )
    }
}

enum class PillSize { Small, Medium, Large }

