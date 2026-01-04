package com.wlodarczyk.whatsmyhabit.ui.theme.screens

import androidx.compose.runtime.Composable
import com.wlodarczyk.whatsmyhabit.model.Habit
import com.wlodarczyk.whatsmyhabit.model.HabitFrequency
import com.wlodarczyk.whatsmyhabit.ui.theme.components.AddHabitDialog
import com.wlodarczyk.whatsmyhabit.ui.theme.components.DeleteHabitDialog
import com.wlodarczyk.whatsmyhabit.ui.theme.components.ExactAlarmPermissionDeniedDialog
import com.wlodarczyk.whatsmyhabit.ui.theme.components.ExactAlarmPermissionExplanationDialog
import com.wlodarczyk.whatsmyhabit.ui.theme.components.NotificationPermissionDeniedDialog
import com.wlodarczyk.whatsmyhabit.ui.theme.components.NotificationPermissionExplanationDialog

@Composable
fun MainScreenDialogs(
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
    onConfirmAddDialog: (String, String, HabitFrequency, Long) -> Unit,

    habitToDelete: Habit?,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,

    showNotificationExplanation: Boolean,
    onDismissNotificationExplanation: () -> Unit,
    onConfirmNotificationPermission: () -> Unit,
    showNotificationDeniedDialog: Boolean,
    onDismissNotificationDenied: () -> Unit,
    onOpenSettingsFromNotificationDenied: () -> Unit,

    showAlarmExplanation: Boolean,
    onDismissAlarmExplanation: () -> Unit,
    onConfirmAlarmPermission: () -> Unit,
    showAlarmDeniedDialog: Boolean,
    onDismissAlarmDenied: () -> Unit,
    onOpenSettingsFromAlarmDenied: () -> Unit
) {
    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = onDismissAddDialog,
            onConfirm = onConfirmAddDialog
        )
    }

    if (habitToDelete != null) {
        DeleteHabitDialog(
            habit = habitToDelete,
            onDismiss = onDismissDeleteDialog,
            onConfirm = onConfirmDelete
        )
    }

    if (showNotificationExplanation) {
        NotificationPermissionExplanationDialog(
            onDismiss = onDismissNotificationExplanation,
            onConfirm = onConfirmNotificationPermission
        )
    }

    if (showNotificationDeniedDialog) {
        NotificationPermissionDeniedDialog(
            onDismiss = onDismissNotificationDenied,
            onOpenSettings = onOpenSettingsFromNotificationDenied
        )
    }

    if (showAlarmExplanation) {
        ExactAlarmPermissionExplanationDialog(
            onDismiss = onDismissAlarmExplanation,
            onConfirm = onConfirmAlarmPermission
        )
    }

    if (showAlarmDeniedDialog) {
        ExactAlarmPermissionDeniedDialog(
            onDismiss = onDismissAlarmDenied,
            onOpenSettings = onOpenSettingsFromAlarmDenied
        )
    }
}