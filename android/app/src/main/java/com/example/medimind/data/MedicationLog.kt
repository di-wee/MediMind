package com.example.medimind.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medication_logs")
data class MedicationLog(
    @PrimaryKey(autoGenerate = true) val logId: Int = 0,
    val medId: Int,
    val timestamp: String,
    val status: String
)
