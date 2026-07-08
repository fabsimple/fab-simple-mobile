package com.fabsimple.app.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.FabButton
import com.fabsimple.app.components.FabTextField
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import kotlinx.coroutines.launch

class SignUpScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var fullName by remember { mutableStateOf("") }
        var companyName by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var successMessage by remember { mutableStateOf<String?>(null) }

        val bgGradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                shape = FabShapes.Modal,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Bootstrap Org",
                        color = Color.White,
                        style = FabType.pageTitle.copy(color = Color.White)
                    )

                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF7F1D1D).copy(alpha = 0.2f), shape = FabShapes.Alert)
                                .padding(8.dp)
                        )
                    }

                    successMessage?.let { success ->
                        Text(
                            text = success,
                            color = Color(0xFF10B981),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFECFDF5).copy(alpha = 0.1f), shape = FabShapes.Alert)
                                .padding(8.dp)
                        )
                    }

                    FabTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        placeholder = "John Doe"
                    )

                    FabTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = "Company Name",
                        placeholder = "Acme Steel Fabricators"
                    )

                    FabTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        placeholder = "admin@acme.com"
                    )

                    FabTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "••••••••",
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FabButton(
                        text = "Sign Up",
                        onClick = {
                            if (loading) return@FabButton
                            loading = true
                            errorMessage = null
                            successMessage = null
                            coroutineScope.launch {
                                try {
                                    AppContainer.authRepository.signUp(email, password, fullName, companyName)
                                    successMessage = "Account created successfully! Please sign in."
                                    email = ""
                                    password = ""
                                    fullName = ""
                                    companyName = ""
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Registration failed"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading
                    )

                    TextButton(
                        onClick = { navigator.pop() }
                    ) {
                        Text(
                            text = "Back to Sign In",
                            color = Color(0xFF818CF8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
