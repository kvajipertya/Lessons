package com.kvajipertya.lessons

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kvajipertya.lessons.data.Repository
import com.kvajipertya.lessons.models.Reminder
import com.kvajipertya.lessons.ui.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    onScheduleReminder: (Reminder) -> Unit = {},
    onCancelReminder: (String) -> Unit = {}
) {
    val language by Repository.instance.language.collectAsState()
    val navController = rememberNavController()
    var currentScreen by remember { mutableStateOf("today") }
    var homeClickCount by remember { mutableStateOf(0) }
    
    val darkModeSetting by Repository.instance.isDarkMode.collectAsState()
    val isDark = when(darkModeSetting) {
        true -> true
        false -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = Strings.get("today", language)) },
                        label = { Text(Strings.get("today", language)) },
                        selected = currentScreen == "today",
                        onClick = {
                            if (currentScreen == "today") {
                                homeClickCount++
                                if (homeClickCount >= 6) {
                                    homeClickCount = 0
                                    currentScreen = "settings"
                                    navController.navigate("settings") {
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                homeClickCount = 1
                                currentScreen = "today"
                                navController.navigate("today") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = Strings.get("timetable_title", language)) },
                        label = { Text(Strings.get("timetable_title", language)) },
                        selected = currentScreen == "timetable",
                        onClick = {
                            homeClickCount = 0
                            currentScreen = "timetable"
                            navController.navigate("timetable") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Notifications, contentDescription = Strings.get("reminders_title", language)) },
                        label = { Text(Strings.get("reminders_title", language)) },
                        selected = currentScreen == "reminders",
                        onClick = {
                            homeClickCount = 0
                            currentScreen = "reminders"
                            navController.navigate("reminders") {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Edit, contentDescription = Strings.get("homework_title", language)) },
                        label = { Text(Strings.get("homework_title", language)) },
                        selected = currentScreen == "homework",
                        onClick = {
                            homeClickCount = 0
                            currentScreen = "homework"
                            navController.navigate("homework") {
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
                composable("homework") { HomeworkScreen() }
                composable("settings") { SettingsScreen() }
            }
        }
    }
}
