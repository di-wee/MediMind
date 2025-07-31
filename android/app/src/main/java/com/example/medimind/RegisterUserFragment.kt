package com.example.medimind

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.*

class RegisterUserFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Set up the clinic dropdown (Spinner)
        val clinicSpinner = view.findViewById<Spinner>(R.id.spinnerClinic)
        val clinicList = listOf("Happy Health Clinic", "Sunshine Medical", "Evergreen Clinic")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, clinicList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        clinicSpinner.adapter = adapter

        // DatePicker for DOB
        val dobTextView = view.findViewById<TextView>(R.id.inputDOB)
        dobTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(requireContext(), { _, y, m, d ->
                val monthStr = String.format("%02d", m + 1)
                val dayStr = String.format("%02d", d)
                dobTextView.text = "$y-$monthStr-$dayStr"
            }, year, month, day)

            dpd.show()
        }

        // Back button navigation
        view.findViewById<Button>(R.id.backToLoginButton).setOnClickListener {
            findNavController().navigateUp()
        }

        // Register button (placeholder)
        view.findViewById<Button>(R.id.registerButton).setOnClickListener {
            val selectedClinic = clinicSpinner.selectedItem.toString()
            val selectedDOB = dobTextView.text.toString()
            Toast.makeText(
                requireContext(),
                "Registered with DOB: $selectedDOB\nClinic: $selectedClinic",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
