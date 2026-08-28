package com.kvajipertya.lessons

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kvajipertya.lessons.models.Reminder
import com.kvajipertya.lessons.ui.RemindersScreen
import com.kvajipertya.lessons.ui.TimetableScreen
import com.kvajipertya.lessons.ui.TodayScreen
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    onScheduleReminder: (Reminder) -> Unit = {},
    onCancelReminder: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    var currentScreen by remember { mutableStateOf("today") }

    val colorScheme = if (isSystemInDarkTheme()) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                        label = { Text("Today") },
                        selected = currentScreen == "today",
                        onClick = {
                            currentScreen = "today"
                            navController.navigate("today") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Timetable") },
                        label = { Text("Timetable") },
                        selected = currentScreen == "timetable",
                        onClick = {
                            currentScreen = "timetable"
                            navController.navigate("timetable") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Reminders") },
                        label = { Text("Reminders") },
                        selected = currentScreen == "reminders",
                        onClick = {
                            currentScreen = "reminders"
                            navController.navigate("reminders") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "today",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("today") { TodayScreen() }
                composable("timetable") { TimetableScreen() }
                composable("reminders") { 
                    RemindersScreen(onScheduleReminder, onCancelReminder) 
                }
            }
        }
    }
}
