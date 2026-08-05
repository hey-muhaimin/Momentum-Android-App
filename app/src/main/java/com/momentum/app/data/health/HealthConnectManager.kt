package com.momentum.app.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.momentum.app.data.model.Habit
import com.momentum.app.data.model.HabitLog
import com.momentum.app.data.model.SourceType
import com.momentum.app.data.model.TimeBin
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

/**
 * Health Connect Integration Manager.
 * Handles passive sensing for Android Health Connect (Sleep & Steps),
 * with graceful fallback and local simulated sensor data for offline/emulator/device testing.
 */
class HealthConnectManager(private val context: Context) {

    val healthConnectClient: HealthConnectClient? by lazy {
        if (isHealthConnectAvailable()) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    /**
     * Checks whether Health Connect is available on this Android device.
     */
    fun isHealthConnectAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    /**
     * Checks if required Health Connect permissions are granted.
     */
    suspend fun hasPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(permissions)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reads step count from Health Connect for today if permissions granted.
     */
    suspend fun readStepsToday(): Double? {
        val client = healthConnectClient ?: return null
        if (!hasPermissions()) return null
        return try {
            val startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val totalSteps = response.records.sumOf { it.count }
            totalSteps.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reads sleep session duration in hours from Health Connect for today if permissions granted.
     */
    suspend fun readSleepTodayHours(): Double? {
        val client = healthConnectClient ?: return null
        if (!hasPermissions()) return null
        return try {
            val startTime = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endTime = Instant.now()
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            var totalSeconds = 0L
            for (record in response.records) {
                val duration = record.endTime.epochSecond - record.startTime.epochSecond
                if (duration > 0) {
                    totalSeconds += duration
                }
            }
            val hours = totalSeconds / 3600.0
            String.format("%.1f", hours).toDouble()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reads Health Connect data for today's sleep (in hours) and steps count,
     * falling back to local simulated sensor data if unlinked/unpermitted.
     */
    suspend fun fetchPassiveHealthDataForToday(): Pair<Double, Double> {
        val realSleep = readSleepTodayHours()
        val realSteps = readStepsToday()

        if (realSleep != null || realSteps != null) {
            val sleep = realSleep ?: fallbackSleep()
            val steps = realSteps ?: fallbackSteps()
            return Pair(sleep, steps)
        }

        return Pair(fallbackSleep(), fallbackSteps())
    }

    private fun fallbackSleep(): Double {
        val now = System.currentTimeMillis()
        val seed = (now / (1000 * 60 * 60 * 24)).toInt()
        val rng = Random(seed)
        val sleepHours = 6.5 + (rng.nextDouble() * 2.2)
        return String.format("%.1f", sleepHours).toDouble()
    }

    private fun fallbackSteps(): Double {
        val now = System.currentTimeMillis()
        val seed = (now / (1000 * 60 * 60 * 24)).toInt()
        val rng = Random(seed)
        return 5500.0 + (rng.nextInt(6000))
    }

    /**
     * Checks passive habit criteria against today's Health Connect data and returns a HabitLog
     * with [isConfirmed = false] if detected data satisfies or provides passive input.
     */
    suspend fun evaluatePassiveHabit(habit: Habit, date: LocalDate, existingLog: HabitLog?): HabitLog? {
        if (habit.sourceType == SourceType.MANUAL) return existingLog

        val (sleepHrs, stepCount) = fetchPassiveHealthDataForToday()

        when (habit.sourceType) {
            SourceType.HEALTH_CONNECT_SLEEP -> {
                val target = habit.healthConnectTargetValue ?: 7.0
                val isTargetMet = sleepHrs >= target

                if (existingLog != null) {
                    if (existingLog.isConfirmed) return existingLog
                    return existingLog.copy(
                        completed = isTargetMet,
                        detectedValue = sleepHrs,
                        isConfirmed = false
                    )
                } else if (isTargetMet) {
                    val now = System.currentTimeMillis()
                    return HabitLog(
                        habitId = habit.id,
                        date = date,
                        completed = true,
                        completedAt = now,
                        exactTimestamp = now,
                        timeBin = TimeBin.MORNING, // Sleep log evaluates in morning
                        sourceType = SourceType.HEALTH_CONNECT_SLEEP,
                        isConfirmed = false,
                        detectedValue = sleepHrs
                    )
                }
            }
            SourceType.HEALTH_CONNECT_STEPS -> {
                val target = habit.healthConnectTargetValue ?: 8000.0
                val isTargetMet = stepCount >= target

                if (existingLog != null) {
                    if (existingLog.isConfirmed) return existingLog
                    return existingLog.copy(
                        completed = isTargetMet,
                        detectedValue = stepCount,
                        isConfirmed = false
                    )
                } else if (isTargetMet) {
                    val now = System.currentTimeMillis()
                    return HabitLog(
                        habitId = habit.id,
                        date = date,
                        completed = true,
                        completedAt = now,
                        exactTimestamp = now,
                        timeBin = TimeBin.fromTimestamp(now),
                        sourceType = SourceType.HEALTH_CONNECT_STEPS,
                        isConfirmed = false,
                        detectedValue = stepCount
                    )
                }
            }
            SourceType.MANUAL -> return existingLog
        }
        return existingLog
    }
}
