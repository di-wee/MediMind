package com.example.medimind

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import com.example.medimind.network.RegisterRequest
import kotlinx.coroutines.launch
import java.util.*

class RegisterUserFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val clinicSpinner = view.findViewById<Spinner>(R.id.spinnerClinic)

        // Mutable list for clinic names and a mapping for IDs
        val clinicNames = mutableListOf<String>()
        val nameToIdMap = mutableMapOf<String, String>()

        // Adapter initially empty until data is fetched
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, clinicNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        clinicSpinner.adapter = adapter

        // Fetch clinics dynamically from backend
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.retrofitService.getClinics()
                clinicNames.clear()
                nameToIdMap.clear()

                // Populate lists with data from backend
                response._embedded.clinics.forEach {
                    clinicNames.add(it.clinicName)
                    nameToIdMap[it.clinicName] = it.id
                }

                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load clinics: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

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

        // Back to login
        view.findViewById<Button>(R.id.backToLoginButton).setOnClickListener {
            findNavController().navigateUp()
        }

        // Register button action
        view.findViewById<Button>(R.id.registerButton).setOnClickListener {
            val email = view.findViewById<EditText>(R.id.inputEmail).text.toString()
            val password = view.findViewById<EditText>(R.id.inputPassword).text.toString()
            val nric = view.findViewById<EditText>(R.id.inputNRIC).text.toString()
            val firstName = view.findViewById<EditText>(R.id.inputFirstName).text.toString()
            val lastName = view.findViewById<EditText>(R.id.inputLastName).text.toString()
            val gender = view.findViewById<EditText>(R.id.inputGender).text.toString()
            val dob = dobTextView.text.toString()
            val selectedClinicName = clinicSpinner.selectedItem.toString()

            // Build request – sending clinicName (you can switch to clinicId if needed)
            val request = RegisterRequest(
                email, password, nric, firstName, lastName,
                gender, dob, selectedClinicName
            )

            lifecycleScope.launch {
                try {
                    val response = ApiClient.retrofitService.register(request)
                    Toast.makeText(
                        requireContext(),
                        "Registered: ${response.firstName} ${response.lastName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_registerUserFragment_to_loginFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
