package com.momentum.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.momentum.app.data.model.Habit
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    const val WORK_NAME_PREFIX = "daily_habit_reminder"

    /**
     * Schedules daily general habit logging reminders for multiple custom times.
     */
    fun scheduleDailyReminders(
        context: Context,
        times: List<Pair<Int, Int>>,
        enabled: Boolean = true
    ) {
        val workManager = WorkManager.getInstance(context.applicationContext)

        if (!enabled || times.isEmpty()) {
            workManager.cancelAllWorkByTag("general_daily_reminder")
            return
        }

        // Cancel all existing general daily reminders to avoid orphaned tasks
        workManager.cancelAllWorkByTag("general_daily_reminder")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val now = LocalDateTime.now()

        times.forEach { (hour, minute) ->
            val targetTime = LocalTime.of(hour, minute)
            var targetDateTime = now.with(targetTime).withSecond(0).withNano(0)

            if (now.isAfter(targetDateTime)) {
                targetDateTime = targetDateTime.plusDays(1)
            }

            val initialDelayMillis = Duration.between(now, targetDateTime).toMillis()

            val reminderWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("general_daily_reminder")
                .build()

            val uniqueName = "${WORK_NAME_PREFIX}_${hour}_${minute}"
            workManager.enqueueUniquePeriodicWork(
                uniqueName,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                reminderWorkRequest
            )
        }
    }

    fun scheduleDailyReminder(
        context: Context,
        hour: Int = 21,
        minute: Int = 0,
        enabled: Boolean = true
    ) {
        scheduleDailyReminders(context, listOf(Pair(hour, minute)), enabled)
    }

    /**
     * Schedules reminders for specific individual habits based on their custom reminder times.
     */
    fun scheduleHabitReminders(
        context: Context,
        habits: List<Habit>
    ) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        
        // Cancel existing habit reminder tags first
        workManager.cancelAllWorkByTag("individual_habit_reminder")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val now = LocalDateTime.now()

        habits.filter { it.archivedAt == null }.forEach { habit ->
            val reminderTimes = habit.getReminderTimesList()
            reminderTimes.forEach { (hour, minute) ->
                val targetTime = LocalTime.of(hour, minute)
                var targetDateTime = now.with(targetTime).withSecond(0).withNano(0)

                if (now.isAfter(targetDateTime)) {
                    targetDateTime = targetDateTime.plusDays(1)
                }

                val initialDelayMillis = Duration.between(now, targetDateTime).toMillis()

                val workData = workDataOf(
                    "habit_id" to habit.id,
                    "habit_name" to habit.name
                )

                val habitWorkRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
                    24, TimeUnit.HOURS
                )
                    .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(workData)
                    .setConstraints(constraints)
                    .addTag("individual_habit_reminder")
                    .addTag("habit_${habit.id}")
                    .build()

                val uniqueName = "habit_reminder_${habit.id}_${hour}_${minute}"
                workManager.enqueueUniquePeriodicWork(
                    uniqueName,
                    ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                    habitWorkRequest
                )
            }
        }
    }
}


