package com.example.medimind

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
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
import java.util.regex.Pattern

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

        // Setup gender dropdown
        val genderSpinner = view.findViewById<Spinner>(R.id.spinnerGender)
        val genderOptions = listOf("Male", "Female")
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Back to login
        view.findViewById<Button>(R.id.backToLoginButton).setOnClickListener {
            findNavController().navigateUp()
        }

        // Fields
        val emailField = view.findViewById<EditText>(R.id.inputEmail)
        val passwordField = view.findViewById<EditText>(R.id.inputPassword)
        val confirmPasswordField = view.findViewById<EditText>(R.id.inputConfirmPassword)
        val nricField = view.findViewById<EditText>(R.id.inputNRIC)
        val firstNameField = view.findViewById<EditText>(R.id.inputFirstName)
        val lastNameField = view.findViewById<EditText>(R.id.inputLastName)

        // Register button action
        view.findViewById<Button>(R.id.registerButton).setOnClickListener {

            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()
            val nric = nricField.text.toString().trim()
            val firstName = firstNameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val gender = genderSpinner.selectedItem?.toString() ?: ""
            val dob = dobTextView.text.toString()
            val selectedClinicName = clinicSpinner.selectedItem?.toString() ?: ""

            // --------- VALIDATION START ----------
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailField.error = "Enter a valid email"
                return@setOnClickListener
            }

            val nricRegex = "^[STFG]\\d{7}[A-Z]$"
            if (!Pattern.matches(nricRegex, nric)) {
                nricField.error = "Enter a valid NRIC (e.g., S1234567A)"
                return@setOnClickListener
            }

            if (password.length < 6) {
                passwordField.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordField.error = "Passwords do not match"
                return@setOnClickListener
            }

            if (firstName.isEmpty()) {
                firstNameField.error = "First name required"
                return@setOnClickListener
            }

            if (lastName.isEmpty()) {
                lastNameField.error = "Last name required"
                return@setOnClickListener
            }

            if (dob.isEmpty() || dob == "Select DOB") {
                Toast.makeText(requireContext(), "Please select a date of birth", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (gender.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // --------- VALIDATION END ----------

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
