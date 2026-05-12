package com.example.nammamistri

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Worker(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val wage: Double,
    val advance: Double,
    val projectId: Int // 🔥 NEW (connects worker to project)
)