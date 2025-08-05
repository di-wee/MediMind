package com.example.medimind

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.medimind.ReminderUtils.scheduleAlarm
import com.example.medimind.network.ApiClient
import com.example.medimind.network.newMedicationRequest
import kotlinx.coroutines.launch
import java.util.Calendar


class NewMedManualFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_med_manual, container, false)
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backBtnFromManual = view.findViewById<Button>(R.id.btnBackFromManual)
        backBtnFromManual.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        val sharedPreferences =
            requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPreferences.getString("patientId", null)

        val medicationNameInput = view.findViewById<TextView>(R.id.medicationNameInputManual)
        val dosageInput = view.findViewById<TextView>(R.id.dosageInputManual)
        val frequencyInput = view.findViewById<TextView>(R.id.frequencyInputManual)
        val instructionInput = view.findViewById<TextView>(R.id.instructionInputManual)
        val noteInput = view.findViewById<TextView>(R.id.noteInputManual)

        val saveBtnFromManual = view.findViewById<Button>(R.id.btnSaveFromManual)
        saveBtnFromManual.setOnClickListener {
            var instruction = instructionInput.text.toString()
            var note = noteInput.text.toString()
            var frequency = frequencyInput.text.toString().toIntOrNull() ?: 0
            //generate default times
            var times = generateDefaultTimes(frequency)
            //save new medication to database and set new alarm
            val medicationName = medicationNameInput.text.toString()
            val dosage = dosageInput.text.toString()
            if (patientId == null || medicationName.isBlank() || dosage.isBlank() || frequency == 0) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all fields correctly",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    //save to db
                    val service = ApiClient.retrofitService
                    for (time in times) {
                        val request = newMedicationRequest(
                            medicationName = medicationName,
                            patientId = patientId,
                            dosage = dosage,
                            frequency = frequency,
                            instructions = instruction,
                            notes = note,
                            isActive = true,
                            time = time
                        )
                        val saveMedicationResponse = service.saveMedication(request)
                        //convert generated times to timeMillis List
                        var timeMilli = convertToScheduleList(time)
                        //set alarm in alarmManager
                        //set new alarm
                        scheduleAlarm(requireContext(), timeMilli, patientId)
                    }
                    Toast.makeText(requireContext(), "Medication saved", Toast.LENGTH_SHORT)
                        .show()
                    parentFragmentManager.popBackStack()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun generateDefaultTimes(frequency: Int): List<String> {
        val times = mutableListOf<String>()

        try {
            if (frequency <= 0) return emptyList()

            if (frequency == 1) {
                times.add("0900")
            } else {
                val totalMinutes = 12 * 60
                val freqGap = totalMinutes / (frequency - 1)

                for (i in 0 until frequency) {
                    val timeInMinutes = 540 + i * freqGap
                    val hour = timeInMinutes / 60
                    val minute = timeInMinutes % 60
                    val timeStr = String.format("%02d:%02d", hour, minute)  // HH:MM format
                    times.add(timeStr)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return times
    }
    fun convertToScheduleList(time: String): Long {

        val now = Calendar.getInstance()
        val (hour, minute) = time.split(":").map { it.toInt() }
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                    add(Calendar.DATE, 1)
            }
        }
            return cal.timeInMillis
    }

}