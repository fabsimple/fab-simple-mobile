package com.fabsimple.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.fabsimple.app.presentation.screens.auth.LoginScreen
import com.fabsimple.app.presentation.screens.worker.WorkerQueueScreen
import com.fabsimple.app.presentation.screens.dashboard.DashboardScreen
import com.fabsimple.app.theme.FabSimpleTheme
import com.fabsimple.shared.di.AppContainer

@Composable
fun App() {
    val sessionState by AppContainer.authRepository.currentSession.collectAsState()
    val startScreen = if (sessionState != null) {
        if (sessionState!!.role == "worker") {
            WorkerQueueScreen()
        } else {
            DashboardScreen()
        }
    } else {
        LoginScreen()
    }

    FabSimpleTheme(darkTheme = sessionState?.role == "worker") {
        Navigator(startScreen) { navigator ->
            LaunchedEffect(navigator) {
                AppContainer.authRepository.sessionExpired.collect {
                    AppContainer.authRepository.signOut()
                    navigator.replaceAll(LoginScreen("Session expired. Please log in again."))
                }
            }
            SlideTransition(navigator)
        }
    }
}
