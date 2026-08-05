package com.momentum.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.momentum.app.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userPrefs = UserPreferencesRepository(context)
                    val timesList = userPrefs.reminderTimesList.first()
                    val enabled = userPrefs.isReminderEnabled.first()
                    ReminderScheduler.scheduleDailyReminders(context, timesList, enabled)

                    val database = com.momentum.app.data.local.AppDatabase.getDatabase(context)
                    val activeHabits = database.habitDao().getActiveHabitsSync()
                    ReminderScheduler.scheduleHabitReminders(context, activeHabits)
                    HealthConnectSyncWorker.schedule(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

