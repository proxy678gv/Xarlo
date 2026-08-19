package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.datastore.ThemeMode
import com.example.ui.components.XarloBottomBar
import com.example.ui.components.XarloTopBar
import com.example.ui.components.ZlvaAssistantModal
import com.example.ui.screens.focus.FocusScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.schedule.ScheduleScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.track.TrackScreen
import com.example.ui.theme.XarloTheme
import com.example.ui.viewmodel.XarloViewModel
import com.example.ui.viewmodel.XarloViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: XarloViewModel by viewModels {
        val app = application as XarloApplication
        XarloViewModelFactory(
            repository = app.repository,
            alarmScheduler = app.alarmScheduler,
            notificationHelper = app.notificationHelper
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPrefs by viewModel.userPreferences.collectAsState()
            val systemDark = isSystemInDarkTheme()

            val isDarkTheme = when (userPrefs.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> systemDark
            }

            val currentDestination by viewModel.currentNavDestination.collectAsState()
            val isZlvaOpen by viewModel.isZlvaOpen.collectAsState()
            val zlvaMessages by viewModel.zlvaMessages.collectAsState()

            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                viewModel.toastEvent.collectLatest { msg ->
                    snackbarHostState.showSnackbar(
                        message = msg,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            XarloTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        XarloTopBar(
                            onToggleTheme = {
                                val next = if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                                viewModel.setThemeMode(next)
                            },
                            onOpenZlva = { viewModel.openZlva() }
                        )
                    },
                    bottomBar = {
                        XarloBottomBar(
                            currentDestination = currentDestination,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("xarlo_main_scaffold")
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Crossfade(
                            targetState = currentDestination,
                            label = "nav_transition"
                        ) { destination ->
                            when (destination) {
                                "home" -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateTo = { viewModel.navigateTo(it) },
                                    onOpenZlva = { viewModel.openZlva() }
                                )
                                "schedule" -> ScheduleScreen(
                                    viewModel = viewModel
                                )
                                "track" -> TrackScreen(
                                    viewModel = viewModel
                                )
                                "focus" -> FocusScreen(
                                    viewModel = viewModel
                                )
                                "settings" -> SettingsScreen(
                                    viewModel = viewModel,
                                    onOpenZlva = { viewModel.openZlva() }
                                )
                                else -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateTo = { viewModel.navigateTo(it) },
                                    onOpenZlva = { viewModel.openZlva() }
                                )
                            }
                        }
                    }
                }

                // ZLVA Offline AI Assistant Modal
                ZlvaAssistantModal(
                    isOpen = isZlvaOpen,
                    onClose = { viewModel.closeZlva() },
                    messages = zlvaMessages,
                    onSendMessage = { prompt -> viewModel.sendZlvaMessage(prompt) },
                    onExecuteIntent = { intent -> viewModel.executeZlvaIntent(intent) }
                )
            }
        }
    }
}

