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

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(context)
        val today = LocalDate.now()

        val moodLog = database.moodLogDao().getMoodForDateSync(today)
        val habitLogs = database.habitLogDao().getLogsForDateSync(today)
        val activeHabits = database.habitDao().getActiveHabitsSync()

        val completedCount = habitLogs.count { it.completed }
        val totalHabits = activeHabits.size

        if (moodLog == null || completedCount < totalHabits) {
            sendReminderNotification(
                context,
                moodLogged = moodLog != null,
                completedHabits = completedCount,
                totalHabits = totalHabits
            )
        }

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "momentum_daily_reminders"
        const val NOTIFICATION_ID = 1001

        fun sendReminderNotification(
            context: Context,
            moodLogged: Boolean,
            completedHabits: Int,
            totalHabits: Int
        ) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Daily Momentum Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders to log daily habits and mood rating"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "today")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val title = if (!moodLogged) "How's your energy today?" else "Keep your Momentum!"
            val text = when {
                !moodLogged && completedHabits == 0 -> "Tap to rate today's energy and check off your habits."
                !moodLogged -> "Rate your mood to see how your $completedHabits habits move the needle!"
                else -> "You've logged $completedHabits/$totalHabits habits. Finish today's chain!"
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }
}
