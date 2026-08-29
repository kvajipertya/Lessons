package com.kvajipertya.lessons.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalTime

@Serializable
enum class SchoolDay {
    Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
}

@Serializable
data class TimetableEntry(
    val id: String,
    val subject: String,
    val startTime: String, // HH:mm
    val endTime: String,   // HH:mm
    val day: SchoolDay
) {
    companion object
}

@Serializable
data class Homework(
    val id: String,
    val task: String,
    val subject: String,
    val isDone: Boolean = false
) {
    companion object
}

@Serializable
data class Reminder(
    val id: String,
    val title: String,
    val subject: String,
    val dueDate: String, // yyyy-MM-dd
    val isCompleted: Boolean = false
) {
    companion object
}
