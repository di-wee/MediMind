package com.example.medimind.network

import com.example.medimind.data.MedicationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Request body for registration
data class RegisterRequest(
    val email: String,
    val password: String,
    val nric: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dob: String,
    val clinicName: String // or clinicId if you choose
)

// Request body for login
data class LoginRequest(
    val email: String,
    val password: String
)

// Patient response (simplified for now)
data class PatientResponse(
    val id: String,
    val email: String,
    val nric: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dob: String
)

// Response for a single clinic
data class ClinicResponse(
    val id: String,
    val clinicName: String
)

data class ClinicListResponse(
    val _embedded: EmbeddedClinics
)

data class EmbeddedClinics(
    val clinics: List<ClinicResponse>
)

interface ApiService {

    // Existing endpoint
    @GET("api/patient/{id}/medications")
    suspend fun getPatientMedications(@Path("id") patientId: String): List<MedicationResponse>

    // Registration
    @POST("api/patient/register")
    suspend fun register(@Body request: RegisterRequest): PatientResponse

    // Login
    @POST("api/patient/login")
    suspend fun login(@Body request: LoginRequest): PatientResponse

    // Fetch patient details
    @GET("api/patient/{id}")
    suspend fun getPatient(@Path("id") patientId: String): PatientResponse

    // Fetch list of clinics
    @GET("clinics")
    suspend fun getClinics(): ClinicListResponse
}
