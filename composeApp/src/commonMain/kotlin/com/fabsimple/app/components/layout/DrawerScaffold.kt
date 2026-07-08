package com.fabsimple.app.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabsimple.app.theme.*
import kotlinx.coroutines.launch

/**
 * DrawerScaffold — Responsive application shell with Sidebar and TopBar.
 * Desktop: Sidebar is permanently docked.
 * Mobile: Sidebar slides out as a modal drawer.
 */
@Composable
fun DrawerScaffold(
    title: String,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Determine layout dynamically (simple screen width check can be simulated or done inside BoxWithConstraints)
    BoxWithConstraints(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        val isDesktop = maxWidth >= 1024.dp

        if (isDesktop) {
            // Permanent sidebar layout
            Row(modifier = Modifier.fillMaxSize()) {
                SidebarContent(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    onSignOut = onSignOut
                )

                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    TopBar(
                        title = title,
                        onMenuClick = {
                            // Hamburger menu does nothing on desktop since sidebar is permanently open
                        },
                        onSearchClick = onSearchClick
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(FabColors.Background)
                    ) {
                        content()
                    }
                }
            }
        } else {
            // Modal drawer layout for mobile/tablet
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        SidebarContent(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                onNavigate(route)
                                coroutineScope.launch { drawerState.close() }
                            },
                            onSignOut = {
                                onSignOut()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TopBar(
                        title = title,
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onSearchClick = onSearchClick
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(FabColors.Background)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
