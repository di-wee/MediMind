package com.example.medimind

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.medimind.ReminderUtils.scheduleAlarm
import com.example.medimind.network.ApiClient
import com.example.medimind.network.newMedicationRequest
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import androidx.fragment.app.viewModels
import com.example.medimind.viewmodel.MedicationViewModel
import com.example.medimind.viewmodel.SaveMedResult
import java.util.Locale

class NewMedManualFragment : Fragment() {

    // ViewModel instance (uses ApiClient.retrofitService by default as provided)
    private val medicationViewModel: MedicationViewModel by viewModels()

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

        // Back arrow
        view.findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            findNavController().navigateUp()
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
            val dosageNum = dosage.toDoubleOrNull() ?:0.0
            //this make sure when insert to DB is like xx tablets
            val dosageDisplay = when {
                dosageNum == 1.0 -> "1 tablet"
                dosageNum == 0.5 -> "0.5 tablet"
                dosageNum % 1 == 0.0 -> "${dosageNum.toInt()} tablets"
                else -> "$dosage tablets"
            }
            if (patientId == null || medicationName.isBlank() || dosage.isBlank() || frequency == 0) {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all fields correctly",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // call ViewModel to save
            lifecycleScope.launch {
                try {
                    val result = medicationViewModel.saveMedication(
                        medicationName = medicationName,
                        patientId = patientId,
                        dosage = dosageDisplay,
                        frequency = frequency,
                        instructions = instruction,
                        notes = note,
                        times = times // ViewModel accepts "HH:mm" or "HHmm" and normalizes
                    )

                    when (result) {
                        is SaveMedResult.Success -> {
                            //convert generated times to timeMillis List
                            for (time in times) {
                                val timeMilli = convertToScheduleList(time)
                                //set alarm in alarmManager
                                //set new alarm
                                scheduleAlarm(requireContext(), timeMilli, patientId)
                            }
                            Toast.makeText(requireContext(), result.message.ifBlank { "Medication saved" }, Toast.LENGTH_SHORT)
                                .show()
                            requestNotificationPermissionIfNeeded()
                            parentFragmentManager.popBackStack()
                        }
                        is SaveMedResult.Duplicate -> {
                            // 409 duplicate from backend
                            // Show inline error near name (TextView supports setError)
                            medicationNameInput.error = "An active medication with this name already exists."
                            Toast.makeText(
                                requireContext(),
                                if (result.message.isNotBlank()) result.message else "Duplicate active medication name.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is SaveMedResult.NotFound -> {
                            Toast.makeText(
                                requireContext(),
                                if (result.message.isNotBlank()) result.message else "Patient not found. Please re-login.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is SaveMedResult.Error -> {
                            Toast.makeText(
                                requireContext(),
                                result.message.ifBlank { "Failed to save medication." },
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
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
                times.add("09:00")
            } else {
                val totalMinutes = 12 * 60
                val freqGap = totalMinutes / (frequency - 1)

                for (i in 0 until frequency) {
                    val timeInMinutes = 540 + i * freqGap
                    val hour = timeInMinutes / 60
                    val minute = timeInMinutes % 60
                    val timeStr = String.format(Locale.UK,"%02d:%02d", hour, minute)  // HH:MM format
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

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val prefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val asked = prefs.getBoolean("asked_notification_permission", false)
            if (!asked) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
                prefs.edit {
                    putBoolean("asked_notification_permission", true)
                }
            }
        }
    }
}
