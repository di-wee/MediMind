package com.example.medimind

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment

class EditMedicineDetailsFragment : Fragment() {
    private lateinit var frequencyInput: EditText
    private lateinit var timeInputContainer:LinearLayout
    private lateinit var backButton: Button
    private lateinit var saveButton: Button

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

        val medicineName = arguments?.getString("medicineName") ?: "Unknown Medicine"
        view.findViewById<TextView>(R.id.medicineNameTitleEdit).text = "$medicineName"

        //default frequency I set it 3, later might retrieve from DB
        frequencyInput.setText("3")
        generateTimeFields(3)

        frequencyInput.addTextChangedListener {
            val freq = frequencyInput.text.toString().toIntOrNull() ?: 0
            generateTimeFields(freq)
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        saveButton.setOnClickListener {
            //save to DB, implement later
        }
    }

    private fun generateTimeFields(frequency:Int) {
        timeInputContainer.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        val defaultTime = listOf("0900", "1300", "1800", "2100", "2300")

        for (i in 1..frequency) {
            var timeBox = inflater.inflate(R.layout.time_input_box, timeInputContainer, false) as EditText
            timeBox.hint = defaultTime.getOrNull(i-1) ?: "HHMM"
            timeInputContainer.addView(timeBox)
        }
    }
}