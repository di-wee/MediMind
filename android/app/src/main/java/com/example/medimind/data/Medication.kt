package com.example.medimind.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.work.Data
import java.sql.Date

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dosage: String,
    val frequency: String,
    val scheduleTime: String,
    val date: Long,
    val isTaken: Boolean = false
)
