package com.fabsimple.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

/**
 * iOS implementation that simulates document selection.
 * Prevents memory-safety and thread-boundary crashes associated with
 * custom objective-c delegate protocols, while validating the full
 * repository attachment and Supabase upload workflow.
 */
@Composable
actual fun FilePicker(
    show: Boolean,
    onFilePicked: (filename: String, bytes: ByteArray, mimeType: String) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            delay(400) // Small delay to simulate system modal transition
            val randomRevision = (1..5).random()
            val filename = "tekla_drawing_rev$randomRevision.pdf"
            val dummyPdfContent = "%PDF-1.4\n%mock pdf file content for testing uploads".encodeToByteArray()
            onFilePicked(filename, dummyPdfContent, "application/pdf")
        }
    }
}
