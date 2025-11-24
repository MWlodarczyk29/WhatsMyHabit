package com.wlodarczyk.whatsmyhabit.model

enum class HabitFrequency(val displayName: String, val displayNameEn: String, val daysInterval: Int) {
    DAILY("Codziennie", "Daily", 1);

    companion object {
        fun fromString(value: String): HabitFrequency {
            return values().find { it.name == value } ?: DAILY
        }
    }

    fun getDisplayName(isEnglish: Boolean): String {
        return if (isEnglish) displayNameEn else displayName
    }
}