package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.text.InputType
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

class EditMedicineDetailsFragment : Fragment() {
    private lateinit var frequencyInput: EditText
    private lateinit var timeInputContainer:LinearLayout
    private lateinit var backButton: Button
    private lateinit var saveButton: Button
    private lateinit var medicineName: String
    private lateinit var medicineId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_medicine_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frequencyInput = view.findViewById(R.id.frequencyInput)
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
                generateTimeFields(details.frequency, details.activeSchedulesTimes)
            } catch (e: Exception) {
                Toast.makeText(context, "Frequency loading failed", Toast.LENGTH_SHORT).show()
                frequencyInput.setText("3")
                generateTimeFields(3)
            }
        }

        frequencyInput.addTextChangedListener {
            val freq = frequencyInput.text.toString().toIntOrNull() ?: 0
            generateTimeFields(freq)
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
//        val defaultTime = listOf("0900", "1300", "1800", "2100", "2300")

        for (i in 1..frequency) {
            var timeBox = inflater.inflate(R.layout.time_input_box, timeInputContainer, false) as EditText
            timeBox.hint = "HHMM"
            timeBox.inputType = InputType.TYPE_CLASS_NUMBER
            timeBox.setText(presetTimes.getOrNull(i - 1) ?: "")
            timeInputContainer.addView(timeBox)
        }
    }

    private fun saveMedicineDetails() {
        val freq = frequencyInput.text.toString().toIntOrNull()
        if (freq == null || freq <= 0) {
            Toast.makeText(context, "Please enter valid frequency", Toast.LENGTH_SHORT).show()
            return
        }

        val times = mutableListOf<String>()
        for (i in 0 until timeInputContainer.childCount) {
            val editText = timeInputContainer.getChildAt(i) as EditText
            val time = editText.text.toString().trim()
            if (!time.matches(Regex("\\d{4}"))) {
                Toast.makeText(context, "Time format should be 4 digits, like 0900", Toast.LENGTH_SHORT).show()
                return
            }
            times.add(time)
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

        lifecycleScope.launch {
            try {
                ApiClient.retrofitService.saveEditedMedication(request)
                Toast.makeText(context,"Successfully save!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(context, "Save Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}