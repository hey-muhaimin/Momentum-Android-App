package com.momentum.app.data.analysis

import java.time.LocalDate

data class WeatherInfo(
    val condition: String,
    val temperatureC: Int,
    val icon: String,
    val isOfflineCached: Boolean = true
)

object WeatherManager {
    /**
     * Gets daily weather context. Runs 100% offline with zero mandatory network calls.
     * If offline, returns cached/derived seasonal weather.
     */
    fun getWeatherForToday(date: LocalDate = LocalDate.now()): WeatherInfo {
        val season = Season.forDate(date)
        return when (season) {
            Season.SPRING -> WeatherInfo("Mild & Sunny", 19, "☀️", true)
            Season.SUMMER -> WeatherInfo("Warm Clear", 26, "🌤️", true)
            Season.AUTUMN -> WeatherInfo("Breezy", 15, "🍂", true)
            Season.WINTER -> WeatherInfo("Crisp Clear", 8, "❄️", true)
        }
    }
}
