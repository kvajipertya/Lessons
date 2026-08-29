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
import com.kvajipertya.lessons.models.Homework
import com.kvajipertya.lessons.models.SchoolDay
import kotlinx.datetime.*

@Composable
fun HomeworkScreen() {
    val language by Repository.instance.language.collectAsState()
    val homeworkList by Repository.instance.homework.collectAsState()
    val timetable by Repository.instance.timetable.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val currentSchoolDay = SchoolDay.entries.find { it.name == currentDayName }
    
    val todaySubjects = timetable.filter { it.day == currentSchoolDay }.map { it.subject }.toSet()
    val filteredHomework = homeworkList.filter { it.subject in todaySubjects }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = Strings.get("add_homework", language))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(Strings.get("homework_title", language), style = MaterialTheme.typography.headlineMedium)
            Text("${Strings.get("subjects_today", language)}: ${todaySubjects.joinToString(", ").ifEmpty { "None" }}", style = MaterialTheme.typography.bodySmall)
            
            Spacer(modifier = Modifier.height(16.dp))

            if (filteredHomework.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (todaySubjects.isEmpty()) Strings.get("no_classes_today", language) else Strings.get("no_homework_today", language))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredHomework) { hw ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = hw.isDone,
                                        onCheckedChange = { Repository.instance.toggleHomework(hw.id) }
                                    )
                                    Column {
                                        val textDecoration = if (hw.isDone) 
                                            androidx.compose.ui.text.style.TextDecoration.LineThrough 
                                            else androidx.compose.ui.text.style.TextDecoration.None
                                        
                                        Text(
                                            hw.task, 
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = textDecoration
                                        )
                                        Text(hw.subject, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                IconButton(onClick = { 
                                    Repository.instance.removeHomework(hw.id)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = Strings.get("delete", language), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddHomeworkDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { task, subject ->
                    val newHw = Homework(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        task = task,
                        subject = subject
                    )
                    Repository.instance.addHomework(newHw)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHomeworkDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    val language by Repository.instance.language.collectAsState()
    var task by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    
    val timetable by Repository.instance.timetable.collectAsState()
    
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val currentSchoolDay = SchoolDay.entries.find { it.name == currentDayName }
    
    val subjects = timetable.filter { it.day == currentSchoolDay }.map { it.subject }.distinct()
    
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.get("add_homework", language)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = task, 
                    onValueChange = { task = it }, 
                    label = { Text(Strings.get("task", language)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Subject Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(Strings.get("subject_name", language)) },
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
                                text = { Text("No subjects found.") },
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
            }
        },
        confirmButton = {
            Button(onClick = { if (task.isNotBlank() && subject.isNotBlank()) onAdd(task, subject) }) {
                Text(Strings.get("ok", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(Strings.get("cancel", language)) }
        }
    )
}
