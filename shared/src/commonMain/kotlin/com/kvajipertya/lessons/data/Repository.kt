package com.kvajipertya.lessons.data

import com.kvajipertya.lessons.models.Reminder
import com.kvajipertya.lessons.models.TimetableEntry
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.*

class Repository(private val settings: Settings) {
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = false
        coerceInputValues = true
    }

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        _timetable.value = loadTimetable()
        _reminders.value = loadReminders()
    }

    fun addTimetableEntry(entry: TimetableEntry) {
        _timetable.value = _timetable.value + entry
        saveTimetable()
    }

    fun removeTimetableEntry(id: String) {
        _timetable.value = _timetable.value.filter { it.id != id }
        saveTimetable()
    }

    fun addReminder(reminder: Reminder) {
        _reminders.value = _reminders.value + reminder
        saveReminders()
    }

    fun removeReminder(id: String) {
        _reminders.value = _reminders.value.filter { it.id != id }
        saveReminders()
    }

    fun toggleReminder(id: String) {
        _reminders.value = _reminders.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        saveReminders()
    }

    fun cleanupPastReminders() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val filtered = _reminders.value.filter {
            try {
                val dueDate = LocalDate.parse(it.dueDate)
                dueDate >= now
            } catch (e: Exception) {
                true
            }
        }
        if (filtered.size != _reminders.value.size) {
            _reminders.value = filtered
            saveReminders()
        }
    }

    private fun loadTimetable(): List<TimetableEntry> {
        val v4 = settings.getStringOrNull("timetable_v4")
        if (v4 != null) {
            return try {
                json.decodeFromString<List<TimetableEntry>>(v4)
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        val v3 = settings.getStringOrNull("timetable_v3")
        if (v3 != null) {
            val list = try {
                json.decodeFromString<List<TimetableEntry>>(v3)
            } catch (e: Exception) {
                emptyList()
            }
            if (list.isNotEmpty()) {
                saveTimetable(list)
            }
            settings.remove("timetable_v3")
            return list
        }
        return emptyList()
    }

    private fun saveTimetable(list: List<TimetableEntry> = _timetable.value) {
        try {
            val string = json.encodeToString<List<TimetableEntry>>(list)
            settings["timetable_v4"] = string
        } catch (e: Exception) {
        }
    }

    private fun loadReminders(): List<Reminder> {
        val v4 = settings.getStringOrNull("reminders_v4")
        if (v4 != null) {
            return try {
                json.decodeFromString<List<Reminder>>(v4)
            } catch (e: Exception) {
                emptyList()
            }
        }
        
        val v3 = settings.getStringOrNull("reminders_v3")
        if (v3 != null) {
            val list = try {
                json.decodeFromString<List<Reminder>>(v3)
            } catch (e: Exception) {
                emptyList()
            }
            if (list.isNotEmpty()) {
                saveReminders(list)
            }
            settings.remove("reminders_v3")
            return list
        }
        return emptyList()
    }

    private fun saveReminders(list: List<Reminder> = _reminders.value) {
        try {
            val string = json.encodeToString<List<Reminder>>(list)
            settings["reminders_v4"] = string
        } catch (e: Exception) {
        }
    }

    companion object {
        private var _instance: Repository? = null
        
        val instance: Repository
            get() = _instance ?: throw IllegalStateException("Repository not initialized. Call init() first.")

        fun init(settings: Settings) {
            if (_instance == null) {
                _instance = Repository(settings)
            }
        }
    }
}
