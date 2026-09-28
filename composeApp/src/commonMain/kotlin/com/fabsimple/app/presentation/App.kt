package com.fabsimple.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.fabsimple.app.presentation.screens.auth.LoginScreen
import com.fabsimple.app.presentation.screens.splash.SplashScreen
import com.fabsimple.app.theme.FabSimpleTheme
import com.fabsimple.shared.di.AppContainer

@Composable
fun App() {
    val sessionState by AppContainer.authRepository.currentSession.collectAsState()

    FabSimpleTheme(darkTheme = sessionState?.role == "worker") {
        Navigator(SplashScreen()) { navigator ->
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
