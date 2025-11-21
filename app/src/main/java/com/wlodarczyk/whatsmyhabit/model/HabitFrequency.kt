package com.wlodarczyk.whatsmyhabit.model

enum class HabitFrequency(val displayName: String, val daysInterval: Int) {
    DAILY("Codziennie", 1),
    EVERY_2_DAYS("Co 2 dni", 2),
    WEEKLY("Co tydzień", 7),
    MONTHLY("Co miesiąc", 30);

    companion object {
        fun fromString(value: String): HabitFrequency {
            return values().find { it.name == value } ?: DAILY
        }
    }
}