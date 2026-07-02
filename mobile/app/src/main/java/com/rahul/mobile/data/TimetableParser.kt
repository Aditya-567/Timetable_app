package com.rahul.mobile.data

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.StringReader
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class TimetableParser {

    fun parse(scheduleCsv: String, courseCsv: String): TimetableData {
        val courseEntries = parseCourseEntries(courseCsv)
        val cleanedScheduleCsv = cleanScheduleHeader(scheduleCsv)
        val scheduleParser = CSVParser(
            StringReader(cleanedScheduleCsv),
            CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).build()
        )

        val items = mutableListOf<TimetableItem>()
        val events = mutableListOf<TimetableEvent>()
        val sections = sortedSetOf<String>()
        val classrooms = sortedSetOf<String>()
        val dates = mutableSetOf<String>()

        scheduleParser.records.forEachIndexed { rowIndex, row ->
            val date = (row.getSafe("Date") ?: row.getSafe("date") ?: "").trim()
            val time = (row.getSafe("Time") ?: row.getSafe("time") ?: "").trim()
            if (date.isBlank()) return@forEachIndexed

            dates += date

            if (time.isBlank()) {
                val eventTitle = extractEventTitle(row)
                if (!eventTitle.isNullOrBlank()) {
                    events += TimetableEvent(title = eventTitle, startDate = date, endDate = date, dates = listOf(date))
                }
                return@forEachIndexed
            }

            row.toMap().forEach { (rawKey, rawValue) ->
                val key = rawKey.trim()
                if (key.equals("date", ignoreCase = true) || key.equals("time", ignoreCase = true) || key.isBlank()) {
                    return@forEach
                }

                val value = (rawValue ?: "").trim()
                if (value.isBlank()) return@forEach
                
                classrooms += key
                val normalizedValue = normalizeCellValue(value)
                val isLunch = normalizedValue.equals("LUNCH BREAK", ignoreCase = true)
                val isBlocked = normalizedValue.equals("BLOCKED", ignoreCase = true)
                val isCancelled = isCancelledCell(value)
                
                val mapped = when {
    isLunch -> CourseMapping("", "", "Lunch Break", "")
    isBlocked -> CourseMapping("", "", "Blocked Slot", "Reserved")
    else -> mapCellValue(normalizedValue, courseEntries)
}

                // Lunch/Blocked slots repeat identically across every classroom column for the
                // same date/time, so collapse them to one entry. Regular classes must be deduped
                // by abbr+section (not courseName): different sections of the same course, e.g.
                // OME-B and OME-F, share a courseName but run in different classrooms at the same
                // time, and comparing by courseName alone was silently dropping all but the first
                // one encountered.
                val alreadyExists = items.any {
                    it.date == date && it.time == time &&
                        when {
                            isLunch -> it.courseName == "Lunch Break"
                            isBlocked -> it.courseName == "Blocked Slot"
                            else -> it.abbr == mapped.abbr && it.section == mapped.section
                        }
                }
                if (alreadyExists) return@forEach

                if (mapped.section.isNotBlank() && mapped.section != "N/A") {
                    sections += mapped.section
                }

                items += TimetableItem(
                    id = "item-$rowIndex-$key-${mapped.abbr}-${mapped.section}",
                    date = date,
                    time = time,
                    classroom = if (isLunch) "" else key,
                    originalCode = if (isLunch) "LUNCH" else normalizedValue,
                    abbr = mapped.abbr,
                    section = mapped.section,
                    courseName = mapped.courseName,
                    professor = mapped.professor,
                    isCancelled = isCancelled
                )
            }
        }

        return TimetableData(
            items = items.sortedWith(compareBy(
                { parseDate(it.date) ?: LocalDate.MAX },
                { parseStartTime(it.time) ?: LocalTime.MAX },
                { it.classroom }
            )),
            sections = sections.toList(),
            classrooms = classrooms.toList(),
            dates = dates.toList().sortedWith(compareBy({ parseDate(it) ?: LocalDate.MAX }, { it })),
            events = events
                .distinctBy { "${it.startDate.uppercase(Locale.US)}|${it.endDate.uppercase(Locale.US)}|${it.title.uppercase(Locale.US)}" }
                .sortedWith(compareBy({ parseDate(it.startDate) ?: LocalDate.MAX }, { parseDate(it.endDate) ?: LocalDate.MAX }, { it.title }))
        )
    }

    private fun extractEventTitle(row: org.apache.commons.csv.CSVRecord): String? {
        val candidate = row.toMap()
            .asSequence()
            .filter { (rawKey, _) ->
                val key = rawKey.trim()
                !key.equals("date", ignoreCase = true) && !key.equals("time", ignoreCase = true)
            }
            .mapNotNull { (_, rawValue) -> rawValue?.trim()?.takeIf { it.isNotBlank() } }
            .filterNot { it.equals("LUNCH BREAK", ignoreCase = true) }
            .maxByOrNull { it.length }
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            ?: return null

        if (candidate.equals("BLOCKED", ignoreCase = true)) return null
        if (looksLikeClassCode(candidate)) return null
        return candidate
    }

    private fun looksLikeClassCode(value: String): Boolean {
        return Regex("^[A-Za-z]{1,8}\\s*-\\s*[A-Za-z0-9]{1,12}$").matches(value.trim())
    }

    private fun mapCellValue(cellValue: String, courseEntries: List<CourseEntry>): CourseMapping {
        val upper = cellValue.uppercase(Locale.US)

        if (upper == "BLOCKED") {
            return CourseMapping("BLOCKED", "", "Blocked Slot", "Reserved")
        }
        if (upper.contains("HOLIDAY") || upper.contains("VACATION") || upper.contains("OFF-DAY") || upper.contains("OFF DAY")) {
            return CourseMapping("HOLIDAY", "", cellValue, "Academic Holiday")
        }

        val hyphenIndex = cellValue.indexOf('-')
        val abbr: String
        val section: String
        if (hyphenIndex != -1) {
            abbr = cellValue.substring(0, hyphenIndex).trim().uppercase(Locale.US)
            section = cellValue.substring(hyphenIndex + 1).trim()
        } else {
            abbr = cellValue.trim().uppercase(Locale.US)
            section = "N/A"
        }

        val exactSection = courseEntries.firstOrNull { entry ->
            entry.sections
                .split(Regex("[\\s,]+"))
                .map { it.trim().uppercase(Locale.US) }
                .contains(upper)
        }

        val byAbbrAndSection = exactSection ?: courseEntries.firstOrNull { entry ->
            if (!entry.abbr.equals(abbr, ignoreCase = true)) return@firstOrNull false
            val secText = entry.sections.trim().uppercase(Locale.US)
            val normalizedSection = section.uppercase(Locale.US)
            secText.contains(normalizedSection) || normalizedSection.contains(secText)
        }

        val fallback = byAbbrAndSection ?: courseEntries.firstOrNull { it.abbr.equals(abbr, ignoreCase = true) }

        return if (fallback != null) {
            CourseMapping(abbr, section, fallback.courseName, fallback.professor.ifBlank { "Staff/Instructor" })
        } else {
            CourseMapping(abbr, section, cellValue, "Staff/Instructor")
        }
    }

    private fun parseCourseEntries(courseCsv: String): List<CourseEntry> {
        val parser = CSVParser(StringReader(courseCsv), CSVFormat.DEFAULT)
        val result = mutableListOf<CourseEntry>()

        var currentCourse = ""
        var currentAbbr = ""

        parser.records.forEach { row ->
            val c0 = row.getOrEmpty(0).trim()
            val c1 = row.getOrEmpty(1).trim()
            val c3 = row.getOrEmpty(3).trim()
            val c4 = row.getOrEmpty(4).trim()

            if (c0.equals("course", ignoreCase = true) || c1.equals("abbr.", ignoreCase = true) || c0.contains("course details", ignoreCase = true)) {
                return@forEach
            }
            if (c0.isBlank() && c1.isBlank() && c3.isBlank() && c4.isBlank()) return@forEach

            if (c0.isNotBlank()) currentCourse = c0
            if (c1.isNotBlank()) currentAbbr = c1

            if (currentAbbr.isNotBlank()) {
                result += CourseEntry(currentCourse, currentAbbr, c3, if (c4.isBlank()) "Staff/Instructor" else c4)
            }
        }
        return result
    }

    private fun cleanScheduleHeader(scheduleCsv: String): String {
        val lines = scheduleCsv.split(Regex("\\r?\\n"))
        val startIndex = lines.indexOfFirst {
            val lower = it.lowercase(Locale.US)
            lower.contains("date") && lower.contains("time") && lower.contains(",")
        }
        return if (startIndex >= 0) lines.drop(startIndex).joinToString("\n") else scheduleCsv
    }

    private fun parseDate(value: String): LocalDate? {
        val raw = value.trim()
        if (raw.isBlank()) return null
        val normalized = if (raw.contains(",")) raw.substringAfter(',').trim() else raw

        val formats = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("d.M.yyyy"),
            DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US),
            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
        )
        for (formatter in formats) {
            try { return LocalDate.parse(normalized, formatter) } catch (_: DateTimeParseException) {}
        }
        return null
    }

    private fun parseStartTime(timeRange: String): LocalTime? {
        val head = timeRange
    .split(Regex("\\s*[-–—]\\s*"))
    .firstOrNull()
    .orEmpty()
    .trim()
    .replace(" ", "")
    .replace(".", ":") // Converts 13.40 to 13:40
        val formats = listOf(
            DateTimeFormatter.ofPattern("H.mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("HH.mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("h:mma", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("hh:mma", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        )
        formats.forEach { fmt ->
            val parsed = runCatching {
                LocalTime.parse(head.uppercase(Locale.ENGLISH), fmt)
            }.getOrNull()
            if (parsed != null) return parsed
        }
        return null
    }

    private data class CourseMapping(val abbr: String, val section: String, val courseName: String, val professor: String)

    private fun isCancelledCell(cellValue: String): Boolean {
        val upper = cellValue.uppercase(Locale.US)
        if (upper.contains("BLOCKED")) return false
        return upper.contains("CANCEL") || upper.contains("CANCELLED") || upper.contains("CANCELED") || upper.contains("[RED]")
    }

    private fun normalizeCellValue(cellValue: String): String {
        return cellValue
            .replace("[RED]", "", ignoreCase = true)
            .replace("CANCELLED", "", ignoreCase = true)
            .replace("CANCELED", "", ignoreCase = true)
            .replace("CANCEL", "", ignoreCase = true)
            .replace(Regex("\\(\\s*\\)"), "")
            .trim()
    }

    private fun org.apache.commons.csv.CSVRecord.getSafe(key: String): String? = if (isMapped(key)) get(key) else null
    private fun org.apache.commons.csv.CSVRecord.getOrEmpty(index: Int): String = if (size() > index) get(index) else ""
}