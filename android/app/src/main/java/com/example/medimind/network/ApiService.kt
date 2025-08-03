package com.example.medimind.network

import com.example.medimind.data.MedicationResponse
import retrofit2.http.GET
import retrofit2.http.Path


interface ApiService {
    @GET("api/patient/{id}/medList")
    suspend fun getPatientMedications(@Path("id") patientId: String): List<MedicationResponse>
}