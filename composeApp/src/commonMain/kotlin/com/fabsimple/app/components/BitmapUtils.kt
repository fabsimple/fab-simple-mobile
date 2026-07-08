package com.fabsimple.app.components

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Platform-specific conversion of ByteArray bytes (e.g. PNG, JPEG) to a Compose ImageBitmap.
 */
expect fun ByteArray.toImageBitmap(): ImageBitmap
