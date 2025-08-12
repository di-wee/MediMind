package com.example.medimind

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.adapters.MedReminderDialogAdapter
import com.example.medimind.network.ApiClient
import com.example.medimind.network.IntakeMedRequest
import com.example.medimind.network.MedicationIdListRequest
import com.example.medimind.network.ScheduleListRequest
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.widget.TextView

class ReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val timeMillis = intent.getLongExtra("time_millis", -1)
        val patientId = intent.getStringExtra("patient_id")
        val medIdListFromIntent = intent.getStringExtra("med_id_list")?.split(",")?.map { it.trim() }?.toSet()
        if (timeMillis == -1L) {
            finish()
            return
        }
        val localDate = Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        lifecycleScope.launch(Dispatchers.IO) {
            if (patientId == null) {
                Toast.makeText(this@ReminderActivity, "Patient ID is missing", Toast.LENGTH_SHORT).show()
                return@launch
            }
            try {
                val time = convertMillisToLocalTimeString(timeMillis)

                Log.d("ReminderActivity", "🔔 Triggered at millis: $timeMillis → ${convertMillisToLocalTimeString(timeMillis)}")

                val service = ApiClient.retrofitService
                val scheduleListRequest = ScheduleListRequest(time,patientId)
                val response = service.getSchedulesByTime(scheduleListRequest)
                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    finish()
                    return@launch
                }
                val allSchedules = response.body()!!
                Log.d("ReminderActivity", "Fetched ${allSchedules.size} schedules")

                val medToScheduleMap = allSchedules.associateBy { it.medicineId }

                //use medlist to filter
                val finalMedIds = medIdListFromIntent ?: medToScheduleMap.keys
                Log.d("ReminderActivity", "Using medIds: $finalMedIds")
                val filteredSchedules = finalMedIds.mapNotNull { medToScheduleMap[it] }

                val medsRequest = MedicationIdListRequest(finalMedIds.toList())
                val unorderedMeds = service.getMedications(medsRequest)
                Log.d("ReminderActivity", "finalMedIds = $finalMedIds")
                Log.d("ReminderActivity", "unorderedMeds IDs = ${unorderedMeds.map { it.id }}")

                val medsMap = unorderedMeds.associateBy { it.id }

                val meds = filteredSchedules.mapNotNull { medsMap[it.medicineId] }

                if (meds.isEmpty()) {
                    Log.e("ReminderActivity", " No meds matched. Nothing to show.")
                    finish()
                    return@launch
                }
                Log.d("ReminderActivity", "💊 Loaded meds: ${meds.size}")

                val medToSchedule = filteredSchedules.associateBy { it.medicineId }
                Log.d("ReminderActivity", "Fetched ${meds.size} medications: $meds")

                withContext(Dispatchers.Main) {
                    ReminderDialogFragment(
                        meds = meds,
                        localDate = localDate,
                        patientId = patientId,
                        medToSchedule = medToSchedule,
                        timeMillis = timeMillis
                    ).show(supportFragmentManager, "ReminderBottomSheet")
                }
            }catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ReminderActivity,
                        "Something went wrong: ${e.localizedMessage ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            }
        }
    }
    fun convertMillisToLocalTimeString(timeMillis: Long): String {
        val localTime = Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return localTime.toString()
    }
}
