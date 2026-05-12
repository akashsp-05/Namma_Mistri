package com.example.nammamistri

import androidx.room.*

@Dao
interface RatesDao {

    @Insert
    suspend fun insert(rates: Rates)

    @Query("SELECT * FROM Rates WHERE projectId = :projectId LIMIT 1")
    suspend fun getRates(projectId: Int): Rates?

    @Query("DELETE FROM Rates WHERE projectId = :projectId")
    suspend fun deleteRates(projectId: Int)
}