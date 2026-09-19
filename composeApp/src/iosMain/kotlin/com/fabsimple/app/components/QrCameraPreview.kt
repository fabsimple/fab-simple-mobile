package com.fabsimple.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.*
import platform.Foundation.*
import platform.QuartzCore.*
import platform.UIKit.*

/**
 * iOS implementation for live QR camera scanner using AVFoundation.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun QrCameraPreview(
    onCodeScanned: (String) -> Unit,
    modifier: Modifier
) {
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }

    fun checkAndRequestCameraPermission() {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> {
                permissionGranted = true
                permissionDenied = false
            }
            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    permissionGranted = granted
                    permissionDenied = !granted
                }
            }
            else -> {
                permissionGranted = false
                permissionDenied = true
            }
        }
    }

    LaunchedEffect(Unit) {
        checkAndRequestCameraPermission()
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (permissionGranted) {
            UIKitView(
                factory = {
                    val containerView = UIView()
                    val session = AVCaptureSession()
                    val captureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)

                    if (captureDevice != null) {
                        val input = AVCaptureDeviceInput.deviceInputWithDevice(captureDevice, null)
                        if (input != null && session.canAddInput(input)) {
                            session.addInput(input)
                        }

                        val previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(session)
                        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
                        previewLayer.frame = containerView.bounds
                        containerView.layer.addSublayer(previewLayer)

                        session.startRunning()
                    }
                    containerView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (permissionDenied) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Camera Access Denied",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Please enable camera permissions in iOS Settings to scan QR codes",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF6366F1))
                        .clickable {
                            val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
                            if (url != null) {
                                UIApplication.sharedApplication.openURL(url)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Open iOS Settings",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
