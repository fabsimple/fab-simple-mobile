package com.fabsimple.app.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Android actual implementation using ActivityResultContracts.GetContent.
 */
@Composable
actual fun FilePicker(
    show: Boolean,
    onFilePicked: (filename: String, bytes: ByteArray, mimeType: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: ByteArray(0)
                
                // Get filename
                var filename = "file.pdf"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        filename = cursor.getString(nameIndex)
                    }
                }
                
                val mimeType = contentResolver.getType(uri) ?: "application/pdf"
                onFilePicked(filename, bytes, mimeType)
            } catch (e: Exception) {
                e.printStackTrace()
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }

    LaunchedEffect(show) {
        if (show) {
            // Filter only PDFs or images as required by Tekla BOM/Drawing attachments
            launcher.launch("*/*")
        }
    }
}
