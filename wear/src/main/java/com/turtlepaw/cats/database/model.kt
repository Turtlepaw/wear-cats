package com.turtlepaw.cats.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.turtlepaw.cats.utils.Animals
import java.time.LocalDateTime

@Entity(tableName = "images")
data class Image(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** Base64 encoded image string */
    val value: String,
    val animal: Animals
)

@Entity(tableName = "favorite")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: LocalDateTime,
    /** Base64 encoded image string */
    val value: String
)
