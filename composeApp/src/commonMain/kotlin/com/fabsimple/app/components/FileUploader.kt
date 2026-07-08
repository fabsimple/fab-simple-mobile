package com.fabsimple.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabsimple.app.theme.*
import com.fabsimple.shared.domain.model.FileAttachment

/**
 * FileUploader — Displays and manages attachments.
 * Replicates components/ui/FileUploader.tsx
 */
@Composable
fun FileUploader(
    files: List<FileAttachment>,
    onAddFileClick: () -> Unit,
    onOpenFile: (FileAttachment) -> Unit,
    onDeleteFile: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Attachments",
    loading: Boolean = false,
    uploading: Boolean = false,
    error: String? = null,
    maxFiles: Int = 20
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label ${if (files.isNotEmpty()) "(${files.size})" else ""}".uppercase(),
                style = FabType.infoCellLabel
            )

            if (files.size < maxFiles) {
                FabButton(
                    text = if (uploading) "Uploading…" else "Add file",
                    onClick = onAddFileClick,
                    variant = ButtonVariant.Secondary,
                    size = ButtonSize.Small,
                    enabled = !uploading,
                    icon = if (uploading) {
                        { CircularProgressIndicator(color = FabColors.TextPrimary, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp) }
                    } else null
                )
            }
        }

        if (error != null) {
            AlertBanner(message = error, variant = AlertVariant.Danger)
        }

        if (loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                CircularProgressIndicator(color = FabColors.Primary, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Text("Loading…", style = FabType.cardSub)
            }
        } else if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FabTheme.extendedColors.mutedBackground.copy(alpha = 0.5f), FabShapes.InfoCell)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No files yet",
                    style = FabType.cardSub
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                files.forEach { file ->
                    val filename = file.storage_path.substringAfterLast("/")
                    val sizeKb = file.size_bytes?.let { "${(it / 1024.0).toInt()} KB" } ?: "—"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FabTheme.extendedColors.mutedBackground.copy(alpha = 0.5f), FabShapes.InfoCell)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📄",
                            fontSize = 14.sp
                        )

                        Text(
                            text = filename,
                            style = FabType.tableCell.copy(color = FabColors.Primary, fontWeight = FontWeight.Medium),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenFile(file) }
                        )

                        Text(
                            text = sizeKb,
                            style = FabType.cardSub,
                            fontFamily = MonoFontFamily
                        )

                        TextButton(
                            onClick = { onOpenFile(file) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("⬇", style = FabType.body, color = FabColors.TextMuted)
                        }

                        TextButton(
                            onClick = { onDeleteFile(file) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("🗑", style = FabType.body, color = FabColors.Red)
                        }
                    }
                }
            }
        }
    }
}
