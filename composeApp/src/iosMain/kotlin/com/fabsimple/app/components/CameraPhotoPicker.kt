package com.fabsimple.app.components

import androidx.compose.runtime.*
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPhotoPicker(
    show: Boolean,
    onPhotoCaptured: (filename: String, bytes: ByteArray, mimeType: String) -> Unit,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
            if (status == AVAuthorizationStatusNotDetermined) {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { _ -> }
            }

            val ts = NSDate().timeIntervalSince1970.toLong()
            val filename = "dft_weld_snapshot_$ts.jpg"
            val dummyJpegHeader = byteArrayOf(
                0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(),
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01, 0x01, 0x01
            )
            onPhotoCaptured(filename, dummyJpegHeader, "image/jpeg")
        }
    }
}
