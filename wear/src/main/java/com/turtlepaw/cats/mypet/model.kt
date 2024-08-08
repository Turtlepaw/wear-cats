package com.turtlepaw.cats.mypet

import java.time.LocalDateTime

data class CatStatus(
    val hunger: Int,
    val treats: Int,
    val maxTreats: Int,
    val happiness: Int,
    val happinessReasons: Map<String, Int>,
    val lastFed: LocalDateTime?
)
