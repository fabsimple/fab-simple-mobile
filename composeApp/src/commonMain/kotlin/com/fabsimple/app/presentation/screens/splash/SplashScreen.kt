package com.fabsimple.app.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.layout.FabLogoIcon
import com.fabsimple.app.presentation.screens.auth.LoginScreen
import com.fabsimple.app.presentation.screens.dashboard.DashboardScreen
import com.fabsimple.app.presentation.screens.worker.WorkerQueueScreen
import com.fabsimple.shared.di.AppContainer
import kotlinx.coroutines.delay

/**
 * Branded Splash Screen displaying logo animation & resolving initial app session.
 */
class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val scale = remember { Animatable(0.8f) }
        val alpha = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            // Animate scale & opacity
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 600)
            )
            alpha.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 600)
            )

            // Hold splash screen for 1.8 seconds total
            delay(1200)

            // Resolve initial session destination
            val session = AppContainer.authRepository.getSession()
            val destinationScreen = if (session != null) {
                if (session.role == "worker") {
                    WorkerQueueScreen()
                } else {
                    DashboardScreen()
                }
            } else {
                LoginScreen()
            }

            navigator.replace(destinationScreen)
        }

        val bgGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF0B1120), Color(0xFF1E293B))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .padding(24.dp)
            ) {
                // Large Branded Logo Icon
                FabLogoIcon(size = 76.dp)

                Spacer(modifier = Modifier.height(20.dp))

                // Brand Typography
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Fab",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Simple",
                        color = Color(0xFF818CF8),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Steel Fabrication & Shop Floor Operations",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Bottom Loading Indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF818CF8),
                        strokeWidth = 2.5.dp
                    )
                    Text(
                        text = "Initializing...",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
