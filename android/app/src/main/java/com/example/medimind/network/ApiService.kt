package com.example.medimind.network

import com.example.medimind.data.EditMedRequest
import com.example.medimind.data.EditMedResponse
import com.example.medimind.data.MedicationResponse
import com.example.medimind.data.IntakeHistoryResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

// Request body for updating a patient profile
// password is nullable; only updated if provided
data class UpdatePatientRequest(
    val email: String,
    val password: String?,   // nullable to allow no password change
    val nric: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dob: String
)

// Clinic info included inside a Patient response
data class ClinicInPatient(
    val clinicName: String
)

// Patient response (includes clinic details)
data class PatientResponse(
    val id: String,
    val email: String,
    val nric: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dob: String,
    val clinic: ClinicInPatient? // includes clinic info
)

// Response for a single clinic (used in spinner list)
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

data class ScheduleItem(
    val scheduledTime: String,
    val medicationName: String,
    val quantity: String,
    val isActive: Boolean
)

interface ApiService {

    // LST: Get medications for a patient
    @GET("api/patient/{id}/medList")
    suspend fun getPatientMedications(@Path("id") patientId: String): List<MedicationResponse>

    // LST:
    @GET("api/medication/{medicationId}/edit")
    suspend fun getMedicationEditDetails(@Path("medicationId") medId: String): EditMedResponse

    // LST:
    @POST("api/medication/edit/save")
    suspend fun saveEditedMedication(@Body body: EditMedRequest): ResponseBody

    // Registration endpoint
    @POST("api/patient/register")
    suspend fun register(@Body request: RegisterRequest): PatientResponse

    // Login endpoint
    @POST("api/patient/login")
    suspend fun login(@Body request: LoginRequest): PatientResponse

    // Fetch patient details (returns patient info + clinic)
    @GET("api/patient/{id}")
    suspend fun getPatient(@Path("id") patientId: String): PatientResponse

    // Update patient details (PUT)
    @PUT("api/patient/{id}")
    suspend fun updatePatient(
        @Path("id") patientId: String,
        @Body request: UpdatePatientRequest
    ): PatientResponse

    // Fetch list of clinics (for registration spinner)
    @GET("clinics")
    suspend fun getClinics(): ClinicListResponse

    // Get intake history for a patient
    @GET("api/patients/{patientId}/intake-history")
    suspend fun getIntakeHistory(@Path("patientId") patientId: String): List<IntakeHistoryResponse>

    //LST: deactivate medication
    @PUT("api/medication/{medicationId}/deactivate")
    suspend fun deactivateMedication(@Path("medicationId") medId: String): ResponseBody

    // Get daily recurring medication schedule
    @GET("api/schedule/daily/{patientId}")
    suspend fun getDailySchedule(@Path("patientId") patientId: String): List<ScheduleItem>
}
