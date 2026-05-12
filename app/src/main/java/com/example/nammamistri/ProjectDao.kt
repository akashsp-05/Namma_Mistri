package com.example.nammamistri

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update // 🔥 IMPORTANT

@Dao
interface ProjectDao {

    @Insert
    suspend fun insert(project: Project)

    @Query("SELECT * FROM Project")
    suspend fun getAllProjects(): List<Project>

    @Query("DELETE FROM Project WHERE id = :id")
    suspend fun deleteProject(id: Int)

    @Update
    suspend fun update(project: Project)
}