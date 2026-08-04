package com.momentum.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momentum.app.data.model.Habit
import com.momentum.app.data.model.HabitImpact
import com.momentum.app.data.model.HabitLog
import com.momentum.app.data.model.MoodLog
import com.momentum.app.ui.components.IconHelper
import com.momentum.app.ui.theme.CellBg
import com.momentum.app.ui.theme.Coral
import com.momentum.app.ui.theme.GreenLight
import com.momentum.app.ui.theme.GreenPrimary
import com.momentum.app.ui.theme.GreenSecondary
import com.momentum.app.ui.theme.TealAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun InsightsScreen(
    habitImpacts: List<HabitImpact>,
    moodLogs: List<MoodLog>,
    activeHabits: List<Habit> = emptyList(),
    recentLogs: List<HabitLog> = emptyList(),
    onEditHabit: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysWithBothCount = habitImpacts.maxOfOrNull { it.daysWithBoth } ?: 0
    val moodDaysCount = moodLogs.size
    val hasEnoughData = daysWithBothCount >= 5
    val isMoodOnlyUser = moodDaysCount >= 5 && daysWithBothCount == 0

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
                text = "WHAT MOVES THE NEEDLE",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Signal / Gathering Signal State
        if (hasEnoughData) {
            val maxAbsScore = habitImpacts.maxOfOrNull { abs(it.correlationScore) }?.coerceAtLeast(0.5) ?: 1.0

            items(habitImpacts, key = { it.habit.id }) { impact ->
                ImpactItemCard(
                    impact = impact,
                    maxAbsScore = maxAbsScore,
                    onClick = { onEditHabit(impact.habit) }
                )
            }
        } else {
            // Empty / Gathering Signal Card (.p-card.p-insight)
            item {
                InsightsEmptyStateCard(
                    daysWithBothCount = daysWithBothCount,
                    isMoodOnlyUser = isMoodOnlyUser
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MOOD & ENERGY TRENDS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Interactive Trend & Rating Data Visualization Chart
        item {
            TrendVisualizationCard(
                moodLogs = moodLogs,
                activeHabits = activeHabits,
                recentLogs = recentLogs
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

enum class TimeFrame(val days: Int, val label: String) {
    DAYS_7(7, "7D"),
    DAYS_14(14, "14D"),
    DAYS_30(30, "30D"),
    DAYS_90(90, "90D")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendVisualizationCard(
    moodLogs: List<MoodLog>,
    activeHabits: List<Habit>,
    recentLogs: List<HabitLog>
) {
    var selectedTimeFrame by remember { mutableStateOf(TimeFrame.DAYS_30) }
    var selectedDayIndex by remember { mutableIntStateOf(-1) }
    var showMoodLine by remember { mutableStateOf(true) }
    var showHabitsLine by remember { mutableStateOf(true) }

    val today = LocalDate.now()
    val timeframeDays = selectedTimeFrame.days
    val dateList = (0 until timeframeDays).map { today.minusDays((timeframeDays - 1 - it).toLong()) }

    val moodMap = moodLogs.associateBy { it.date }
    val logsByDate = recentLogs.groupBy { it.date }

    val activeMoodScores = dateList.mapNotNull { moodMap[it]?.score }
    val avgMoodScore = if (activeMoodScores.isNotEmpty()) activeMoodScores.average() else 0.0

    val habitCompletionRatios = dateList.map { date ->
        val completedCount = logsByDate[date]?.count { it.completed } ?: 0
        val totalActive = activeHabits.size.coerceAtLeast(1)
        (completedCount.toDouble() / totalActive).coerceIn(0.0, 1.0)
    }
    val avgHabitRatio = if (habitCompletionRatios.isNotEmpty()) habitCompletionRatios.average() else 0.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mood & Energy Ratings",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rating trends over time",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(2.dp)
                ) {
                    TimeFrame.values().forEach { tf ->
                        val isSelected = selectedTimeFrame == tf
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable {
                                    selectedTimeFrame = tf
                                    selectedDayIndex = -1
                                }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tf.label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Summary Badges Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text("AVG MOOD", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (avgMoodScore > 0) String.format("%.1f", avgMoodScore) else "—",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(" / 5", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(getMoodEmoji(avgMoodScore.toInt()), fontSize = 13.sp)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenLight)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text("HABIT ENERGY", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${(avgHabitRatio * 100).toInt()}%",
                                color = GreenSecondary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("⚡", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Tooltip Info Box
            AnimatedVisibility(
                visible = selectedDayIndex in dateList.indices,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150))
            ) {
                if (selectedDayIndex in dateList.indices) {
                    val selDate = dateList[selectedDayIndex]
                    val selMood = moodMap[selDate]
                    val selCompleted = logsByDate[selDate]?.count { it.completed } ?: 0
                    val totalActive = activeHabits.size.coerceAtLeast(1)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GreenLight)
                            .border(1.dp, GreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = if (selMood != null) "Mood: ${getMoodEmoji(selMood.score)} ${selMood.score}/5" else "Mood: Not logged",
                                        color = GreenPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Habits: $selCompleted/$totalActive (${(selCompleted * 100 / totalActive)}%)",
                                        color = GreenSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { selectedDayIndex = -1 }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // High Precision Interactive Canvas Line Chart
            val chartPointsCount = dateList.size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(selectedTimeFrame, dateList) {
                            detectTapGestures { offset ->
                                val W = size.width.toFloat()
                                val padL = 20f
                                val padR = 20f
                                val stepX = (W - padL - padR) / (chartPointsCount - 1).coerceAtLeast(1)

                                val clickedIdx = ((offset.x - padL + stepX / 2f) / stepX).toInt()
                                if (clickedIdx in dateList.indices) {
                                    selectedDayIndex = clickedIdx
                                }
                            }
                        }
                ) {
                    val W = size.width
                    val H = size.height
                    val padL = 20f
                    val padR = 16f
                    val padT = 12f
                    val padB = 24f

                    val stepX = (W - padL - padR) / (chartPointsCount - 1).coerceAtLeast(1)
                    val yTop = padT
                    val yBot = H - padB
                    val chartH = yBot - yTop

                    for (g in 0..4) {
                        val y = yTop + (chartH * g / 4f)
                        drawLine(
                            color = CellBg,
                            start = Offset(padL, y),
                            end = Offset(W - padR, y),
                            strokeWidth = 1.2f
                        )
                    }

                    val moodPoints = dateList.map { date -> moodMap[date]?.score }
                    val habitPoints = dateList.map { date ->
                        val completedCount = logsByDate[date]?.count { it.completed } ?: 0
                        val totalActive = activeHabits.size.coerceAtLeast(1)
                        1.0 + (completedCount.toDouble() / totalActive) * 4.0
                    }

                    if (showHabitsLine && habitPoints.isNotEmpty()) {
                        val habitPath = Path()
                        val fillPath = Path()

                        habitPoints.forEachIndexed { i, score ->
                            val x = padL + i * stepX
                            val y = yBot - ((score - 1) / 4.0).toFloat() * chartH
                            if (i == 0) {
                                habitPath.moveTo(x, y)
                                fillPath.moveTo(x, yBot)
                                fillPath.lineTo(x, y)
                            } else {
                                habitPath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                        }
                        val lastX = padL + (habitPoints.size - 1) * stepX
                        fillPath.lineTo(lastX, yBot)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(TealAccent.copy(alpha = 0.25f), TealAccent.copy(alpha = 0.0f)),
                                startY = yTop,
                                endY = yBot
                            )
                        )

                        drawPath(
                            path = habitPath,
                            color = TealAccent,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )
                    }

                    if (showMoodLine && moodPoints.isNotEmpty()) {
                        val moodPath = Path()
                        val fillPath = Path()

                        var started = false
                        moodPoints.forEachIndexed { i, scoreOpt ->
                            val score = scoreOpt ?: 3
                            val x = padL + i * stepX
                            val y = yBot - ((score - 1) / 4.0).toFloat() * chartH

                            if (!started) {
                                moodPath.moveTo(x, y)
                                fillPath.moveTo(x, yBot)
                                fillPath.lineTo(x, y)
                                started = true
                            } else {
                                moodPath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                        }

                        val lastX = padL + (moodPoints.size - 1) * stepX
                        fillPath.lineTo(lastX, yBot)
                        fillPath.close()

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(GreenPrimary.copy(alpha = 0.25f), GreenPrimary.copy(alpha = 0.0f)),
                                startY = yTop,
                                endY = yBot
                            )
                        )

                        drawPath(
                            path = moodPath,
                            color = GreenPrimary,
                            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                        )

                        moodPoints.forEachIndexed { i, scoreOpt ->
                            val x = padL + i * stepX
                            val score = scoreOpt ?: 3
                            val y = yBot - ((score - 1) / 4.0).toFloat() * chartH

                            if (scoreOpt != null) {
                                drawCircle(color = Color.White, radius = 4.5f, center = Offset(x, y))
                                drawCircle(color = GreenPrimary, radius = 3f, center = Offset(x, y))
                            }
                        }
                    }

                    if (selectedDayIndex in dateList.indices) {
                        val selX = padL + selectedDayIndex * stepX
                        drawLine(
                            color = GreenPrimary.copy(alpha = 0.5f),
                            start = Offset(selX, yTop),
                            end = Offset(selX, yBot),
                            strokeWidth = 1.5f
                        )

                        val moodVal = moodPoints[selectedDayIndex] ?: 3
                        val selY = yBot - ((moodVal - 1) / 4.0).toFloat() * chartH
                        drawCircle(color = Color.White, radius = 7f, center = Offset(selX, selY))
                        drawCircle(color = GreenPrimary, radius = 4.5f, center = Offset(selX, selY))
                    }
                }
            }

            // Legend Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showMoodLine) GreenLight else CellBg)
                        .clickable { showMoodLine = !showMoodLine }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(if (showMoodLine) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Mood / Energy Score",
                        color = if (showMoodLine) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (showHabitsLine) GreenLight else CellBg)
                        .clickable { showHabitsLine = !showHabitsLine }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(if (showHabitsLine) TealAccent else MaterialTheme.colorScheme.onSurfaceVariant))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Habit Completion Rate",
                        color = if (showHabitsLine) GreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

fun getMoodEmoji(score: Int): String {
    return when (score) {
        1 -> "😫"
        2 -> "🙁"
        3 -> "😐"
        4 -> "😊"
        5 -> "⚡"
        else -> "✨"
    }
}

@Composable
fun InsightsEmptyStateCard(
    daysWithBothCount: Int,
    isMoodOnlyUser: Boolean
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val emptyCellColor = if (isDark) com.momentum.app.ui.theme.CellDarkTheme else CellBg
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sparkline graphic vector (.p-card.p-insight)
            Canvas(
                modifier = Modifier
                    .width(100.dp)
                    .height(28.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.7f)
                    lineTo(size.width * 0.22f, size.height * 0.15f)
                    lineTo(size.width * 0.42f, size.height * 0.75f)
                    lineTo(size.width * 0.7f, size.height * 0.75f)
                    lineTo(size.width * 0.82f, size.height * 0.35f)
                    lineTo(size.width, size.height * 0.6f)
                }
                drawPath(
                    path = path,
                    color = secondaryColor,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Still gathering signal",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isMoodOnlyUser) {
                    "Log at least one habit to see what habits impact your energy ratings."
                } else {
                    "Log your mood and habits for a few more days — patterns need at least 5 days to mean anything."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Progress count line & 5 dashes indicator (.p-dashes)
            val progress = daysWithBothCount.coerceIn(0, 5)
            Text(
                text = "$progress of 5 days logged",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0 until 5).forEach { index ->
                    val isFilled = index < progress
                    Box(
                        modifier = Modifier
                            .width(26.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isFilled) primaryColor else emptyCellColor)
                    )
                }
            }
        }
    }
}

@Composable
fun ImpactItemCard(
    impact: HabitImpact,
    maxAbsScore: Double,
    onClick: () -> Unit
) {
    val isPositive = impact.correlationScore >= 0
    val fillFraction = (abs(impact.correlationScore) / maxAbsScore).coerceIn(0.1, 1.0).toFloat()

    val animatedFill by animateFloatAsState(
        targetValue = fillFraction,
        animationSpec = tween(durationMillis = 800),
        label = "impact_fill"
    )

    val parseColor = runCatching {
        Color(android.graphics.Color.parseColor(impact.habit.colorHex))
    }.getOrDefault(GreenPrimary)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(parseColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = IconHelper.getIcon(impact.habit.iconKey),
                            contentDescription = null,
                            tint = parseColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = impact.habit.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", impact.correlationScore)}",
                    color = if (isPositive) GreenPrimary else Coral,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(CellBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFill)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPositive) GreenPrimary else Coral
                        )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isPositive) {
                    "On days you hit this, your energy averages higher than days you skip it."
                } else {
                    "On days you hit this, your energy averages lower than days you skip it."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

