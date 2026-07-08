package com.fabsimple.app.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabsimple.app.theme.*

/**
 * ResourceModal — Form Dialog wrapper matching ResourceModal.tsx.
 * Provides standard Cancel/Submit buttons, loading state, and error banners.
 */
@Composable
fun ResourceModal(
    title: String,
    onClose: () -> Unit,
    onSubmit: () -> Unit,
    submitting: Boolean = false,
    error: String? = null,
    submitLabel: String = "Save",
    content: @Composable ColumnScope.() -> Unit
) {
    Modal(
        title = title,
        onDismissRequest = onClose,
        buttons = {
            FabButton(
                text = "Cancel",
                onClick = onClose,
                variant = ButtonVariant.Secondary,
                size = ButtonSize.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            FabButton(
                text = if (submitting) "Saving…" else submitLabel,
                onClick = onSubmit,
                variant = ButtonVariant.Primary,
                size = ButtonSize.Medium,
                enabled = !submitting,
                icon = if (submitting) {
                    { CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp) }
                } else null
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()

            if (error != null) {
                AlertBanner(
                    message = error,
                    variant = AlertVariant.Danger
                )
            }
        }
    }
}

/**
 * Form Field layout helper matching Field in ResourceModal.tsx.
 */
@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = FabType.infoCellLabel
            )
            if (required) {
                Text(
                    text = " *",
                    color = FabColors.Red,
                    style = FabType.infoCellLabel
                )
            }
        }

        content()

        if (hint != null) {
            Text(
                text = hint,
                style = FabType.cardSub,
                color = FabColors.TextMuted
            )
        }
    }
}
