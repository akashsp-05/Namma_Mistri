package com.example.nammamistri

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Worker::class,
        Project::class,
        Rates::class,
        Photo::class,
        Attendance::class,
        Expense::class   // 🔥 ADD THIS
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workerDao(): WorkerDao
    abstract fun projectDao(): ProjectDao
    abstract fun ratesDao(): RatesDao

    abstract fun photoDao(): PhotoDao
    abstract fun attendanceDao(): AttendanceDao

    abstract fun expenseDao(): ExpenseDao
}
