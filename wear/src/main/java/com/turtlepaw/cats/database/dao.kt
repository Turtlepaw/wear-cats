package com.turtlepaw.cats.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ImageDao {
    @Insert
    suspend fun insertImage(measurement: Image)

    @Insert
    suspend fun insertImages(images: List<Image>)

    @Query("SELECT * FROM images ORDER BY RANDOM()")
    suspend fun getImages(): List<Image>

    @Query("DELETE FROM images")
    suspend fun deleteAllImages()

    suspend fun replaceImages(newImages: List<Image>) {
        deleteAllImages()
        insertImages(newImages)
    }
}

@Dao
interface FavoritesDao {
    @Insert
    suspend fun insertFavorite(favorite: Favorite)

    @Query("SELECT * FROM favorite ORDER BY timestamp DESC")
    suspend fun getFavorites(): List<Favorite>

    @Query("DELETE FROM favorite WHERE id = :favoriteId")
    suspend fun deleteFavoriteById(favoriteId: Int)
}

