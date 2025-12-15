package com.wlodarczyk.whatsmyhabit

import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitColor
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

class HabitUnitTest {

    @Test
    fun `should create habit with default values`() {
        val habit = Habit(
            id = 1,
            name = "Morning workout",
            time = "08:00",
            done = false,
            frequency = HabitFrequency.DAILY,
            color = HabitColor.GREEN.value,
            streak = 0,
            lastCompletedDate = null
        )

        assertEquals("Morning workout", habit.name)
        assertEquals("08:00", habit.time)
        assertFalse(habit.done)
        assertEquals(0, habit.streak)
        assertNull(habit.lastCompletedDate)
    }

    @Test
    fun `should set streak to 1 on first completion`() {
        val habit = Habit(
            id = 1,
            name = "Reading",
            time = "20:00",
            done = false,
            streak = 0,
            lastCompletedDate = null
        )

        val newStreak = habit.updateStreakOnCompletion()

        assertEquals(1, newStreak)
    }

    @Test
    fun `should increase streak when completing next day`() {
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val habit = Habit(
            id = 1,
            name = "Meditation",
            time = "07:00",
            done = false,
            streak = 5,
            lastCompletedDate = yesterday
        )

        val newStreak = habit.updateStreakOnCompletion()

        assertEquals(6, newStreak)
    }

    @Test
    fun `should reset streak after missing a day`() {
        val threeDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
        val habit = Habit(
            id = 1,
            name = "Running",
            time = "06:00",
            done = false,
            streak = 10,
            lastCompletedDate = threeDaysAgo
        )

        val newStreak = habit.updateStreakOnCompletion()

        assertEquals(1, newStreak)
    }

    @Test
    fun `should reset habit after day change`() {
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val habit = Habit(
            id = 1,
            name = "Test habit",
            time = "12:00",
            done = true,
            lastCompletedDate = yesterday
        )

        val shouldReset = habit.shouldReset()

        assertTrue(shouldReset)
    }
}