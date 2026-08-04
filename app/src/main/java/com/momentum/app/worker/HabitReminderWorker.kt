package com.momentum.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.momentum.app.MainActivity
import com.momentum.app.R
import com.momentum.app.data.local.AppDatabase
import java.time.LocalDate

class HabitReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getLong("habit_id", -1L)
        val habitName = inputData.getString("habit_name") ?: "Habit"

        if (habitId == -1L) return Result.success()

        val database = AppDatabase.getDatabase(context)
        val today = LocalDate.now()

        val activeHabits = database.habitDao().getActiveHabitsSync()
        val targetHabit = activeHabits.find { it.id == habitId }

        // If habit is deleted or archived, skip notification
        if (targetHabit == null) return Result.success()

        val habitLogs = database.habitLogDao().getLogsForDateSync(today)
        val isCompleted = habitLogs.any { it.habitId == habitId && it.completed }

        // Only send notification if habit is not completed today yet
        if (!isCompleted) {
            sendHabitNotification(context, habitId, targetHabit.name)
        }

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "momentum_habit_reminders"

        fun sendHabitNotification(
            context: Context,
            habitId: Long,
            habitName: String
        ) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Individual Habit Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for specific tracked habits"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "today")
                putExtra("habit_id", habitId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                habitId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Time for $habitName! ✨")
                .setContentText("Tap to mark '$habitName' complete and build your streak.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify((2000 + habitId).toInt(), notification)
        }
    }
}
