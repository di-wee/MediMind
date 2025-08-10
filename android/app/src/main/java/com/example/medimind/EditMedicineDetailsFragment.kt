package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.medimind.data.EditMedRequest
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class EditMedicineDetailsFragment : Fragment() {
    private lateinit var frequencyInput: EditText
    private lateinit var timeInputContainer:LinearLayout
    private lateinit var backButton: Button
    private lateinit var saveButton: Button
    private lateinit var medicineName: String
    private lateinit var medicineId: String
    private var initialTimes: List<String> = emptyList()
    private var initialFrequency: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_medicine_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frequencyInput = view.findViewById(R.id.frequencyInput)
        frequencyInput.inputType = InputType.TYPE_CLASS_NUMBER

        timeInputContainer = view.findViewById(R.id.timeInputContainer)
        backButton = view.findViewById(R.id.btnBack)
        saveButton = view.findViewById(R.id.btnSave)

        medicineName = arguments?.getString("medicineName") ?: "Unknown Medicine"
        medicineId = arguments?.getString("medicineId") ?: return

        view.findViewById<TextView>(R.id.medicineNameTitleEdit).text = medicineName

        lifecycleScope.launch {
            try {
                val details = ApiClient.retrofitService.getMedicationEditDetails(medicineId)
                frequencyInput.setText(details.frequency.toString())
                initialFrequency = details.frequency
                initialTimes = details.activeSchedulesTimes.sorted()
                generateTimeFields(initialFrequency, initialTimes)
            } catch (e: Exception) {
                Toast.makeText(context, "Frequency loading failed", Toast.LENGTH_SHORT).show()
                frequencyInput.setText("3")
                generateTimeFields(3)
            }
        }

        frequencyInput.addTextChangedListener {
            val freq = frequencyInput.text.toString().toIntOrNull() ?: 0
            if (freq in 1..10) {
                generateTimeFields(freq)
            } else {
                timeInputContainer.removeAllViews()
                if (freq > 10) {
                    Toast.makeText(context, "Max frequency is 10", Toast.LENGTH_SHORT).show()
                }
            }
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        saveButton.setOnClickListener {
            saveMedicineDetails()
        }
    }

    private fun generateTimeFields(frequency:Int, presetTimes: List<String> = emptyList()) {
        timeInputContainer.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())

        val sortedTimes = presetTimes.sorted()

        for (i in 1..frequency) {
            var timeBox = inflater.inflate(R.layout.time_input_box, timeInputContainer, false) as EditText
            timeBox.hint = "HHMM"
            timeBox.inputType = InputType.TYPE_CLASS_NUMBER
            val preset = sortedTimes.getOrNull(i - 1) ?: ""
            timeBox.setText(preset)

            timeBox.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus && (v as EditText).text.toString() == preset) {
                    v.setText("")
                }
            }

            timeInputContainer.addView(timeBox)
        }
    }

    private fun saveMedicineDetails() {
        val freq = frequencyInput.text.toString().toIntOrNull()
        if (freq == null || freq <= 0) {
            Toast.makeText(context, "Please enter valid frequency", Toast.LENGTH_SHORT).show()
            return
        }
        if (freq > 10) {
            Toast.makeText(context, "Frequency too high (max 10 times per day)", Toast.LENGTH_SHORT).show()
            return
        }

        val times = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        for (i in 0 until timeInputContainer.childCount) {
            val editText = timeInputContainer.getChildAt(i) as EditText
            var time = editText.text.toString().trim()

            if (time.matches(Regex("\\d{4}"))) {
                time = time.substring(0,2) + ":" + time.substring(2,4)
            }else if (!time.matches(Regex("\\d{2}:\\d{2}"))) {
                Toast.makeText(context, "Time format should be HHMM, like0900", Toast.LENGTH_SHORT).show()
                return
            }

            val parts = time.split(":")
            val hour = parts[0].toIntOrNull()
            val minute = parts[1].toIntOrNull()
            if (hour !in 0..23 || minute !in 0..59) {
                Toast.makeText(context, "Invalid time: $time", Toast.LENGTH_SHORT).show()
                return
            }

            if (!seen.add(time)) {
                Toast.makeText(context, "Duplicate time: $time", Toast.LENGTH_SHORT).show()
                return
            }

            times.add(time)
        }

        times.sort()

        if (freq == initialFrequency && times == initialTimes) {
            Toast.makeText(context, "No changes detected", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPreferences.getString("patientId", null)
        if (patientId == null) {
            Toast.makeText(context, "Patient can not found", Toast.LENGTH_SHORT).show()
            return
        }

        val request = EditMedRequest(
            medicationId = medicineId,
            patientId = patientId,
            frequency = freq,
            times = times
        )

        Log.d("EditMedRequest", request.toString())

        lifecycleScope.launch {
            try {
                Log.d("EditMedRequest", "Before sending request at: ${System.currentTimeMillis()}")

                ApiClient.retrofitService.saveEditedMedication(request)

                Log.d("EditMedRequest", "After sending request at: ${System.currentTimeMillis()}")
                Log.d("EditMedRequest", "Request sent: $request")
                Toast.makeText(context,"Successfully save!", Toast.LENGTH_SHORT).show()
                for (timeStr in times) {
                    val parts = timeStr.split(":")
                    val hour = parts[0].toInt()
                    val minute = parts[1].toInt()
                    val localTime = LocalTime.of(hour, minute)
                    val zoned = localTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault())
                    val timeMillis = zoned.toInstant().toEpochMilli()

                    //save edited new alarm
                    ReminderUtils.scheduleAlarm(context = requireContext(), timeMilli = timeMillis, patientId = patientId)
                }
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                Log.d("EditMedError", "Failed to send request", e)
                Log.e("EditMedError", "Failed to send request: ${e.message}", e)
                Toast.makeText(context, "Save Failed!", Toast.LENGTH_SHORT).show()
            }
        }


    }

}