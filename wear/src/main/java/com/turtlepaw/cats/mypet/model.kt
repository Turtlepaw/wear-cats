package com.turtlepaw.cats.mypet

import java.time.LocalDateTime

data class CatStatus(
    val hunger: Int,
    val treats: Int,
    val dailyTreatsUsed: Int, // Renamed from maxTreats
    val happiness: Int,
    val happinessReasons: Map<String, Int>,
    val lastFed: LocalDateTime?,
    val lastUpdate: LocalDateTime? // New field for tracking last update
)
