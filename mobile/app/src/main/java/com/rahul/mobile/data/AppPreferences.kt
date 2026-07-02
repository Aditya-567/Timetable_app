package com.rahul.mobile.data

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean = prefs.getBoolean(KEY_ONBOARDED, false)

    fun saveProfile(fullName: String, photoUri: String?) {
        prefs.edit()
            .putString(KEY_FULL_NAME, fullName.trim())
            .putString(KEY_PHOTO_URI, photoUri)
            .putBoolean(KEY_ONBOARDED, true)
            .apply()
    }

    fun getProfile(): UserProfile {
        return UserProfile(
            fullName = prefs.getString(KEY_FULL_NAME, "") ?: "",
            photoUri = prefs.getString(KEY_PHOTO_URI, null)
        )
    }

    fun saveFilters(courseFilter: String, sectionFilter: String) {
        prefs.edit()
            .putString(KEY_FILTER_COURSE, courseFilter)
            .putString(KEY_FILTER_SECTION, sectionFilter)
            .apply()
    }

    fun getFilters(): SavedFilters {
        return SavedFilters(
            courseFilter = prefs.getString(KEY_FILTER_COURSE, FILTER_ALL) ?: FILTER_ALL,
            sectionFilter = prefs.getString(KEY_FILTER_SECTION, FILTER_ALL) ?: FILTER_ALL
        )
    }

    fun saveNotificationSettings(settings: NotificationSettings) {
        prefs.edit()
            .putBoolean(KEY_NOTIF_ENABLED, settings.enabled)
            .putString(KEY_NOTIF_SECTION, settings.section)
            .putInt(KEY_NOTIF_MINUTES_BEFORE, settings.minutesBefore)
            .apply()
    }

    fun getNotificationSettings(): NotificationSettings {
        return NotificationSettings(
            enabled = prefs.getBoolean(KEY_NOTIF_ENABLED, false),
            section = prefs.getString(KEY_NOTIF_SECTION, FILTER_ALL) ?: FILTER_ALL,
            minutesBefore = prefs.getInt(KEY_NOTIF_MINUTES_BEFORE, 10)
        )
    }

    data class UserProfile(
        val fullName: String,
        val photoUri: String?
    )

    data class SavedFilters(
        val courseFilter: String,
        val sectionFilter: String
    )

    data class NotificationSettings(
        val enabled: Boolean,
        val section: String,
        val minutesBefore: Int
    )

    companion object {
        const val FILTER_ALL = "all"

        private const val PREFS_NAME = "campussync_prefs"
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_PHOTO_URI = "photo_uri"
        private const val KEY_FILTER_COURSE = "filter_course"
        private const val KEY_FILTER_SECTION = "filter_section"
        private const val KEY_NOTIF_ENABLED = "notif_enabled"
        private const val KEY_NOTIF_SECTION = "notif_section"
        private const val KEY_NOTIF_MINUTES_BEFORE = "notif_minutes_before"
    }
}
