package com.fabsimple.app.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific QR camera preview composable for Android and iOS.
 */
@Composable
expect fun QrCameraPreview(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
)
