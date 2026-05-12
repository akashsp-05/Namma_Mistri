package com.example.nammamistri

import androidx.room.*

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: Photo)

    @Query("SELECT * FROM Photo WHERE projectId = :projectId")
    suspend fun getPhotos(projectId: Int): List<Photo>
    @Query("DELETE FROM Photo WHERE id = :id")
    suspend fun deletePhoto(id: Int)
    @Query("DELETE FROM Photo WHERE projectId = :projectId")
    suspend fun deletePhotosByProject(projectId: Int)
}