package com.turtlepaw.cats.mypet

enum class Moods {
    Hunger
}

class MoodManager(moods: Map<String, Int> = emptyMap()) {
    private val moodMap = moods.toMutableMap()

    fun overrideMood(mood: String, count: Int): MoodManager {
        moodMap[mood] = count
        return this
    }

    fun toMap(): Map<String, Int> {
        return moodMap.toMap()
    }

    companion object {
        fun fromMap(map: Map<String, Int>): MoodManager {
            return MoodManager(map)
        }
    }
}