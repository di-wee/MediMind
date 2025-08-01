package com.example.medimind.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "med_id")
    val medId: Int,

    @ColumnInfo(name = "time_millis")
    val timeMillis: Long
)
