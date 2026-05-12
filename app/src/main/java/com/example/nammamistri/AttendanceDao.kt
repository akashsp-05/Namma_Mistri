package com.example.nammamistri

import androidx.room.*

@Dao
interface AttendanceDao {

    @Insert
    suspend fun insert(attendance: Attendance)

    @Query("SELECT * FROM Attendance WHERE projectId = :projectId AND workerId = :workerId LIMIT 1")
    suspend fun getAttendance(projectId: Int, workerId: Int): Attendance?

    @Update
    suspend fun update(attendance: Attendance)

    // 🔥 NEW FUNCTION (VERY IMPORTANT FOR PROGRESS)
    @Query("SELECT * FROM Attendance WHERE projectId = :projectId")
    suspend fun getAllAttendance(projectId: Int): List<Attendance>
}