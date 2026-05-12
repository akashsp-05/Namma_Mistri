package com.example.nammamistri

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val createdDate: Long,
    val totalDays: Int, // 🔥 comma added here
    val completedDays: Int = 0
)