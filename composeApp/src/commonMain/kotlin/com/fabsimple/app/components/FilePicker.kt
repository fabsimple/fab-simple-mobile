package com.fabsimple.app.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific file picker Composable.
 */
@Composable
expect fun FilePicker(
    show: Boolean,
    onFilePicked: (filename: String, bytes: ByteArray, mimeType: String) -> Unit,
    onDismiss: () -> Unit
)
