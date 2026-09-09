package com.adioevo.daw.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adioevo.daw.ui.screens.DawScreen
import com.adioevo.daw.ui.screens.SettingsScreen

sealed class NavigationDestination(val route: String, val label: String) {
    object DAW : NavigationDestination("daw", "DAW")
    object OnStage : NavigationDestination("onstage", "OnStage")
    object Mixer : NavigationDestination("mixer", "Mixer")
    object MIDI : NavigationDestination("midi", "MIDI")
    object SoundFonts : NavigationDestination("soundfonts", "SoundFonts")
    object Settings : NavigationDestination("settings", "Settings")
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    val destinations = listOf(
        NavigationDestination.DAW,
        NavigationDestination.OnStage,
        NavigationDestination.Mixer,
        NavigationDestination.MIDI,
        NavigationDestination.SoundFonts,
        NavigationDestination.Settings,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        label = { Text(destination.label) },
                        icon = {
                            Icon(
                                imageVector = if (index == 0) Icons.Default.Home else Icons.Default.Settings,
                                contentDescription = destination.label
                            )
                        },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationRoute ?: "daw") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = NavigationDestination.DAW.route
            ) {
                composable(NavigationDestination.DAW.route) {
                    DawScreen()
                }
                composable(NavigationDestination.OnStage.route) {
                    Box { Text("OnStage - Coming Soon") }
                }
                composable(NavigationDestination.Mixer.route) {
                    Box { Text("Mixer - Coming Soon") }
                }
                composable(NavigationDestination.MIDI.route) {
                    Box { Text("MIDI Settings - Coming Soon") }
                }
                composable(NavigationDestination.SoundFonts.route) {
                    Box { Text("SoundFonts - Coming Soon") }
                }
                composable(NavigationDestination.Settings.route) {
                    SettingsScreen()
                }
            }
        }
    }
}
