package com.liferise.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.liferise.app.core.designsystem.LifeRiseTheme
import com.liferise.app.core.navigation.BottomNavItem
import com.liferise.app.core.navigation.LifeRiseNavGraph
import com.liferise.app.core.navigation.Screen
import com.liferise.app.domain.model.AppTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission granted or denied handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationPermission()

        val appContainer = (application as LifeRiseApplication).container

        setContent {
            val settings by appContainer.settingsRepository.getSettingsFlow().collectAsState(initial = null)
            val darkTheme = when (settings?.theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            LifeRiseTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine if bottom navigation bar should be visible
                val showBottomBar = BottomNavItem.items.any { it.route == currentRoute }

                // Check intent navigation
                LaunchedEffect(intent) {
                    val navigateTo = intent.getStringExtra("navigate_to")
                    if (navigateTo == "tasks") {
                        navController.navigate(Screen.Tasks.route)
                    } else if (navigateTo == "alarm") {
                        navController.navigate(Screen.Alarm.route)
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = NavigationBarDefaults.Elevation
                            ) {
                                BottomNavItem.items.forEach { item ->
                                    val isSelected = currentRoute == item.route
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.title) },
                                        label = { Text(item.title) },
                                        selected = isSelected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    LifeRiseNavGraph(
                        navController = navController,
                        container = appContainer,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(permission)
            }
        }
    }
}
