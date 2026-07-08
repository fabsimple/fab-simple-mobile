package com.fabsimple.app.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fabsimple.app.theme.*
import kotlinx.coroutines.delay

enum class ToastType { Success, Error, Info }

data class ToastMessage(
    val id: Long,
    val message: String,
    val type: ToastType = ToastType.Info,
    val durationMs: Long = 3000L
)

/**
 * ToastManager - A helper class to host active toast messages.
 */
class ToastState {
    var activeToast by mutableStateOf<ToastMessage?>(null)
        private set

    fun show(message: String, type: ToastType = ToastType.Info, durationMs: Long = 3000L) {
        activeToast = ToastMessage(
            id = ClockSystemTime(),
            message = message,
            type = type,
            durationMs = durationMs
        )
    }

    fun dismiss() {
        activeToast = null
    }

    private fun ClockSystemTime(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
}

val LocalToastState = staticCompositionLocalOf { ToastState() }

/**
 * ToastContainer — Overlays toast notifications at the top of the screen.
 */
@Composable
fun ToastContainer(
    state: ToastState,
    modifier: Modifier = Modifier
) {
    val toast = state.activeToast

    LaunchedEffect(toast) {
        if (toast != null) {
            delay(toast.durationMs)
            state.dismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            if (toast != null) {
                val (bg, border, text) = when (toast.type) {
                    ToastType.Success -> Triple(FabColors.GreenBg, FabColors.GreenBorder, FabColors.Green)
                    ToastType.Error -> Triple(FabColors.RedBg, FabColors.RedBorder, FabColors.Red)
                    ToastType.Info -> Triple(FabColors.BlueBg, FabColors.BlueBorder, FabColors.Blue)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, border, RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = toast.message,
                            style = FabType.bodySmall.copy(color = text),
                            fontWeight = FontWeight.Bold
                        )

                        TextButton(
                            onClick = { state.dismiss() },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                text = "✕",
                                style = FabType.bodySmall.copy(color = text),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
