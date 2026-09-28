package com.fabsimple.app.components

import androidx.compose.runtime.Composable

/**
 * Platform-specific Camera & Image picker with permissions handling for Android & iOS.
 */
@Composable
expect fun CameraPhotoPicker(
    show: Boolean,
    onPhotoCaptured: (filename: String, bytes: ByteArray, mimeType: String) -> Unit,
    onDismiss: () -> Unit
)
