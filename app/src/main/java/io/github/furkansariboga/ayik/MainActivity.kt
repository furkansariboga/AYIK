/*
    AYIK - Abstinence Clock
    Copyright (C) 2026  Furkan Sarıboğa
    Licensed under GPL v3 — see LICENSE file.
*/
package io.github.furkansariboga.ayik

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import io.github.furkansariboga.ayik.presentation.HabitViewModel
import io.github.furkansariboga.ayik.presentation.add_entry.AddEntryScreen
import io.github.furkansariboga.ayik.presentation.dashboard.DashboardScreen
import io.github.furkansariboga.ayik.presentation.lock.LockScreen
import io.github.furkansariboga.ayik.presentation.lock.SetupLockScreen
import io.github.furkansariboga.ayik.presentation.navigation.Screen
import io.github.furkansariboga.ayik.presentation.settings.SettingsScreen
import io.github.furkansariboga.ayik.security.SecurityManager
import io.github.furkansariboga.ayik.ui.theme.AYIKTheme
import io.github.furkansariboga.ayik.util.MilestoneNotificationHelper
import io.github.furkansariboga.ayik.widget.WidgetUpdateManager
import io.github.furkansariboga.ayik.widget.WidgetDataHelper
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val securityManager = SecurityManager(this)
        MilestoneNotificationHelper.createNotificationChannel(this)

        // Initialize widget update schedule
        lifecycleScope.launch {
            val interval = WidgetDataHelper.getUpdateIntervalMinutes(this@MainActivity)
            WidgetUpdateManager.scheduleUpdates(this@MainActivity, interval)
        }

        setContent {
            AYIKTheme {
                var isUnlocked by remember { mutableStateOf(!securityManager.isLockEnabled) }

                if (!isUnlocked) {
                    LockScreen(securityManager = securityManager, onUnlocked = { isUnlocked = true })
                } else {
                    val navController = rememberNavController()
                    val viewModel: HabitViewModel = hiltViewModel()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    Scaffold(
                        topBar = {
                            if (currentRoute == Screen.Dashboard.route) {
                                CenterAlignedTopAppBar(
                                    title = { Text(stringResource(R.string.app_name)) },
                                    actions = {
                                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                            Icon(Icons.Default.Settings, stringResource(R.string.settings))
                                        }
                                    }
                                )
                            }
                        },
                        floatingActionButton = {
                            if (currentRoute == Screen.Dashboard.route) {
                                AnimatedVisibility(visible = true, enter = scaleIn(tween(300)) + fadeIn(tween(300)), exit = scaleOut(tween(200)) + fadeOut(tween(200))) {
                                    FloatingActionButton(onClick = { navController.navigate(Screen.AddEntry.createRoute()) }) {
                                        Icon(Icons.Default.Add, stringResource(R.string.add_new_entry))
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController, startDestination = Screen.Dashboard.route, modifier = Modifier.padding(innerPadding),
                            enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } },
                            exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { -it / 4 } },
                            popEnterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 } },
                            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it / 4 } }
                        ) {
                            composable(Screen.Dashboard.route) {
                                DashboardScreen(viewModel = viewModel, onAddEntryClick = { navController.navigate(Screen.AddEntry.createRoute()) }, onEditClick = { navController.navigate(Screen.AddEntry.createRoute(it)) })
                            }
                            composable(route = Screen.AddEntry.route, arguments = listOf(navArgument("habitId") { type = NavType.IntType; defaultValue = -1 })) { backStackEntry ->
                                val habitId = backStackEntry.arguments?.getInt("habitId") ?: -1
                                AddEntryScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() }, habitId = if (habitId != -1) habitId else null)
                            }
                            composable(Screen.Settings.route) {
                                SettingsScreen(onNavigateBack = { navController.popBackStack() }, onSetupLock = { navController.navigate(Screen.SetupLock.route) }, securityManager = securityManager)
                            }
                            composable(Screen.SetupLock.route) {
                                SetupLockScreen(securityManager = securityManager, onNavigateBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
