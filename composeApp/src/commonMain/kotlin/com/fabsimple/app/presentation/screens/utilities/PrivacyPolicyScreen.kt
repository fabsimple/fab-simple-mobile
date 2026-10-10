package com.fabsimple.app.presentation.screens.utilities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.theme.FabShapes
import com.fabsimple.app.theme.FabType

/**
 * Google Play Console Compliant Privacy Policy Screen for FabSimple Mobile.
 * Accessible from Login, SignUp, and Sidebar settings.
 */
class PrivacyPolicyScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollState = rememberScrollState()
        var showDeletionModal by remember { mutableStateOf(false) }
        var deletionSubmitted by remember { mutableStateOf(false) }

        val bgGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { navigator.pop() }) {
                        Text(
                            text = "← Back",
                            color = Color(0xFF818CF8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Privacy Policy",
                        color = Color.White,
                        style = FabType.cardTitle,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }

                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                // Scrollable Policy Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = FabShapes.Card,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "FabSimple Mobile Privacy Policy",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Last Updated: September 28, 2026 • Version 5.1",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "FabSimple provides structural steel fabrication tracking, AISC/AWS quality compliance, and CAD/BOM integration (Tekla PowerFab, SDS2, KISS). This policy explains how data is collected, used, and protected.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Section 1: Data Collection
                    PolicySection(
                        title = "1. Information We Collect",
                        content = "• Account Information: Name, email address, role (Owner, PM, Foreman, Inspector, Worker), and organization details.\n" +
                                "• CAD & Drawing Data: PDF blueprints, piece marks, material heat numbers, and Tekla PowerFab / SDS2 / KISS export files (.kss, .eje, .csv, .xlsx).\n" +
                                "• Media: Inspection photographs, weld check photos, and quality assurance logs.\n" +
                                "• Device & Crash Logs: Operating system version, app diagnostics, and shop network telemetry."
                    )

                    // Section 2: Hardware Permissions
                    PolicySection(
                        title = "2. Device Permissions Disclosures",
                        content = "• CAMERA: Used exclusively to scan QR codes on steel assemblies, part marks, and blueprint title blocks for station routing.\n" +
                                "• STORAGE / MEDIA: Used to cache vector structural drawings and PDF blueprints offline in shop bays.\n" +
                                "• INTERNET: Required to sync fabrication status and quality logs with your enterprise backend."
                    )

                    // Section 3: Tekla & Third Party Data
                    PolicySection(
                        title = "3. Tekla PowerFab & CAD Data Protection",
                        content = "FabSimple parses Tekla Structures, Tekla PowerFab, SDS2, and KISS material exports strictly inside your shop's isolated database tenant. We do not sell, trade, or share your structural blueprints or fabrication records with external advertisers or third parties."
                    )

                    // Section 4: Security
                    PolicySection(
                        title = "4. Data Security & Storage",
                        content = "All data transmitted between mobile devices and backend servers is encrypted using 256-bit TLS/HTTPS. Database tables enforce strict Row-Level Security (RLS) to ensure tenant isolation."
                    )

                    // Section 5: Account Deletion Request (Play Store Required)
//                    Card(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = FabShapes.Card,
//                        colors = CardDefaults.cardColors(containerColor = Color(0xFF312E81).copy(alpha = 0.4f)),
//                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6366F1))
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//                            Text(
//                                text = "5. Account & Data Deletion",
//                                color = Color.White,
//                                fontSize = 15.sp,
//                                fontWeight = FontWeight.Bold
//                            )
//                            Spacer(modifier = Modifier.height(6.dp))
//                            Text(
//                                text = "In compliance with Google Play Store policies, you may request full deletion of your user account and data at any time. Personal credentials and authentication tokens will be deleted within 30 days.",
//                                color = Color(0xFFE0E7FF),
//                                fontSize = 13.sp,
//                                lineHeight = 18.sp
//                            )
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            if (deletionSubmitted) {
//                                Text(
//                                    text = "✓ Account deletion request received. Our privacy team will process it within 30 days.",
//                                    color = Color(0xFF34D399),
//                                    fontSize = 12.sp,
//                                    fontWeight = FontWeight.Bold
//                                )
//                            } else {
//                                FabButton(
//                                    text = "Request Account Deletion",
//                                    onClick = { showDeletionModal = true },
//                                    modifier = Modifier.fillMaxWidth()
//                                )
//                            }
//                        }
//                    }

                    // Section 6: Contact
                    PolicySection(
                        title = "5. Contact Privacy Office",
                        content = "For privacy inquiries or statutory data requests, contact our security team at:\n" +
                                "Email: privacy@fabsimple.com\n" +
                                "Web: https://fabsimple.com/privacy"
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Deletion Modal Dialog
            if (showDeletionModal) {
                AlertDialog(
                    onDismissRequest = { showDeletionModal = false },
                    title = {
                        Text(text = "Confirm Account Deletion Request", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            text = "Are you sure you want to submit an account deletion request for your FabSimple profile? Your account credentials will be erased upon confirmation.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeletionModal = false
                                deletionSubmitted = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Submit Request", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeletionModal = false }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                    },
                    containerColor = Color(0xFF1E293B),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PolicySection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FabShapes.Card,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
