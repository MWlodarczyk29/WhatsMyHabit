package com.wlodarczyk.whatsmyhabit.model

data class Habit(
    val id: Int,
    val name: String,
    val time: String,
    var done: Boolean = false,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val createdDate: Long = System.currentTimeMillis(),
    var lastCompletedDate: Long? = null
) {
    /**
     * sprawdza czy nawyk powinien być aktywny dzisiaj
     */
    fun isActiveToday(): Boolean {
        val today = getTodayStartMillis()
        val createdDay = getDayStartMillis(createdDate)

        val daysDiff = ((today - createdDay) / (24 * 60 * 60 * 1000)).toInt()

        return when (frequency) {
            HabitFrequency.DAILY -> true
            HabitFrequency.EVERY_2_DAYS -> daysDiff % 2 == 0
            HabitFrequency.WEEKLY -> daysDiff % 7 == 0
            HabitFrequency.MONTHLY -> daysDiff % 30 == 0
        }
    }

    /**
     * sprawdza czy nawyk powinien zostać zresetowany
     * (czy jest nowy dzień i nawyk był już zaznaczony)
     */
    fun shouldReset(): Boolean {
        if (!done || lastCompletedDate == null) return false

        val today = getTodayStartMillis()
        val completedDay = getDayStartMillis(lastCompletedDate!!)

        return today > completedDay
    }

    private fun getTodayStartMillis(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getDayStartMillis(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}