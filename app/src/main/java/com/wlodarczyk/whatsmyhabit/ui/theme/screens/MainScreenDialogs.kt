package com.wlodarczyk.whatsmyhabit.ui.theme.screens

import androidx.compose.runtime.Composable
import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import com.wlodarczyk.whatsmyhabit.ui.theme.components.*

@Composable
fun MainScreenDialogs(
    // dialog dodawania nawyku
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
    onConfirmAddDialog: (String, String, HabitFrequency) -> Unit,

    // dialog usuwania nawyku
    habitToDelete: Habit?,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,

    // dialogi pozwolena na powiadomienia
    showNotificationExplanation: Boolean,
    onDismissNotificationExplanation: () -> Unit,
    onConfirmNotificationPermission: () -> Unit,
    showNotificationDeniedDialog: Boolean,
    onDismissNotificationDenied: () -> Unit,
    onOpenSettingsFromNotificationDenied: () -> Unit,

    // dialog uprawnień o dokładne alarmy
    showAlarmExplanation: Boolean,
    onDismissAlarmExplanation: () -> Unit,
    onConfirmAlarmPermission: () -> Unit,
    showAlarmDeniedDialog: Boolean,
    onDismissAlarmDenied: () -> Unit,
    onOpenSettingsFromAlarmDenied: () -> Unit
) {
    // dialog dodawania nawyku
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = onDismissAddDialog,
            onConfirm = onConfirmAddDialog
        )
    }

    // dialog usuwania nawyku
    if (habitToDelete != null) {
        DeleteHabitDialog(
            habit = habitToDelete,
            onDismiss = onDismissDeleteDialog,
            onConfirm = onConfirmDelete
        )
    }

    // dialog wyjaśniający uprawnienie do powiadomień
    if (showNotificationExplanation) {
        NotificationPermissionExplanationDialog(
            onDismiss = onDismissNotificationExplanation,
            onConfirm = onConfirmNotificationPermission
        )
    }

    // dialog informujący o braku uprawnienia do powiadomień
    if (showNotificationDeniedDialog) {
        NotificationPermissionDeniedDialog(
            onDismiss = onDismissNotificationDenied,
            onOpenSettings = onOpenSettingsFromNotificationDenied
        )
    }

    // dialog wyjaśniający uprawnienie do dokładnych alarmów
    if (showAlarmExplanation) {
        ExactAlarmPermissionExplanationDialog(
            onDismiss = onDismissAlarmExplanation,
            onConfirm = onConfirmAlarmPermission
        )
    }

    // dialog informujący o braku uprawnienia do dokładnych alarmów
    if (showAlarmDeniedDialog) {
        ExactAlarmPermissionDeniedDialog(
            onDismiss = onDismissAlarmDenied,
            onOpenSettings = onOpenSettingsFromAlarmDenied
        )
    }
}