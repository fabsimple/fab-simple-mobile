package com.fabsimple.app.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific PDF WebView composable for Android and iOS.
 * Renders live PDF documents inline inside Compose Multiplatform.
 */
@Composable
expect fun PdfWebView(
    url: String,
    modifier: Modifier = Modifier
)
