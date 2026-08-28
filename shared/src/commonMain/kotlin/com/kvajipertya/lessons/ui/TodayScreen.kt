package com.kvajipertya.lessons.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kvajipertya.lessons.data.Repository
import com.kvajipertya.lessons.models.SchoolDay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TodayScreen() {
    val timetable by Repository.instance.timetable.collectAsState()
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentDayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    
    val currentSchoolDay = SchoolDay.entries.find { it.name == currentDayName }
    
    val todaySubjects = timetable.filter { it.day == currentSchoolDay }
        .sortedBy { it.startTime }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Today's Schedule", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(currentDayName, color = MaterialTheme.colorScheme.secondary)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (todaySubjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No subjects scheduled for today!")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(todaySubjects) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(entry.subject, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${entry.startTime} - ${entry.endTime}", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
