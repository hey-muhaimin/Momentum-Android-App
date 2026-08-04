package com.momentum.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PhonelinkOff
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    val iconMap = mapOf(
        "sleep" to Icons.Default.Bedtime,
        "exercise" to Icons.Default.FitnessCenter,
        "book" to Icons.Default.Book,
        "phone" to Icons.Default.PhonelinkOff,
        "water" to Icons.Default.WaterDrop,
        "meditation" to Icons.Default.SelfImprovement,
        "journal" to Icons.Default.EditNote,
        "food" to Icons.Default.Restaurant,
        "code" to Icons.Default.Code,
        "walk" to Icons.Default.DirectionsWalk
    )

    fun getIcon(key: String): ImageVector {
        return iconMap[key.lowercase()] ?: Icons.Default.Star
    }

    val availableKeys = iconMap.keys.toList()
}
