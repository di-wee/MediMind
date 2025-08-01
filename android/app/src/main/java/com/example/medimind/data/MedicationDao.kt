package com.example.medimind.data

import androidx.room.*

@Dao
interface MedicationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(med: Medication): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicationLog)

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getById(id: Int): Medication?

    @Query("SELECT * FROM medications")
    fun getAllMedications(): List<Medication>

    @Query("SELECT * FROM medication_logs")
    fun getAllLogs(): List<MedicationLog>

    @Query("SELECT * FROM medication_logs WHERE medId = :medId")
    fun getLogsForMedication(medId: Int): List<MedicationLog>

    @Query("DELETE FROM medications WHERE id = :medId")
    fun deleteMedication(medId: Int)

    @Query("SELECT * FROM medications WHERE date = :date")
    fun getMedicationsForDate(date: String): List<Medication>

}
