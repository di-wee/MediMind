package com.example.medimind.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medimind.network.ApiClient
import com.example.medimind.network.ApiService
import com.example.medimind.network.newMedicationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Result of attempting to save a medication via /api/medication/save
 */
sealed class SaveMedResult {
    data class Success(val message: String) : SaveMedResult()
    data class Duplicate(val message: String) : SaveMedResult()   // HTTP 409
    data class NotFound(val message: String) : SaveMedResult()    // HTTP 404
    data class Error(val message: String) : SaveMedResult()       // Other errors
}

class MedicationViewModel(
    private val api: ApiService = ApiClient.retrofitService
) : ViewModel() {

    // Optional: observable one-shot result for UI that prefers observing instead of calling suspend
    private val _saveResult = MutableLiveData<SaveMedResult>()
    val saveResult: LiveData<SaveMedResult> = _saveResult

    /**
     * Suspend version — call from a coroutine (e.g., lifecycleScope.launch { ... })
     * Prefer this when you want structured concurrency in your Fragment.
     */
    suspend fun saveMedication(
        medicationName: String,
        patientId: String,
        dosage: String,
        frequency: Int,
        instructions: String?,
        notes: String?,
        times: List<String> // Accepts "HH:mm" or "HHmm"; converts to "HH:mm"
    ): SaveMedResult = withContext(Dispatchers.IO) {
        // Client-side normalization to align with backend expectations
        val cleanedName = cleanName(medicationName)
        val normalizedTimes = normalizeTimes(times)

        val req = newMedicationRequest(
            medicationName = cleanedName,
            patientId = patientId,
            dosage = dosage,
            frequency = frequency,
            instructions = instructions ?: "",
            notes = notes ?: "",
            isActive = true, // backend sets true anyway; harmless to send
            times = normalizedTimes
        )

        try {
            val body = api.saveMedication(req) // throws HttpException on non-2xx
            SaveMedResult.Success(body.string().ifBlank { "Medication saved" })
        } catch (e: HttpException) {
            when (e.code()) {
                409 -> SaveMedResult.Duplicate(e.response()?.errorBody()?.string().orEmpty())
                404 -> SaveMedResult.NotFound(e.response()?.errorBody()?.string().orEmpty())
                else -> SaveMedResult.Error(e.response()?.errorBody()?.string().orEmpty())
            }
        } catch (e: Exception) {
            SaveMedResult.Error(e.message ?: "Network or unexpected error")
        }
    }

    /**
     * Convenience non-suspend wrapper that posts to LiveData.
     * Useful if you prefer observe() pattern from your Fragment.
     */
    fun saveMedicationLive(
        medicationName: String,
        patientId: String,
        dosage: String,
        frequency: Int,
        instructions: String?,
        notes: String?,
        times: List<String>
    ) {
        viewModelScope.launch {
            val result = saveMedication(
                medicationName = medicationName,
                patientId = patientId,
                dosage = dosage,
                frequency = frequency,
                instructions = instructions,
                notes = notes,
                times = times
            )
            _saveResult.postValue(result)
        }
    }

    // --- Helpers ---

    private fun cleanName(name: String): String =
        name.trim().replace("\\s+".toRegex(), " ")

    private fun normalizeTimes(times: List<String>): List<String> =
        times.map { normalizeToHHmm(it) }

    private fun normalizeToHHmm(t: String): String {
        val s = t.trim()
        return when {
            HH_MM_REGEX.matches(s) -> s
            HHMM_REGEX.matches(s)  -> s.substring(0, 2) + ":" + s.substring(2)
            else -> throw IllegalArgumentException("Time must be HH:mm (e.g., 09:00)")
        }
    }

    companion object {
        private val HH_MM_REGEX = Regex("^\\d{2}:\\d{2}$")
        private val HHMM_REGEX  = Regex("^\\d{4}$")
    }
}
