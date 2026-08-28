package com.kvajipertya.lessons.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kvajipertya.lessons.data.Repository
import com.kvajipertya.lessons.models.Reminder
import kotlinx.datetime.*

@Composable
fun RemindersScreen(
    onScheduleReminder: (Reminder) -> Unit = {},
    onCancelReminder: (String) -> Unit = {}
) {
    val reminders by Repository.instance.reminders.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text("Reminders", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(16.dp))

            if (reminders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reminders set.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(reminders) { reminder ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = reminder.isCompleted,
                                        onCheckedChange = { Repository.instance.toggleReminder(reminder.id) }
                                    )
                                    Column {
                                        val textDecoration = if (reminder.isCompleted) 
                                            androidx.compose.ui.text.style.TextDecoration.LineThrough 
                                            else androidx.compose.ui.text.style.TextDecoration.None
                                        
                                        Text(
                                            reminder.title, 
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = textDecoration
                                        )
                                        Text("${reminder.subject} • Due: ${reminder.dueDate}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                IconButton(onClick = { 
                                    Repository.instance.removeReminder(reminder.id)
                                    onCancelReminder(reminder.id)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddReminderDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, subject, date ->
                    val newReminder = Reminder(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        title = title,
                        subject = subject,
                        dueDate = date
                    )
                    Repository.instance.addReminder(newReminder)
                    onScheduleReminder(newReminder)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val timetable by Repository.instance.timetable.collectAsState()
    val subjects = timetable.map { it.subject }.distinct()
    
    // Initial date: today
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedDate by remember { mutableStateOf(now) }
    
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("What's due?") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Subject Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, "Select Subject", Modifier.clickable { expanded = true })
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (subjects.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No subjects found. Add them in Timetable.") },
                                onClick = { expanded = false }
                            )
                        } else {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        subject = s
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Date Picker Trigger
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = selectedDate.toString(),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank() && subject.isNotBlank()) onAdd(title, subject, selectedDate.toString()) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = Clock.System.now().toEpochMilliseconds()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.fromEpochMilliseconds(it)
                            .toLocalDateTime(TimeZone.UTC).date
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
