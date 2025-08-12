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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import com.example.medimind.network.UpdatePatientRequest
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale

class EditProfileFragment : Fragment() {

    // Keep the user's non-editable identity fields so we can submit them unchanged
    private var currentNric: String? = null
    private var currentDob: String? = null  // YYYY-MM-DD from backend
    private var originalEmail: String? = null

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

        // Editable fields (NRIC & DOB removed from UI)
        val email = view.findViewById<EditText>(R.id.editEmail)
        val firstName = view.findViewById<EditText>(R.id.editFirstName)
        val lastName = view.findViewById<EditText>(R.id.editLastName)
        val gender = view.findViewById<EditText>(R.id.editGender)
        val password = view.findViewById<EditText>(R.id.editPassword)
        val confirmPassword = view.findViewById<EditText>(R.id.editConfirmPassword)

        // Prefill editable fields + capture immutable fields
        if (patientId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val profile = ApiClient.retrofitService.getPatient(patientId)
                    val lower = (profile.email ?: "").trim().lowercase(Locale.ROOT)
                    email.setText(lower)
                    originalEmail = lower

                    firstName.setText(profile.firstName ?: "")
                    lastName.setText(profile.lastName ?: "")
                    gender.setText(profile.gender ?: "")

                    // store NRIC/DOB (not editable, but included in update payload)
                    currentNric = profile.nric
                    currentDob = profile.dob
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Live email format validation (don’t show error if unchanged)
        email.doAfterTextChanged {
            val raw = it?.toString()?.trim().orEmpty()
            val normalized = raw.lowercase(Locale.ROOT)
            when {
                originalEmail != null && normalized == originalEmail -> email.error = null
                raw.isEmpty() -> email.error = "Email is required"
                !Patterns.EMAIL_ADDRESS.matcher(raw).matches() -> email.error = "Invalid email format"
                else -> email.error = null
            }
        }

        // Save with validation
        view.findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
            // clear errors
            listOf(email, firstName, lastName, gender, password, confirmPassword).forEach { it.error = null }

            val emailStrRaw = email.text.toString().trim()
            val emailStr = emailStrRaw.lowercase(Locale.ROOT)   // normalize (matches backend)
            val firstNameStr = firstName.text.toString().trim()
            val lastNameStr = lastName.text.toString().trim()
            val genderStr = gender.text.toString().trim()
            val pass1 = password.text.toString()
            val pass2 = confirmPassword.text.toString()

            // Required: Email (unless unchanged formatting-wise)
            if (originalEmail == null || emailStr != originalEmail) {
                if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                    email.error = "Enter a valid email"; email.requestFocus(); return@setOnClickListener
                }
            }

            // Required: Names
            if (firstNameStr.isEmpty()) { firstName.error = "First name required"; firstName.requestFocus(); return@setOnClickListener }
            if (lastNameStr.isEmpty()) { lastName.error = "Last name required"; lastName.requestFocus(); return@setOnClickListener }

            // Required: Gender (letters/spaces)
            if (genderStr.isEmpty() || !genderStr.matches(Regex("^[A-Za-z ]+$"))) {
                gender.error = "Enter a valid gender"; gender.requestFocus(); return@setOnClickListener
            }

            // Optional password: only validate if provided
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
            // Ensure we have immutable values before updating
            if (currentNric.isNullOrEmpty() || currentDob.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Unable to proceed. Please reopen this screen and try again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val request = UpdatePatientRequest(
                email = emailStr,                          // normalized
                password = if (pass1.isNotEmpty()) pass1 else null,
                nric = currentNric!!,                      // unchanged
                firstName = firstNameStr,
                lastName = lastNameStr,
                gender = genderStr,
                dob = currentDob!!                         // unchanged
            )

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val updated = ApiClient.retrofitService.updatePatient(patientId, request)

                    // Update the local baseline so further edits don't trigger false errors
                    originalEmail = (updated.email ?: emailStr).trim().lowercase(Locale.ROOT)

                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)

                } catch (e: HttpException) {
                    if (e.code() == 409) {
                        // Backend duplicate email
                        email.error = "Email already in use"
                        email.requestFocus()
                    } else {
                        Toast.makeText(requireContext(), "Update failed (${e.code()})", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
