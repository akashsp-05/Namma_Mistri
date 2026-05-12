package com.example.nammamistri

import androidx.room.*

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM Expense WHERE projectId = :projectId")
    suspend fun getExpenses(projectId: Int): List<Expense>

    @Query("DELETE FROM Expense WHERE id = :id")
    suspend fun deleteExpense(id: Int)
    @Query("DELETE FROM Expense WHERE projectId = :projectId")
    suspend fun deleteExpensesByProject(projectId: Int)
}