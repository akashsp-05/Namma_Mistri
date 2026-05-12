package com.example.nammamistri

import androidx.room.*

@Dao
interface WorkerDao {

    @Insert
    suspend fun insert(worker: Worker)

    // 🔥 Use this for project-wise data
    @Query("SELECT * FROM Worker WHERE projectId = :projectId")
    suspend fun getWorkersByProject(projectId: Int): List<Worker>

    @Query("DELETE FROM Worker WHERE id = :id")
    suspend fun deleteWorker(id: Int)

    @Update
    suspend fun updateWorker(worker: Worker)
    @Query("DELETE FROM Worker WHERE projectId = :projectId")
    suspend fun deleteWorkersByProject(projectId: Int)

}