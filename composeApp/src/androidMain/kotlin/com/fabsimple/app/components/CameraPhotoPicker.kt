package com.fabsimple.app.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

@Composable
actual fun CameraPhotoPicker(
    show: Boolean,
    onPhotoCaptured: (filename: String, bytes: ByteArray, mimeType: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()
                val filename = "dft_weld_snapshot_${System.currentTimeMillis()}.jpg"
                onPhotoCaptured(filename, bytes, "image/jpeg")
            } catch (e: Exception) {
                e.printStackTrace()
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: ByteArray(0)
                var filename = "dft_photo_${System.currentTimeMillis()}.jpg"
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        filename = cursor.getString(nameIndex)
                    }
                }
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                onPhotoCaptured(filename, bytes, mimeType)
            } catch (e: Exception) {
                e.printStackTrace()
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            cameraLauncher.launch(null)
        } else {
            galleryLauncher.launch("image/*")
        }
    }

    LaunchedEffect(show) {
        if (show) {
            if (hasCameraPermission) {
                cameraLauncher.launch(null)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
