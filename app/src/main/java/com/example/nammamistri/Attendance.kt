package com.example.nammamistri

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workerId: Int,
    val projectId: Int,
    val daysWorked: Int
)