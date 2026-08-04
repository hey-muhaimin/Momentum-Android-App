package com.momentum.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.momentum.app.data.model.Habit
import com.momentum.app.ui.theme.Momentum

@Composable
fun HabitRow(
    habit: Habit,
    isCompleted: Boolean,
    streakCount: Int,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val tints = Momentum.Tints
    val tileBgColor = when (habit.iconKey.lowercase()) {
        "sleep" -> tints.sleep
        "exercise", "walk" -> tints.move
        "phone" -> tints.noPhone
        "water" -> tints.water
        else -> runCatching {
            Color(android.graphics.Color.parseColor(habit.colorHex))
        }.getOrDefault(primaryColor).copy(alpha = 0.18f)
    }
    val iconTintColor = when (habit.iconKey.lowercase()) {
        "sleep", "exercise", "walk", "phone", "water" -> primaryColor
        else -> runCatching {
            Color(android.graphics.Color.parseColor(habit.colorHex))
        }.getOrDefault(primaryColor)
    }

    // Fire pulse animation state
    val fireScale = remember { Animatable(1f) }
    val fireGlowAlpha = remember { Animatable(0f) }
    val checkScale = remember { Animatable(1f) }

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            fireScale.snapTo(0.6f)
            fireScale.animateTo(
                targetValue = 1.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            fireScale.animateTo(1.0f, spring(stiffness = Spring.StiffnessMedium))

            fireGlowAlpha.snapTo(0.7f)
            fireGlowAlpha.animateTo(0f, animationSpec = tween(600))

            checkScale.snapTo(0.7f)
            checkScale.animateTo(
                targetValue = 1.2f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            checkScale.animateTo(1.0f, spring(stiffness = Spring.StiffnessMedium))
        } else {
            fireScale.snapTo(1f)
            fireGlowAlpha.snapTo(0f)
            checkScale.snapTo(1f)
        }
    }

    val handleToggle = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onToggle()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = if (isCompleted) Color.Transparent else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { handleToggle() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Habit Icon Tile
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tileBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconHelper.getIcon(habit.iconKey),
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Habit Name & Streak Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = habit.name,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                if (streakCount > 0) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Flame",
                        tint = primaryColor,
                        modifier = Modifier
                            .size(12.dp)
                            .graphicsLayer {
                                scaleX = fireScale.value
                                scaleY = fireScale.value
                            }
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$streakCount day streak",
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Flame",
                        tint = primaryColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Completed today",
                        color = primaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Not logged yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // More Options Dots button
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Edit habit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Checkbox .p-check (27dp x 27dp, rounded 9dp)
        Box(
            modifier = Modifier
                .size(27.dp)
                .graphicsLayer {
                    scaleX = checkScale.value
                    scaleY = checkScale.value
                }
                .clip(RoundedCornerShape(9.dp))
                .background(if (isCompleted) primaryColor else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (isCompleted) primaryColor else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(9.dp)
                )
                .clickable { handleToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}


