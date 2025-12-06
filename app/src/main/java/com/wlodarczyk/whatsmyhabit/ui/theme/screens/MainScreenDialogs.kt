package com.wlodarczyk.whatsmyhabit.ui.theme.screens

import androidx.compose.runtime.Composable
import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import com.wlodarczyk.whatsmyhabit.ui.theme.components.*

/**
 * Komponent zarządzający wszystkimi dialogami na głównym ekranie
 *
 * ZMIANA: onConfirmAddDialog przyjmuje teraz 4 parametry (dodano color)
 */
@Composable
fun MainScreenDialogs(
    // Dialog dodawania nawyku
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
    onConfirmAddDialog: (String, String, HabitFrequency, Long) -> Unit,  // ← DODANO: Long (color)

    // Dialog usuwania nawyku
    habitToDelete: Habit?,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,

    // Dialogi pozwoleń na powiadomienia
    showNotificationExplanation: Boolean,
    onDismissNotificationExplanation: () -> Unit,
    onConfirmNotificationPermission: () -> Unit,
    showNotificationDeniedDialog: Boolean,
    onDismissNotificationDenied: () -> Unit,
    onOpenSettingsFromNotificationDenied: () -> Unit,

    // Dialog uprawnień o dokładne alarmy
    showAlarmExplanation: Boolean,
    onDismissAlarmExplanation: () -> Unit,
    onConfirmAlarmPermission: () -> Unit,
    showAlarmDeniedDialog: Boolean,
    onDismissAlarmDenied: () -> Unit,
    onOpenSettingsFromAlarmDenied: () -> Unit
) {
    // Dialog dodawania nawyku
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = onDismissAddDialog,
            onConfirm = onConfirmAddDialog  // Teraz pasuje sygnatura: (String, String, HabitFrequency, Long) -> Unit
        )
    }

    // Dialog usuwania nawyku
    if (habitToDelete != null) {
        DeleteHabitDialog(
            habit = habitToDelete,
            onDismiss = onDismissDeleteDialog,
            onConfirm = onConfirmDelete
        )
    }

    // Dialog wyjaśniający uprawnienie do powiadomień
    if (showNotificationExplanation) {
        NotificationPermissionExplanationDialog(
            onDismiss = onDismissNotificationExplanation,
            onConfirm = onConfirmNotificationPermission
        )
    }

    // Dialog informujący o braku uprawnienia do powiadomień
    if (showNotificationDeniedDialog) {
        NotificationPermissionDeniedDialog(
            onDismiss = onDismissNotificationDenied,
            onOpenSettings = onOpenSettingsFromNotificationDenied
        )
    }

    // Dialog wyjaśniający uprawnienie do dokładnych alarmów
    if (showAlarmExplanation) {
        ExactAlarmPermissionExplanationDialog(
            onDismiss = onDismissAlarmExplanation,
            onConfirm = onConfirmAlarmPermission
        )
    }

    // Dialog informujący o braku uprawnienia do dokładnych alarmów
    if (showAlarmDeniedDialog) {
        ExactAlarmPermissionDeniedDialog(
            onDismiss = onDismissAlarmDenied,
            onOpenSettings = onOpenSettingsFromAlarmDenied
        )
    }
}