package com.turtlepaw.cats.database

import androidx.room.TypeConverter
import com.turtlepaw.cats.utils.Animals
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(formatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun fromAnimals(value: Animals): String {
        return value.name
    }

    @TypeConverter
    fun toAnimals(value: String): Animals {
        return Animals.valueOf(value)
    }
}
