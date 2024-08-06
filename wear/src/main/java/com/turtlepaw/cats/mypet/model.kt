package com.turtlepaw.cats.mypet

data class CatStatus(
    val hunger: Int,
    val treats: Int,
    val happiness: Int,
    val happinessReasons: Map<String, Int>
)
