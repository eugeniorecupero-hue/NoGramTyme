package com.example.nogramtime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nogramtime.data.AppDatabase
import com.example.nogramtime.service.ForegroundMonitorService
import com.example.nogramtime.ui.HomeScreen
import com.example.nogramtime.ui.ScheduleScreen
import com.example.nogramtime.ui.SettingsScreen
import kotlinx.coroutines.launch

/**
 * Main activity hosting the Compose UI. Sets up navigation between the Home,
 * Schedule and Settings screens. It also starts the foreground monitor service
 * on creation to increase reliability.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kick off the foreground monitor service
        startForegroundService(Intent(this, ForegroundMonitorService::class.java))
        // Warm up the database so pre‑population happens on first launch
        AppDatabase.getInstance(applicationContext)
        setContent {
            NoGramTimeApp()
        }
    }
}

@Composable
fun NoGramTimeApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(navController)
            }
            composable("schedule") {
                ScheduleScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            route = "home",
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = "Home"
        ),
        BottomNavItem(
            route = "schedule",
            icon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
            label = "Pianificazione"
        ),
        BottomNavItem(
            route = "settings",
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = "Impostazioni"
        )
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = item.icon,
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
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

data class BottomNavItem(val route: String, val icon: @Composable () -> Unit, val label: String)