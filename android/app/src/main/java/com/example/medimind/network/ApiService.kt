package com.example.medimind.network

import com.example.medimind.data.EditMedRequest
import com.example.medimind.data.ImageOutput
import com.example.medimind.service.EditMedResponse
import com.example.medimind.service.MedicationResponse
import com.example.medimind.service.IntakeHistoryResponse
import okhttp3.Call
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import java.time.LocalDate

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

data class newMedicationRequest(
    val medicationName:String,
    val patientId: String,
    val dosage: String,
    val frequency: Int,
    val instructions:String,
    val notes:String,
    val isActive:Boolean,
    val times:List<String>
    )

data class MedicationIdListRequest(
    val medicationIds: List<String>
)
data class MedResponse(
    val id: String,
    val medicationName: String,
    val intakeQuantity: String,
    val frequency: Int,
    val timing: String,
    val instructions: String,
    val note: String,
    val isActive: Boolean
)
data class ScheduleResponse(
    val scheduleId:String,
    val scheduledTime: String,
    val isActive:Boolean,
    val medicineId: String,
)

data class IntakeMedRequest(
    val medicationId:String,
    val loggedDate: String,
    val isTaken:Boolean,
    val patientId:String,
    val scheduleId: String,
    val clientRequestId: String
)

data class IntakeResponse(
    val id: String,
    val loggedDate: String,
    val isTaken: Boolean,
    val doctorNote: String,
    val patientId:String,
    val scheduleId:String
)

data class ScheduleListRequest(
    val time:String,
    val patientId:String
)

data class SaveMedicationResponse(
    val ScheduleId: String,
    val time: String,
    val MedicationId: String,
    val MedicationName:String
)

interface ApiService {
    //Sorry guys,please do not use the response types in "data" and check the response body structure in the backend

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

    //get medication from medId list
    @POST("api/medication/medList")
    suspend fun getMedications(@Body request: MedicationIdListRequest): List<MedResponse>

    // save new medication
    @POST("api/medication/save")
    suspend fun saveMedication(@Body request: newMedicationRequest): ResponseBody

    //get active schedule list by timeMillis & patientId
    @POST("api/schedule/find")
    suspend fun getSchedulesByTime(@Body request: ScheduleListRequest): Response<List<ScheduleResponse>>

    //create intakeHistory after alarm
    @POST("api/intakeHistory/create")
    suspend fun createMedicationLog(@Body request: IntakeMedRequest): Response<Unit>

    // Get daily recurring medication schedule
    @GET("api/schedule/daily/{patientId}")
    suspend fun getDailySchedule(@Path("patientId") patientId: String): List<ScheduleItem>

    @Multipart
    @POST("/api/medication/predict_image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): ImageOutput
}
