package com.rahul.mobile.data

data class TimetableApiResponse(
    val scheduleCsv: String,
    val courseCsv: String
)

data class CourseEntry(
    val courseName: String,
    val abbr: String,
    val sections: String,
    val professor: String
)

data class TimetableItem(
    val id: String,
    val date: String,
    val time: String,
    val classroom: String,
    val originalCode: String,
    val abbr: String,
    val section: String,
    val courseName: String,
    val professor: String,
    val isCancelled: Boolean = false
)

data class TimetableEvent(
    val title: String,
    val startDate: String,
    val endDate: String,
    val dates: List<String> = emptyList()
)

data class TimetableData(
    val items: List<TimetableItem>,
    val sections: List<String>,
    val classrooms: List<String>,
    val dates: List<String>,
    val events: List<TimetableEvent>,
    val weather: WeatherData? = null
)

data class UiState(
    val loading: Boolean = false,
    val items: List<TimetableItem> = emptyList(),
    val events: List<TimetableEvent> = emptyList(),
    val sections: List<String> = emptyList(),
    val weather: WeatherData? = null, 
    val error: String? = null,
    val lastSync: String = "Not Synced"
)

data class WeatherData(
    val temp: String,
    val rainChance: String,
    val condition: String,
    val message: String
)