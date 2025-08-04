package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medimind.network.ApiClient
import com.example.medimind.network.UpdatePatientRequest
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        // Get references to fields
        val email = view.findViewById<EditText>(R.id.editEmail)
        val nric = view.findViewById<EditText>(R.id.editNRIC)
        val firstName = view.findViewById<EditText>(R.id.editFirstName)
        val lastName = view.findViewById<EditText>(R.id.editLastName)
        val gender = view.findViewById<EditText>(R.id.editGender)
        val dob = view.findViewById<EditText>(R.id.editDob)
        val password = view.findViewById<EditText>(R.id.editPassword)
        val confirmPassword = view.findViewById<EditText>(R.id.editConfirmPassword)

        // Pre-fill current details from backend
        if (patientId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val profile = ApiClient.retrofitService.getPatient(patientId)
                email.setText(profile.email)
                nric.setText(profile.nric)
                firstName.setText(profile.firstName)
                lastName.setText(profile.lastName)
                gender.setText(profile.gender)
                dob.setText(profile.dob)
            }
        }

        // Save button logic
        view.findViewById<Button>(R.id.saveProfileButton).setOnClickListener {
            val pass1 = password.text.toString()
            val pass2 = confirmPassword.text.toString()

            // Validate password confirmation
            if (pass1.isNotEmpty() && pass1 != pass2) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (patientId != null) {
                val request = UpdatePatientRequest(
                    email.text.toString(),
                    if (pass1.isNotEmpty()) pass1 else null,
                    nric.text.toString(),
                    firstName.text.toString(),
                    lastName.text.toString(),
                    gender.text.toString(),
                    dob.text.toString()
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
}
