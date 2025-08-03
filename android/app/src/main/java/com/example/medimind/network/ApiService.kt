package com.example.medimind.network

import com.example.medimind.data.EditMedRequest
import com.example.medimind.data.EditMedResponse
import com.example.medimind.data.MedicationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {
    @GET("api/patient/{id}/medList")
    suspend fun getPatientMedications(@Path("id") patientId: String): List<MedicationResponse>

    @GET("api/medication/{medicationId}/edit")
    suspend fun getMedicationEditDetails(@Path("medicationId") medId: String): EditMedResponse

    @POST("api/medication/edit/save")
    suspend fun saveEditedMedication(@Body body: EditMedRequest): Void
}