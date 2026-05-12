package com.example.nammamistri

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Rates(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val projectId: Int,
    val brickRate: Double,
    val cementRate: Double,
    val sandRate: Double
)