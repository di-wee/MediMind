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
        val medIdListFromIntent =
            intent.getStringExtra("med_id_list")?.split(",")?.map { it.trim() }?.toSet()

        if (timeMillis == -1L) {
            finish()
            return
        }

        val localDate = Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        lifecycleScope.launch(Dispatchers.IO) {
            if (patientId == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderActivity, "Patient ID is missing", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@launch
            }

            try {
                val time = convertMillisToLocalTimeString(timeMillis)
                Log.d(
                    "ReminderActivity",
                    "🔔 Triggered at millis: $timeMillis → ${convertMillisToLocalTimeString(timeMillis)}"
                )

                val service = ApiClient.retrofitService
                val scheduleListRequest = ScheduleListRequest(time, patientId)
                val response = service.getSchedulesByTime(scheduleListRequest)

                if (!response.isSuccessful || response.body().isNullOrEmpty()) {
                    withContext(Dispatchers.Main) { finish() }
                    return@launch
                }

                val allSchedules = response.body()!!
                Log.d("ReminderActivity", "Fetched ${allSchedules.size} schedules")

                val medToScheduleMap = allSchedules.associateBy { it.medicineId }

                // use medlist to filter
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
                    Log.e("ReminderActivity", "No meds matched. Nothing to show.")
                    withContext(Dispatchers.Main) { finish() }
                    return@launch
                }
                Log.d("ReminderActivity", "loaded meds: ${meds.size}")

                val medToSchedule = filteredSchedules.associateBy { it.medicineId }

                Log.d("ReminderActivity", "fetched ${meds.size} medications: $meds")

                val details = meds.map {
                    val title = "${it.medicationName}:  "
                    val intake = "${it.intakeQuantity} "
                    val instruction = "Instructions: ${it.instructions.ifBlank { "" }}"
                    val note = "Notes: ${it.note.ifBlank { "" }}"
                    buildStyledSpannable(title, intake, instruction, note)
                }.toTypedArray()

                withContext(Dispatchers.Main) {
                    // build custom material dialog content using recyclerview
                    val dialogView = LayoutInflater.from(this@ReminderActivity)
                        .inflate(R.layout.dialog_med_reminder, null)

                    val headerCard = dialogView.findViewById<MaterialCardView?>(R.id.headerCard)
                    val timeText = dialogView.findViewById<TextView?>(R.id.timeText)
                    val recycler = dialogView.findViewById<RecyclerView>(R.id.recyclerMedList)

                    timeText?.text = formatTimeForDisplay(time)
                    headerCard?.setCardBackgroundColor(
                        ContextCompat.getColor(this@ReminderActivity, android.R.color.holo_blue_light)
                    )

                    var selectedCount = 0

                    recycler.layoutManager = LinearLayoutManager(this@ReminderActivity)
                    val adapter = MedReminderDialogAdapter(details.toList()) { _, isChecked ->
                        selectedCount += if (isChecked) 1 else -1
                    }
                    recycler.adapter = adapter

                    val dialog = MaterialAlertDialogBuilder(this@ReminderActivity)
                        .setTitle(if (timeText == null) "💊 Medication Reminder" else null)
                        .setMessage(if (timeText == null) "Scheduled at ${formatTimeForDisplay(time)}" else null)
                        .setView(dialogView)
                        .setPositiveButton("Snooze All") { _, _ ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                val unselectedMedIds = mutableListOf<String>()
                                val checkedStates = adapter.getCheckedStates()

                                if (selectedCount == 0) {
                                    // If no selections, Snooze All
                                    meds.forEach { med ->
                                        val schedule = medToSchedule[med.id] ?: return@forEach
                                        val count = getSnoozeCount(this@ReminderActivity, schedule.scheduleId)
                                        if (count < 4) {
                                            Log.d(
                                                "ReminderActivity",
                                                "Snooze All: med=${med.medicationName}, scheduleId=${schedule.scheduleId}, count=$count"
                                            )
                                            increaseSnoozeCount(this@ReminderActivity, schedule.scheduleId)
                                            unselectedMedIds.add(med.id)
                                        } else {
                                            val uniqueId = UUID.randomUUID().toString()
                                            val intakeMedRequest = IntakeMedRequest(
                                                medicationId = med.id,
                                                loggedDate = localDate.toString(),
                                                isTaken = false,
                                                patientId = patientId,
                                                scheduleId = schedule.scheduleId,
                                                clientRequestId = uniqueId
                                            )
                                            Log.d(
                                                "ReminderActivity",
                                                "sending isTaken = ${intakeMedRequest.isTaken}, requestId = ${intakeMedRequest.clientRequestId}"
                                            )
                                            Log.d("ReminderActivity", "full json: ${Gson().toJson(intakeMedRequest)}")
                                            service.createMedicationLog(intakeMedRequest)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@ReminderActivity,
                                                    "Too many snoozes",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else {
                                    // Mark selected as taken, snooze unselected
                                    for (i in meds.indices) {
                                        val med = meds[i]
                                        val schedule = medToSchedule[med.id] ?: continue
                                        val isChecked = checkedStates.getOrNull(i) ?: false
                                        if (isChecked) {
                                            val uniqueId = UUID.randomUUID().toString()
                                            val intakeMedRequest = IntakeMedRequest(
                                                medicationId = med.id,
                                                loggedDate = localDate.toString(),
                                                isTaken = true,
                                                patientId = patientId,
                                                scheduleId = schedule.scheduleId,
                                                clientRequestId = uniqueId
                                            )
                                            Log.d(
                                                "ReminderActivity",
                                                "submitting: clientRequestId=$uniqueId, isTaken=${intakeMedRequest.isTaken}, med=${med.medicationName}"
                                            )
                                            val createResp = service.createMedicationLog(intakeMedRequest)
                                            if (!createResp.isSuccessful) {
                                                Log.e("ReminderActivity", "failed to create med log")
                                            }
                                            clearSnoozeCount(this@ReminderActivity, schedule.scheduleId)
                                        } else {
                                            unselectedMedIds.add(med.id)
                                            val count = getSnoozeCount(this@ReminderActivity, schedule.scheduleId)
                                            Log.d(
                                                "ReminderActivity",
                                                "snooze for med=${med.medicationName}, scheduleId=${schedule.scheduleId}, count=$count"
                                            )
                                            if (count < 1) {
                                                increaseSnoozeCount(this@ReminderActivity, schedule.scheduleId)
                                            } else {
                                                val uniqueId = UUID.randomUUID().toString()
                                                val intakeMedRequest = IntakeMedRequest(
                                                    medicationId = med.id,
                                                    loggedDate = localDate.toString(),
                                                    isTaken = false,
                                                    patientId = patientId,
                                                    scheduleId = schedule.scheduleId,
                                                    clientRequestId = uniqueId
                                                )
                                                Log.d(
                                                    "ReminderActivity",
                                                    "submitting: clientRequestId=$uniqueId, isTaken=${intakeMedRequest.isTaken}, med=${med.medicationName}"
                                                )
                                                service.createMedicationLog(intakeMedRequest)
                                                clearSnoozeCount(this@ReminderActivity, schedule.scheduleId)
                                            }
                                        }
                                    }
                                }

                                if (unselectedMedIds.isNotEmpty()) {
                                    Log.d("ReminderActivity", "calling snoozeReminder for medIds=$unselectedMedIds")
                                    ReminderUtils.snoozeReminder(
                                        this@ReminderActivity,
                                        unselectedMedIds,
                                        patientId,
                                        timeMillis
                                    )
                                }
                                withContext(Dispatchers.Main) { finish() }
                            }
                        }
                        .setOnCancelListener { finish() }
                        .create()

                    dialog.setOnShowListener {
                        val positiveButton =
                            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)

                        fun updatePositiveButton() {
                            positiveButton.text = when {
                                selectedCount == 0 -> "Snooze All"
                                selectedCount == 1 -> "Mark 1 Medication as Taken"
                                else -> "Mark $selectedCount Medications as Taken"
                            }
                        }

                        updatePositiveButton()

                        recycler.adapter = MedReminderDialogAdapter(details.toList()) { _, isChecked ->
                            selectedCount += if (isChecked) 1 else -1
                            updatePositiveButton()
                        }
                    }

                    dialog.show()
                }
            } catch (e: Exception) {
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

    fun formatTimeForDisplay(timeString: String): String {
        return try {
            val parts = timeString.split(":")
            if (parts.size >= 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].padStart(2, '0')
                val period = if (hour < 12) "AM" else "PM"
                val displayHour = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                "$displayHour:$minute $period"
            } else {
                timeString
            }
        } catch (_: Exception) {
            timeString
        }
    }

    fun convertMillisToLocalTimeString(timeMillis: Long): String {
        val localTime = Instant.ofEpochMilli(timeMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return localTime.toString()
    }

    // --- Snooze helpers (SharedPreferences) ---
    fun increaseSnoozeCount(context: Context, scheduleId: String) {
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        val current = prefs.getInt(scheduleId, 0)
        prefs.edit().putInt(scheduleId, current + 1).apply()
    }

    fun clearSnoozeCount(context: Context, scheduleId: String) {
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        prefs.edit().remove(scheduleId).apply()
    }

    fun getSnoozeCount(context: Context, scheduleId: String): Int {
        val prefs = context.getSharedPreferences("SnoozePrefs", Context.MODE_PRIVATE)
        return prefs.getInt(scheduleId, 0)
    }

    // --- Styled text for dialog items ---
    fun buildStyledSpannable(
        title: String,
        intake: String,
        instruction: String,
        note: String
    ): SpannableString {
        val fullText = "$title$intake\n$instruction\n\n$note"
        val spannable = SpannableString(fullText)

        val titleLineEnd = title.length + intake.length
        spannable.setSpan(
            RelativeSizeSpan(1.15f),
            0,
            titleLineEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            title.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            RelativeSizeSpan(0.9f),
            titleLineEnd + 1,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannable
    }
}
