package com.fabsimple.app.presentation.screens.worker

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.AlertBanner
import com.fabsimple.app.components.AlertVariant
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.components.FabTextField
import com.fabsimple.app.components.QrCameraPreview
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import kotlinx.coroutines.launch

class ScannerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        var manualCode by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Scanning laser animation line
        val infiniteTransition = rememberInfiniteTransition()
        val laserProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        fun extractPartIdOrMark(raw: String): String {
            val trimmed = raw.trim()
            if (trimmed.startsWith("fabsimple://part/")) {
                return trimmed.removePrefix("fabsimple://part/").substringBefore("?").substringBefore("#")
            }
            if (trimmed.contains("/parts/")) {
                return trimmed.substringAfter("/parts/").substringBefore("?").substringBefore("#")
            }
            return trimmed
        }

        fun submitCode(code: String) {
            val cleaned = extractPartIdOrMark(code)
            if (cleaned.isBlank() || isSubmitting) return

            isSubmitting = true
            errorMessage = null

            coroutineScope.launch {
                try {
                    val allParts = AppContainer.partRepository.getParts(null)
                    val matchingPart = allParts.find { p ->
                        p.id.equals(cleaned, ignoreCase = true) ||
                                p.part_mark.equals(cleaned, ignoreCase = true) ||
                                (p.assembly_mark != null && p.assembly_mark!!.equals(cleaned, ignoreCase = true))
                    }

                    if (matchingPart != null) {
                        navigator.replace(PartDetailScreen(matchingPart.id))
                    } else {
                        errorMessage = "Part '$cleaned' not found in system queue"
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Error searching for part code"
                } finally {
                    isSubmitting = false
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1120))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                // Title
                Text(
                    text = "QR Code Scanner",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Camera Viewport Container matching screenshot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Camera Preview
                    QrCameraPreview(
                        onCodeScanned = { scannedCode ->
                            submitCode(scannedCode)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Laser Scanning Line Animation Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 2.dp.toPx()
                        val laserY = size.height * laserProgress
                        val laserColor = Color(0xFF6366F1) // Indigo/Laser line

                        // Draw scanning laser beam line
                        drawLine(
                            color = laserColor.copy(alpha = 0.85f),
                            start = Offset(0f, laserY),
                            end = Offset(size.width, laserY),
                            strokeWidth = strokeWidth
                        )
                    }

                    // Target Corner Reticles Overlay
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val cornerLen = 24.dp.toPx()
                        val stroke = 3.dp.toPx()
                        val cornerColor = Color.White.copy(alpha = 0.7f)

                        // Top-Left
                        drawLine(cornerColor, Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
                        drawLine(cornerColor, Offset(0f, 0f), Offset(0f, cornerLen), stroke)

                        // Top-Right
                        drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), stroke)
                        drawLine(cornerColor, Offset(size.width, 0f), Offset(size.width, cornerLen), stroke)

                        // Bottom-Left
                        drawLine(cornerColor, Offset(0f, size.height), Offset(cornerLen, size.height), stroke)
                        drawLine(cornerColor, Offset(0f, size.height), Offset(0f, size.height - cornerLen), stroke)

                        // Bottom-Right
                        drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), stroke)
                        drawLine(cornerColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), stroke)
                    }
                }

                // Error Message Banner
                if (errorMessage != null) {
                    AlertBanner(
                        message = errorMessage!!,
                        variant = AlertVariant.Danger,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Manual Input Label
                Text(
                    text = "Or enter code manually:",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // Manual Input Field matching screenshot
                FabTextField(
                    value = manualCode,
                    onValueChange = {
                        manualCode = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = "Enter part ID or part mark"
                )

                // Submit Button matching screenshot
                FabButton(
                    text = if (isSubmitting) "Searching…" else "Submit Code",
                    onClick = { submitCode(manualCode) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = manualCode.isNotBlank() && !isSubmitting
                )

                // Cancel Link matching screenshot
                TextButton(
                    onClick = { navigator.pop() },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF818CF8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
