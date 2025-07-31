package com.example.medimind.data

import androidx.room.*

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule)

    @Query("SELECT * FROM schedules WHERE id = :id")
    fun getById(id: Int): Schedule?

    @Query("SELECT * FROM schedules")
    fun getAll(): List<Schedule>

    @Query("SELECT * FROM schedules WHERE time_millis = :time")
    fun getByTimeMillis(time: Long): List<Schedule>

    @Query("DELETE FROM schedules WHERE med_id = :medId")
    fun deleteByMedId(medId: Int)

    @Delete
    fun delete(schedule: Schedule)

}