package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.medimind.ReminderUtils.scheduleAlarm
import com.example.medimind.data.*
import com.example.medimind.network.ApiClient
import com.example.medimind.network.IntakeMedRequest
import com.example.medimind.network.MedicationIdListRequest
import com.example.medimind.network.ScheduleListRequest
import com.example.medimind.network.newMedicationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import android.graphics.Typeface
import com.google.gson.Gson
import java.util.UUID


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
                val details = meds.map {
                    val title = "${it.medicationName}: "
                    val intake = "${it.intakeQuantity}Tables "
                    val instruction = "Instruction：${it.instructions.ifBlank { "" }}"
                    val note = "Notes:${it.note.ifBlank { "" }}"

                    buildStyledSpannable(title, intake, instruction, note)
                }.toTypedArray()
                val selected = BooleanArray(details.size)

                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@ReminderActivity)
                        .setTitle("Medication Reminder")
                        .setMultiChoiceItems(details, selected) { _, which, isChecked ->
                            selected[which] = isChecked
                        }
                        .setPositiveButton("Save") { _, _ ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                val unselectedMedIds = mutableListOf<String>()
                                for (i in meds.indices) {
                                    val med = meds[i]
                                    val schedule = medToSchedule[med.id] ?: continue
                                    if (selected[i]) {
                                        val uniqueId = UUID.randomUUID().toString()
                                        val intakeMedRequest = IntakeMedRequest(
                                            medicationId = med.id,
                                            loggedDate= localDate.toString(),
                                            isTaken= true,
                                            patientId= patientId,
                                            scheduleId= schedule.scheduleId,
                                            clientRequestId = uniqueId
                                        )
                                        Log.d(
                                            "ReminderActivity",
                                            "📤 Submitting: clientRequestId=$uniqueId, isTaken=${intakeMedRequest.isTaken}, med=${med.medicationName}"
                                        )
                                        val response = service.createMedicationLog(intakeMedRequest)
                                        if (!response.isSuccessful) {
                                            Log.e("ReminderActivity", "Failed to create med log")
                                        }
                                        clearSnoozeCount(this@ReminderActivity,schedule.scheduleId)
                                    } else {
                                        unselectedMedIds.add(med.id)
                                        val count =getSnoozeCount(this@ReminderActivity,schedule.scheduleId)
                                        Log.d("ReminderActivity", "⏰ Calling snoozeReminder() for med=${med.medicationName}, scheduleId=${schedule.scheduleId}, count=$count")
                                        if(count < 1){
                                            increaseSnoozeCount(this@ReminderActivity,schedule.scheduleId)
                                        }else{
                                            val uniqueId = UUID.randomUUID().toString()
                                            val intakeMedRequest = IntakeMedRequest(
                                                medicationId = med.id,
                                                loggedDate= localDate.toString(),
                                                isTaken= false,
                                                patientId= patientId,
                                                scheduleId= schedule.scheduleId,
                                                clientRequestId = uniqueId
                                            )
                                            Log.d(
                                                "ReminderActivity",
                                                "📤 Submitting: clientRequestId=$uniqueId, isTaken=${intakeMedRequest.isTaken}, med=${med.medicationName}"
                                            )
                                            service.createMedicationLog(intakeMedRequest)
                                            clearSnoozeCount(this@ReminderActivity, schedule.scheduleId)
                                        }
                                    }
                                }
                                if (unselectedMedIds.isNotEmpty()) {
                                    Log.d("ReminderActivity", "👉 Calling snoozeReminder for medIds=${unselectedMedIds}")
                                    ReminderUtils.snoozeReminder(
                                        this@ReminderActivity,
                                        unselectedMedIds,
                                        patientId,
                                        timeMillis
                                    )
                                }
                                finish()
                            }
                        }
                        .setNegativeButton("Snooze All") { _, _ ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                val unselectedMedIds = mutableListOf<String>()
                                meds.forEach { med->
                                    val schedule = medToSchedule[med.id] ?: return@forEach
                                    val count = getSnoozeCount(this@ReminderActivity, schedule.scheduleId)
                                    if(count<4){
                                        Log.d("ReminderActivity", "⏰ Calling snoozeReminder() from Snooze All for med=${med.medicationName}, scheduleId=${schedule.scheduleId}, count=$count")
                                        increaseSnoozeCount(this@ReminderActivity,schedule.scheduleId)
                                        unselectedMedIds.add(med.id)
                                    }else{
                                        val uniqueId = UUID.randomUUID().toString()
                                        val intakeMedRequest = IntakeMedRequest(
                                            medicationId = med.id,
                                            loggedDate= localDate.toString(),
                                            isTaken= false,
                                            patientId= patientId,
                                            scheduleId= schedule.scheduleId,
                                            clientRequestId = uniqueId
                                        )
                                        Log.d("ReminderActivity", ">>> Sending isTaken = ${intakeMedRequest.isTaken}, requestId = ${intakeMedRequest.clientRequestId}")
                                        Log.d("ReminderActivity", "Full JSON: ${Gson().toJson(intakeMedRequest)}")
                                        service.createMedicationLog(intakeMedRequest)
                                        Toast.makeText(this@ReminderActivity,
                                            "Too many snoozes!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                Log.d("ReminderActivity", "👉 Calling snoozeReminder for medIds=${unselectedMedIds}")
                                ReminderUtils.snoozeReminder(
                                    this@ReminderActivity,
                                    unselectedMedIds,
                                    patientId,
                                    timeMillis
                                )
                                finish()
                            }
                        }
                        .setOnCancelListener {
                            finish()
                        }
                        .show()
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
    fun increaseSnoozeCount(context: Context,scheduleId: String){
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(scheduleId, 0)
        prefs.edit().putInt(scheduleId, current + 1).apply()
    }
    fun clearSnoozeCount(context: Context,scheduleId:String){
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(scheduleId).apply()
    }
    fun getSnoozeCount(context: Context,scheduleId:String):Int{
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        return prefs.getInt(scheduleId, 0)
    }
    fun buildStyledSpannable(
        title: String,
        intake: String,
        instruction: String,
        note: String
    ): SpannableString {
        val fullText = "$title:$intake\n$instruction\n$note"
        val spannable = SpannableString(fullText)

        val titleLineEnd = title.length + 1 + intake.length
        spannable.setSpan(
            RelativeSizeSpan(1.2f),
            0,
            titleLineEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            titleLineEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            RelativeSizeSpan(0.85f),
            titleLineEnd + 1,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }

}
