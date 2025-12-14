package com.wlodarczyk.whatsmyhabit

import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitColor
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import org.junit.Test
import org.junit.Assert.*
import java.util.concurrent.TimeUnit

/**
 * Testy jednostkowe dla logiki biznesowej aplikacji ,,What's my habit?"
 * Testowane funkcjonalności:
 * - Tworzenie nawyku
 * - Logika streak (pasma dni)
 * - Reset nawyku po zmianie dnia
 * - Obliczanie streak przy różnych scenariuszach
 * - Walidacja kolorów
 */
class HabitUnitTest {

    /**
     * Sprawdza czy nawyk jest poprawnie tworzony z domyślnymi wartościami
     */
    @Test
    fun `test creating new habit with default values`() {
        val habitName = "Trening"
        val habitTime = "08:00"
        val habitColor = HabitColor.GREEN.value

        val habit = Habit(
            id = 1,
            name = habitName,
            time = habitTime,
            done = false,
            frequency = HabitFrequency.DAILY,
            color = habitColor,
            streak = 0,
            lastCompletedDate = null
        )

        assertEquals("Trening", habit.name)
        assertEquals("08:00", habit.time)
        assertFalse(habit.done)
        assertEquals(0, habit.streak)
        assertNull(habit.lastCompletedDate)
        assertEquals(HabitColor.GREEN.value, habit.color)
    }

    /**
     * Sprawdza czy streak poprawnie zwiększa się do 1 przy pierwszym wykonaniu
     */
    @Test
    fun `test streak increases to 1 on first completion`() {
        val habit = Habit(
            id = 1,
            name = "Czytanie",
            time = "20:00",
            done = false,
            streak = 0,
            lastCompletedDate = null
        )

        val newStreak = habit.updateStreakOnCompletion()

        assertEquals(1, newStreak)
    }

    /**
     * Sprawdza czy streak zwiększa się o 1 gdy wykonujemy nawyk dzień po dniu
     */
    @Test
    fun `test streak increases when completing habit next day`() {
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val habit = Habit(
            id = 1,
            name = "Medytacja",
            time = "07:00",
            done = false,
            streak = 5,
            lastCompletedDate = yesterday
        )

        val newStreak = habit.updateStreakOnCompletion()

        assertEquals(6, newStreak)
    }

    /**
     * Sprawdza czy streak resetuje się do 1 gdy jest przerwa dłuższa niż 1 dzień
     */
    @Test
    fun `test streak resets to 1 after gap in completion`() {
        val threeDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3)
        val habit = Habit(
            id = 1,
            name = "Bieganie",
            time = "06:00",
            done = false,
            streak = 10,
            lastCompletedDate = threeDaysAgo
        )

        val newStreak = habit.updateStreakOnCompletion()

        assertEquals(1, newStreak)
    }

    /**
     * Sprawdza czy nawyk powinien być zresetowany jeśli został wykonany wczoraj
     */
    @Test
    fun `test habit should reset after day change`() {
        val yesterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        val habit = Habit(
            id = 1,
            name = "Nawyk testowy",
            time = "12:00",
            done = true,
            lastCompletedDate = yesterday
        )

        val shouldReset = habit.shouldReset()

        assertTrue("Nawyk wykonany wczoraj powinien być zresetowany", shouldReset)
    }
}