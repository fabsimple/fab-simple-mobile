package com.fabsimple.app.presentation.screens.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.components.FabTextField
import com.fabsimple.app.theme.*

class ScannerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var manualCode by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1120))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = "QR Code Scanner",
                    color = Color.White,
                    style = FabType.pageTitle.copy(color = Color.White)
                )

                // Placeholder for camera view
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color.Black)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[ Camera View Placeholder ]",
                        color = Color.DarkGray,
                        style = FabType.body
                    )
                }

                Text(
                    text = "Or enter code manually:",
                    color = Color.LightGray,
                    style = FabType.body
                )

                FabTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    placeholder = "Enter part ID or part mark"
                )

                FabButton(
                    text = "Submit Code",
                    onClick = {
                        if (manualCode.isNotBlank()) {
                            // Find and open part
                            navigator.replace(PartDetailScreen(manualCode))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = manualCode.isNotBlank()
                )

                TextButton(onClick = { navigator.pop() }) {
                    Text("Cancel", color = Color(0xFF818CF8))
                }
            }
        }
    }
}
