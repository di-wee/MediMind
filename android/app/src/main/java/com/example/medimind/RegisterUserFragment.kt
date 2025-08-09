package com.example.medimind

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import com.example.medimind.network.RegisterRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.Locale
import java.util.regex.Pattern

class RegisterUserFragment : Fragment() {

    private var selectedGender: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // --- Top App Bar (back arrow) ---
        view.findViewById<MaterialToolbar>(R.id.topAppBar)?.apply {
            // Optional: set/update title if not set in XML
            // title = "Create New Account"
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        // 1) Replace Spinner reference
        // val clinicSpinner = view.findViewById<Spinner>(R.id.spinnerClinic)
        val clinicDropdown =
            view.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(
                R.id.autoCompleteClinic
            )

        // 2) Keep your lists
        val clinicNames = mutableListOf<String>()
        val nameToIdMap = mutableMapOf<String, String>()

        // 3) Adapter for the dropdown
        val clinicAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_list_item_1, clinicNames)
        clinicDropdown.setAdapter(clinicAdapter)

        // 4) Load clinics (unchanged logic, just notify the new adapter)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.retrofitService.getClinics()
                clinicNames.clear()
                nameToIdMap.clear()
                response._embedded.clinics.forEach {
                    clinicNames.add(it.clinicName)
                    nameToIdMap[it.clinicName] = it.id
                }
                clinicAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load clinics: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // --- Gender buttons ---
        val btnMale = view.findViewById<MaterialButton>(R.id.btnMale)
        val btnFemale = view.findViewById<MaterialButton>(R.id.btnFemale)

        btnMale.setOnClickListener {
            btnMale.isChecked = true
            btnFemale.isChecked = false
            selectedGender = "Male"
        }
        btnFemale.setOnClickListener {
            btnFemale.isChecked = true
            btnMale.isChecked = false
            selectedGender = "Female"
        }

        // --- Fields ---
        val emailField = view.findViewById<EditText>(R.id.inputEmail)
        val passwordField = view.findViewById<EditText>(R.id.inputPassword)
        val confirmPasswordField = view.findViewById<EditText>(R.id.inputConfirmPassword)
        val nricField = view.findViewById<EditText>(R.id.inputNRIC)
        val firstNameField = view.findViewById<EditText>(R.id.inputFirstName)
        val lastNameField = view.findViewById<EditText>(R.id.inputLastName)

        // DOB inputs (DD/MM/YYYY split)
        val dobDay = view.findViewById<TextInputEditText>(R.id.dobDay)
        val dobMonth = view.findViewById<TextInputEditText>(R.id.dobMonth)
        val dobYear = view.findViewById<TextInputEditText>(R.id.dobYear)

        fun moveNext(from: TextInputEditText, to: TextInputEditText?, max: Int) {
            from.doAfterTextChanged {
                if ((it?.length ?: 0) >= max) to?.requestFocus()
            }
        }
        moveNext(dobDay, dobMonth, 2)
        moveNext(dobMonth, dobYear, 2)

        // --- Register button ---
        view.findViewById<Button>(R.id.registerButton).setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()
            val nric = nricField.text.toString().trim()
            val firstName = firstNameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val gender = selectedGender ?: ""
            val dayStr = dobDay.text?.toString()?.trim() ?: ""
            val monthStr = dobMonth.text?.toString()?.trim() ?: ""
            val yearStr = dobYear.text?.toString()?.trim() ?: ""

            // ---- VALIDATION ----
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

            if (gender.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dayStr.isEmpty() || monthStr.isEmpty() || yearStr.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter your full date of birth",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val day = dayStr.toIntOrNull()
            val month = monthStr.toIntOrNull()
            val year = yearStr.toIntOrNull()

            if (day == null || month == null || year == null || !isValidDate(year, month, day)) {
                Toast.makeText(
                    requireContext(),
                    "Enter a valid date (DD/MM/YYYY)",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // ---- END VALIDATION ----

            val dob = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

            val selectedClinicName = clinicDropdown.text?.toString() ?: ""
            val request = RegisterRequest(
                email = email,
                password = password,
                nric = nric,
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                dob = dob,
                clinicName = selectedClinicName
            )

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = ApiClient.retrofitService.register(request)
                    Toast.makeText(
                        requireContext(),
                        "Registered: ${response.firstName} ${response.lastName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_registerUserFragment_to_loginFragment)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Registration failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun isValidDate(year: Int, month: Int, day: Int): Boolean {
        return try {
            if (year < 1900 || year > 2100) return false
            val cal = GregorianCalendar()
            cal.isLenient = false
            cal.set(year, month - 1, day) // Calendar month is 0-based
            cal.time // triggers validation
            true
        } catch (e: Exception) {
            false
        }
    }
}
