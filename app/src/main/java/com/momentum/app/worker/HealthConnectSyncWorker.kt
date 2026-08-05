package com.momentum.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.momentum.app.data.health.HealthConnectManager
import com.momentum.app.data.local.AppDatabase
import com.momentum.app.data.model.SourceType
import com.momentum.app.data.repository.MomentumRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Lightweight WorkManager worker that runs background sync of Health Connect passive metrics (Sleep & Steps)
 * every 6 hours or as triggered, respecting battery optimization constraints.
 */
class HealthConnectSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = MomentumRepository(db.habitDao(), db.habitLogDao(), db.moodLogDao())
            val healthManager = HealthConnectManager(applicationContext)

            val activeHabits = repository.activeHabits.first()
            val passiveHabits = activeHabits.filter { it.sourceType != SourceType.MANUAL }

            if (passiveHabits.isNotEmpty()) {
                val today = LocalDate.now()
                val existingLogs = db.habitLogDao().getLogsForDateSync(today)

                passiveHabits.forEach { habit ->
                    val existing = existingLogs.find { it.habitId == habit.id }
                    val evaluatedLog = healthManager.evaluatePassiveHabit(habit, today, existing)
                    if (evaluatedLog != null && evaluatedLog != existing) {
                        db.habitLogDao().upsertHabitLog(evaluatedLog)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "HealthConnectSyncWorker"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<HealthConnectSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
