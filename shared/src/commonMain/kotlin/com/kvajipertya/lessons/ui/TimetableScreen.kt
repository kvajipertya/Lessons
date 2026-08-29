package com.kvajipertya.lessons.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kvajipertya.lessons.data.Repository
import com.kvajipertya.lessons.models.SchoolDay
import com.kvajipertya.lessons.models.TimetableEntry
import kotlinx.datetime.*

@Composable
fun TimetableScreen() {
    val language by Repository.instance.language.collectAsState()
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val initialDay = SchoolDay.entries.find { it.name == currentDayName } ?: SchoolDay.Monday
    
    var selectedDay by remember { mutableStateOf(initialDay) }
    val timetable by Repository.instance.timetable.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = Strings.get("add_subject", language))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            ScrollableTabRow(selectedTabIndex = selectedDay.ordinal) {
                SchoolDay.entries.forEach { day ->
                    Tab(
                        selected = selectedDay == day,
                        onClick = { selectedDay = day },
                        text = { Text(day.name.take(3)) }
                    )
                }
            }

            val daySubjects = timetable.filter { it.day == selectedDay }.sortedBy { it.startTime }

            if (daySubjects.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(Strings.get("no_subjects", language))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daySubjects) { entry ->
                        SubjectCard(entry)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddSubjectDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { subject, start, end ->
                    Repository.instance.addTimetableEntry(
                        TimetableEntry(
                            id = Clock.System.now().toEpochMilliseconds().toString(),
                            subject = subject,
                            startTime = start,
                            endTime = end,
                            day = selectedDay
                        )
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    val language by Repository.instance.language.collectAsState()
    var subject by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf(LocalTime(8, 0)) }
    var endTime by remember { mutableStateOf(LocalTime(9, 0)) }
    
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("add_subject", language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = subject, 
                    onValueChange = { subject = it }, 
                    label = { Text(Strings.get("subject_name", language)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(Strings.get("start_time", language), style = MaterialTheme.typography.labelMedium)
                Surface(
                    onClick = { showStartPicker = true },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(startTime.toString().take(5), style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Text(Strings.get("end_time", language), style = MaterialTheme.typography.labelMedium)
                Surface(
                    onClick = { showEndPicker = true },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(endTime.toString().take(5), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (subject.isNotBlank()) {
                    onAdd(subject, startTime.toString().take(5), endTime.toString().take(5))
                }
            }) {
                Text(Strings.get("ok", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.get("cancel", language)) }
        }
    )

    if (showStartPicker) {
        val state = rememberTimePickerState(initialHour = startTime.hour, initialMinute = startTime.minute, is24Hour = true)
        TimePickerModal(
            onDismiss = { showStartPicker = false },
            onConfirm = {
                startTime = LocalTime(state.hour, state.minute)
                showStartPicker = false
            },
            state = state
        )
    }

    if (showEndPicker) {
        val state = rememberTimePickerState(initialHour = endTime.hour, initialMinute = endTime.minute, is24Hour = true)
        TimePickerModal(
            onDismiss = { showEndPicker = false },
            onConfirm = {
                endTime = LocalTime(state.hour, state.minute)
                showEndPicker = false
            },
            state = state
        )
    }
}

@Composable
fun SubjectCard(entry: TimetableEntry) {
    val language by Repository.instance.language.collectAsState()
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(entry.subject, fontWeight = FontWeight.Bold)
                Text("${entry.startTime} - ${entry.endTime}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { Repository.instance.removeTimetableEntry(entry.id) }) {
                Icon(Icons.Default.Delete, contentDescription = Strings.get("delete", language), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerModal(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    state: TimePickerState
) {
    val language by Repository.instance.language.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(Strings.get("ok", language)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.get("cancel", language)) }
        },
        text = {
            TimeInput(state = state)
        }
    )
}
