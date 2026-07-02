package com.rahul.mobile.data

import com.rahul.mobile.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class TimetableRepository(
    private val parser: TimetableParser = TimetableParser(),
    private val client: OkHttpClient = OkHttpClient()
) {

    suspend fun loadTimetable(): TimetableData = withContext(Dispatchers.IO) {
        val url = BuildConfig.TIMETABLE_BASE_URL.trimEnd('/') + "/api/timetable"
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    val scheduleCsv = json.optString("scheduleCsv", "")
                    val courseCsv = json.optString("courseCsv", "")
                    if (scheduleCsv.isNotBlank() && courseCsv.isNotBlank()) {
                        val parsed = parser.parse(scheduleCsv, courseCsv)
                        val weatherJson = json.optJSONObject("weather")
                        val apiWeather = if (weatherJson != null) {
                            WeatherData(
                                temp = weatherJson.optString("temp", "N/A"),
                                rainChance = weatherJson.optString("rainChance", ""),
                                condition = weatherJson.optString("condition", ""),
                                message = weatherJson.optString("message", "")
                            )} else null
                        // Prefer allData.events (structured block from backend), fall back to top-level events
                        val allData = json.optJSONObject("allData")
                        val backendEvents = (allData?.optJSONArray("events")
                            ?: json.optJSONArray("events"))?.toTimetableEvents().orEmpty()
                        return@withContext if (backendEvents.isNotEmpty()) {
                            parsed.copy(events = backendEvents, weather = apiWeather)
                        } else {
                            parsed.copy(weather = apiWeather)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback to sample data when backend is unavailable.
        }

        parser.parse(SAMPLE_SCHEDULE_CSV, SAMPLE_COURSE_CSV)
    }

    fun formatLastSync(): String {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
    }

    private fun JSONArray.toTimetableEvents(): List<TimetableEvent> {
        val result = mutableListOf<TimetableEvent>()
        for (index in 0 until length()) {
            val item = optJSONObject(index) ?: continue
            val title = item.optString("title", "").trim()
            val startDate = item.optString("startDate", "").trim()
            val endDate = item.optString("endDate", startDate).trim()
            val datesArray = item.optJSONArray("dates")
            val dates = mutableListOf<String>()
            if (datesArray != null) {
                for (dateIndex in 0 until datesArray.length()) {
                    val date = datesArray.optString(dateIndex, "").trim()
                    if (date.isNotBlank()) {
                        dates += date
                    }
                }
            }

            if (title.isNotBlank() && startDate.isNotBlank()) {
                result += TimetableEvent(
                    title = title,
                    startDate = startDate,
                    endDate = if (endDate.isBlank()) startDate else endDate,
                    dates = dates
                )
            }
        }
        return result
    }

    companion object {
        private const val SAMPLE_SCHEDULE_CSV = """
Date,Time,CR A1,CR A2,CR B1,CR B2
2026-06-29,08:30 - 10:00,DA-A,ME-FIN,CS-B,
2026-06-29,10:15 - 11:45,CS-A,,ME-LSM,DA-B
2026-06-29,12:00 - 13:00,LUNCH BREAK,LUNCH BREAK,LUNCH BREAK,LUNCH BREAK
2026-06-29,13:15 - 14:45,ME-A,DA-FIN,,CS-B
2026-06-30,08:30 - 10:00,CS-B,DA-B,,ME-FIN
2026-06-30,10:15 - 11:45,,CS-A,DA-A,ME-LSM
"""

        private const val SAMPLE_COURSE_CSV = """
Course,Abbr.,Credit,Sections,Professor
Data Analytics & Science,DA,3,DA-A DA-B DA-FIN DA-LSM,Dr. Sarah Connor
Mechanical Engineering,ME,3,ME-A ME-B ME-FIN ME-LSM,Prof. Charles Xavier
Computer Science & AI,CS,3,CS-A CS-B CS-LSM,Dr. Alan Turing
"""
    }
}
