package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import com.example.medimind.network.UpdatePatientRequest
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_edit_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Back arrow
        view.findViewById<MaterialToolbar>(R.id.topAppBar).setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        // Fields
        val email = view.findViewById<EditText>(R.id.editEmail)
        val nric = view.findViewById<EditText>(R.id.editNRIC)
        val firstName = view.findViewById<EditText>(R.id.editFirstName)
        val lastName = view.findViewById<EditText>(R.id.editLastName)
        val gender = view.findViewById<EditText>(R.id.editGender)
        val dob = view.findViewById<EditText>(R.id.editDob)
        val password = view.findViewById<EditText>(R.id.editPassword)
        val confirmPassword = view.findViewById<EditText>(R.id.editConfirmPassword)

        // Prefill
        if (patientId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val profile = ApiClient.retrofitService.getPatient(patientId)
                    email.setText(profile.email ?: "")
                    nric.setText(profile.nric ?: "")
                    firstName.setText(profile.firstName ?: "")
                    lastName.setText(profile.lastName ?: "")
                    gender.setText(profile.gender ?: "")
                    dob.setText(profile.dob ?: "") // YYYY-MM-DD
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Save with validation
        view.findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
            // clear errors
            listOf(email, nric, firstName, lastName, gender, dob, password, confirmPassword).forEach { it.error = null }

            val emailStr = email.text.toString().trim()
            val nricStr = nric.text.toString().trim().uppercase()
            val firstNameStr = firstName.text.toString().trim()
            val lastNameStr = lastName.text.toString().trim()
            val genderStr = gender.text.toString().trim()
            val dobStr = dob.text.toString().trim()
            val pass1 = password.text.toString()
            val pass2 = confirmPassword.text.toString()

            // Email
            if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                email.error = "Enter a valid email"; email.requestFocus(); return@setOnClickListener
            }
            // NRIC
            if (!Regex("^[STFG]\\d{7}[A-Z]$").matches(nricStr)) {
                nric.error = "Enter a valid NRIC (e.g., S1234567A)"; nric.requestFocus(); return@setOnClickListener
            }
            // Names
            if (firstNameStr.isEmpty()) { firstName.error = "First name required"; firstName.requestFocus(); return@setOnClickListener }
            if (lastNameStr.isEmpty()) { lastName.error = "Last name required"; lastName.requestFocus(); return@setOnClickListener }
            // Gender (letters only if provided)
            if (genderStr.isNotEmpty() && !genderStr.matches(Regex("^[A-Za-z ]+$"))) {
                gender.error = "Enter a valid gender"; gender.requestFocus(); return@setOnClickListener
            }
            // DOB
            if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(dobStr)) {
                dob.error = "Use YYYY-MM-DD"; dob.requestFocus(); return@setOnClickListener
            }
            // Password (optional in this version; validate only when provided)
            if (pass1.isNotEmpty() && pass1.length < 6) {
                password.error = "At least 6 characters"; password.requestFocus(); return@setOnClickListener
            }
            if (pass1.isNotEmpty() && pass1 != pass2) {
                confirmPassword.error = "Passwords do not match"; confirmPassword.requestFocus(); return@setOnClickListener
            }

            if (patientId == null) {
                Toast.makeText(requireContext(), "No patientId found. Please log in again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val request = UpdatePatientRequest(
                email = emailStr,
                password = if (pass1.isNotEmpty()) pass1 else null,
                nric = nricStr,
                firstName = firstNameStr,
                lastName = lastNameStr,
                gender = genderStr,
                dob = dobStr
            )

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    ApiClient.retrofitService.updatePatient(patientId, request)
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
