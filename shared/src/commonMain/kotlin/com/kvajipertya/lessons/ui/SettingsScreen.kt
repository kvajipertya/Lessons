package com.kvajipertya.lessons.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kvajipertya.lessons.data.Repository

@Composable
fun SettingsScreen() {
    val language by Repository.instance.language.collectAsState()
    val darkMode by Repository.instance.isDarkMode.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(Strings.get("settings_title", language), style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Dark Mode
        Text(Strings.get("appearance", language), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(Strings.get("theme", language))
                Text(
                    text = when(darkMode) {
                        true -> Strings.get("dark", language)
                        false -> Strings.get("light", language)
                        else -> Strings.get("follow_system", language)
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Row {
                TextButton(onClick = { Repository.instance.setDarkMode(null) }) { Text(Strings.get("auto", language)) }
                TextButton(onClick = { Repository.instance.setDarkMode(false) }) { Text(Strings.get("light", language)) }
                TextButton(onClick = { Repository.instance.setDarkMode(true) }) { Text(Strings.get("dark", language)) }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Language
        Text(Strings.get("localization", language), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        var expanded by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${Strings.get("language", language)}: $language")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf("English", "Georgian").forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang) },
                        onClick = {
                            Repository.instance.setLanguage(lang)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Version Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Lessons App", style = MaterialTheme.typography.titleMedium)
                    Text("${Strings.get("version", language)} 2.0.3 (Stable)", style = MaterialTheme.typography.bodySmall)
                    Text("© 2026 Kvajipertya", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
