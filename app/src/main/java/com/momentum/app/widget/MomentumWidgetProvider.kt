package com.momentum.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.momentum.app.MainActivity
import com.momentum.app.R
import com.momentum.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MomentumWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_HABIT = "com.momentum.app.widget.ACTION_TOGGLE_HABIT"
        const val EXTRA_HABIT_ID = "extra_habit_id"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, MomentumWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, MomentumWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val activeHabits = db.habitDao().getActiveHabitsSync()
                val todayLogs = db.habitLogDao().getLogsForDateSync(LocalDate.now())
                val logMap = todayLogs.associateBy { it.habitId }

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_momentum)

                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("navigate_to", "today")
                    }
                    val mainPendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        mainIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_container, mainPendingIntent)

                    views.removeAllViews(R.id.widget_habit_list)

                    activeHabits.take(4).forEach { habit ->
                        val isDone = logMap[habit.id]?.completed == true
                        val itemView = RemoteViews(context.packageName, R.layout.widget_habit_item)
                        itemView.setTextViewText(R.id.widget_habit_name, habit.name)
                        itemView.setTextViewText(R.id.widget_habit_status, if (isDone) "✓" else "◯")

                        val toggleIntent = Intent(context, MomentumWidgetProvider::class.java).apply {
                            action = ACTION_TOGGLE_HABIT
                            putExtra(EXTRA_HABIT_ID, habit.id)
                        }
                        val togglePendingIntent = PendingIntent.getBroadcast(
                            context,
                            habit.id.toInt(),
                            toggleIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        itemView.setOnClickPendingIntent(R.id.widget_item_row, togglePendingIntent)
                        views.addView(R.id.widget_habit_list, itemView)
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_HABIT) {
            val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1L)
            if (habitId != -1L) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val today = LocalDate.now()
                        val logs = db.habitLogDao().getLogsForDateSync(today)
                        val existing = logs.find { it.habitId == habitId }

                        if (existing != null) {
                            val newStatus = !existing.completed
                            db.habitLogDao().upsertHabitLog(
                                existing.copy(
                                    completed = newStatus,
                                    completedAt = if (newStatus) System.currentTimeMillis() else null
                                )
                            )
                        } else {
                            db.habitLogDao().upsertHabitLog(
                                com.momentum.app.data.model.HabitLog(
                                    habitId = habitId,
                                    date = today,
                                    completed = true,
                                    completedAt = System.currentTimeMillis()
                                )
                            )
                        }
                        updateAllWidgets(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
