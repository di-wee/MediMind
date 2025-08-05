package com.example.medimind

import android.content.Context
import android.os.Bundle
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

class ReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val timeMillis = intent.getLongExtra("time_millis", -1)
        val patientId = intent.getStringExtra("patient_id")
        if (timeMillis == -1L) {
            finish()
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            if (patientId == null) {
                Toast.makeText(this@ReminderActivity, "Patient ID is missing", Toast.LENGTH_SHORT).show()
                return@launch
            }
            try {
                val time = convertMillisToLocalTimeString(timeMillis)
                val service = ApiClient.retrofitService

                val scheduleListRequest = ScheduleListRequest(time,patientId)
                val schedules = service.getSchedulesByTime(scheduleListRequest)
                if (schedules.isEmpty()) {
                    finish()
                    return@launch
                }
                val medIdList = schedules.map { it.medicationId }.distinct()
                val medToSchedule = schedules.associateBy { it.medicationId }
                val medicationIdListRequest = MedicationIdListRequest(medIdList)
                val meds = service.getMedications(medicationIdListRequest)
                val details = meds.map {
                    "${it.medicationName}（${it.intakeQuantity}）\n" +
                            "Instruction：${it.instructions.ifBlank { "" }}"
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
                                for (i in meds.indices) {
                                    val med = meds[i]
                                    val schedule = medToSchedule[med.id] ?: continue
                                    if (selected[i]) {
                                        val intakeMedRequest = IntakeMedRequest(
                                          loggedDate= LocalTime.now(),
                                            isTaken= true,
                                            patientId= patientId,
                                            scheduleId= schedule.id
                                        )
                                        service.createMedicationLog(intakeMedRequest)
                                        clearSnoozeCount(this@ReminderActivity,schedule.id)
                                    } else {
                                        val count =getSnoozeCount(this@ReminderActivity,schedule.id)
                                        if(count < 1){
                                            increaseSnoozeCount(this@ReminderActivity,schedule.id)
                                            ReminderUtils.snoozeReminder(
                                                this@ReminderActivity,
                                                meds[i].id,
                                                patientId
                                            )
                                        }else{
                                            val intakeMedRequest = IntakeMedRequest(
                                                loggedDate= LocalTime.now(),
                                                isTaken= false,
                                                patientId= patientId,
                                                scheduleId= schedule.id
                                            )
                                            service.createMedicationLog(intakeMedRequest)
                                        }
                                        clearSnoozeCount(this@ReminderActivity, schedule.id)
                                    }
                                }
                                finish()
                            }
                        }
                        .setNegativeButton("Snooze All") { _, _ ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                meds.forEach { med->
                                    val schedule = medToSchedule[med.id] ?: return@forEach
                                    val count = getSnoozeCount(this@ReminderActivity, schedule.id)
                                    if(count<4){
                                        increaseSnoozeCount(this@ReminderActivity,schedule.id)
                                        ReminderUtils.snoozeReminder(this@ReminderActivity, med.id, patientId)
                                    }else{
                                        val intakeMedRequest = IntakeMedRequest(
                                            loggedDate= LocalTime.now(),
                                            isTaken= false,
                                            patientId= patientId,
                                            scheduleId= schedule.id
                                        )
                                        service.createMedicationLog(intakeMedRequest)
                                        Toast.makeText(this@ReminderActivity,
                                            "Too many snoozes!", Toast.LENGTH_SHORT).show()
                                    }
                                }
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

}
