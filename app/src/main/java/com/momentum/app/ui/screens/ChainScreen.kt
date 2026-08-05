package com.momentum.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momentum.app.data.model.Habit
import com.momentum.app.data.model.HabitLog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

enum class CalendarCellState {
    DONE,
    GRACE,
    PAUSED,
    MISS,
    FUTURE
}

data class MonthDayCell(
    val date: LocalDate?,
    val state: CalendarCellState,
    val dayNumber: Int?
)

@Composable
fun ChainScreen(
    activeHabits: List<Habit>,
    selectedHabitId: Long?,
    selectedLogs: List<HabitLog>,
    onSelectHabit: (Long) -> Unit,
    calculateStreak: (Habit) -> Int,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(activeHabits, selectedHabitId) {
        if (selectedHabitId == null && activeHabits.isNotEmpty()) {
            onSelectHabit(activeHabits.first().id)
        }
    }

    val selectedHabit = activeHabits.find { it.id == selectedHabitId } ?: activeHabits.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "MONTHLY HABIT CALENDAR",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Horizontal Habit Selector Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeHabits, key = { it.id }) { habit ->
                    val isSelected = habit.id == selectedHabit?.id

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { onSelectHabit(habit.id) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = habit.name,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Monthly Calendar Card
        if (selectedHabit != null) {
            val streak = calculateStreak(selectedHabit)

            item {
                MonthlyCalendarCard(
                    habit = selectedHabit,
                    streak = streak,
                    logs = selectedLogs
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun MonthlyCalendarCard(
    habit: Habit,
    streak: Int,
    logs: List<HabitLog>
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = remember { LocalDate.now() }
    val logMap = remember(logs) { logs.associateBy { it.date } }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    val habitColor = remember(habit.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }.getOrDefault(primaryColor)
    }

    // Days calculation for current month grid
    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1 // Mon = 0, Sun = 6

    val gridItems = remember(currentYearMonth, logs) {
        val list = mutableListOf<MonthDayCell>()
        // Blank cells before day 1
        repeat(dayOfWeekOffset) {
            list.add(MonthDayCell(date = null, state = CalendarCellState.FUTURE, dayNumber = null))
        }
        // Days 1..daysInMonth
        var lastGraceDate: LocalDate? = null
        for (d in 1..daysInMonth) {
            val date = currentYearMonth.atDay(d)
            val isFuture = date.isAfter(today)
            val log = logMap[date]
            val isCompleted = log?.completed == true

            val state = when {
                isCompleted -> CalendarCellState.DONE
                isFuture -> CalendarCellState.FUTURE
                else -> {
                    // Check if grace applies for a single missed day
                    val prevLog = logMap[date.minusDays(1)]
                    val nextLog = logMap[date.plusDays(1)]
                    val isSingleMiss = (prevLog?.completed == true) || (nextLog?.completed == true)
                    val daysSinceGrace = lastGraceDate?.let { date.toEpochDay() - it.toEpochDay() } ?: 100

                    if (isSingleMiss && daysSinceGrace >= 7) {
                        lastGraceDate = date
                        CalendarCellState.GRACE
                    } else {
                        CalendarCellState.MISS
                    }
                }
            }
            list.add(MonthDayCell(date = date, state = state, dayNumber = d))
        }
        list
    }

    // Month statistics
    val completedCountInMonth = (1..daysInMonth).count { d ->
        val date = currentYearMonth.atDay(d)
        logMap[date]?.completed == true
    }
    val elapsedDaysInMonth = if (currentYearMonth.year == today.year && currentYearMonth.month == today.month) {
        today.dayOfMonth
    } else if (currentYearMonth.isBefore(YearMonth.now())) {
        daysInMonth
    } else {
        0
    }
    val completionPercent = if (elapsedDaysInMonth > 0) {
        (completedCountInMonth * 100) / elapsedDaysInMonth
    } else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with Month Navigation (< Month Year >)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.2).sp
                )

                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Stats row (Completed, Streak, Grace Days)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryContainer)
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "COMPLETED",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$completedCountInMonth / $daysInMonth days",
                            color = primaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryContainer)
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "STREAK",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$streak days 🔥",
                            color = primaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryContainer)
                        .padding(vertical = 8.dp, horizontal = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "RATE",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$completionPercent%",
                            color = primaryColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Days of Week Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayLabel ->
                    Text(
                        text = dayLabel,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Month Grid (7 Columns)
            val totalRows = (gridItems.size + 6) / 7
            val gridHeight = (totalRows * 42 + (totalRows - 1) * 6).dp

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = false
            ) {
                items(gridItems) { item ->
                    if (item.date == null) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val isToday = item.date == today

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when (item.state) {
                                        CalendarCellState.DONE -> habitColor
                                        CalendarCellState.GRACE -> MaterialTheme.colorScheme.surfaceVariant
                                        CalendarCellState.PAUSED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                        CalendarCellState.MISS -> MaterialTheme.colorScheme.surfaceVariant
                                        CalendarCellState.FUTURE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                                .border(
                                    width = when {
                                        isToday -> 2.dp
                                        item.state == CalendarCellState.GRACE -> 1.5.dp
                                        item.state == CalendarCellState.PAUSED -> 1.dp
                                        else -> 0.dp
                                    },
                                    color = when {
                                        isToday -> primaryColor
                                        item.state == CalendarCellState.GRACE -> habitColor
                                        item.state == CalendarCellState.PAUSED -> MaterialTheme.colorScheme.secondary
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = item.dayNumber.toString(),
                                    color = when (item.state) {
                                        CalendarCellState.DONE -> MaterialTheme.colorScheme.onPrimary
                                        CalendarCellState.FUTURE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday || item.state == CalendarCellState.DONE) FontWeight.ExtraBold else FontWeight.Medium
                                )

                                if (item.state == CalendarCellState.DONE) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                } else if (item.state == CalendarCellState.GRACE) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(habitColor)
                                    )
                                } else if (item.state == CalendarCellState.PAUSED) {
                                    Text(
                                        text = "⏸",
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Legend & Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(habitColor))
                        Text("Done", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .border(1.dp, habitColor, CircleShape)
                        )
                        Text("Grace", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("⏸", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
                        Text("Paused", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .border(1.dp, primaryColor, CircleShape)
                        )
                        Text("Today", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
