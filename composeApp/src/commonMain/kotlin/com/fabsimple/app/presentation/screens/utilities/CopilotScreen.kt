package com.fabsimple.app.presentation.screens.utilities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.fabsimple.app.components.CopilotPanel
import com.fabsimple.app.state.GlobalProjectState
import com.fabsimple.app.theme.*
import com.fabsimple.shared.di.AppContainer
import com.fabsimple.shared.domain.model.ChatMessage
import kotlinx.coroutines.launch

class CopilotScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val activeProject by GlobalProjectState.currentProject.collectAsState()

        var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
        var input by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        fun sendMessage(content: String) {
            if (content.isBlank() || busy) return
            val updatedMessages = messages + ChatMessage(role = "user", content = content)
            messages = updatedMessages
            input = ""
            busy = true
            error = null

            coroutineScope.launch {
                try {
                    val reply = AppContainer.copilotRepository.query(updatedMessages, activeProject.id)
                    messages = updatedMessages + ChatMessage(role = "assistant", content = reply)
                } catch (e: Exception) {
                    error = e.message ?: "Failed to get reply"
                } finally {
                    busy = false
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B1120))
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            CopilotPanel(
                open = true,
                messages = messages,
                input = input,
                onInputChange = { input = it },
                onSend = { sendMessage(it) },
                onClose = { navigator.pop() },
                busy = busy,
                error = error,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
