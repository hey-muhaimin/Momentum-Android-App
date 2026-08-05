package com.momentum.app.data.wear

import android.content.Context

data class WearOSStatus(
    val isConnected: Boolean = true,
    val deviceName: String = "Galaxy Watch / Pixel Watch",
    val lastWristSyncTime: String = "Just now"
)

object WearOSManager {
    fun getWearStatus(context: Context): WearOSStatus {
        return WearOSStatus(
            isConnected = true,
            deviceName = "Wear OS Smartwatch",
            lastWristSyncTime = "Synced recently"
        )
    }
}
