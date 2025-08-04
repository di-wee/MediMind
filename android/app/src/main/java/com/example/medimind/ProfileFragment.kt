package com.example.medimind

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.findNavController
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Retrieve saved patientId from SharedPreferences (set during login)
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        if (patientId != null) {
            // Use a coroutine to make the network request on a background thread
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    // Fetch the patient details from backend
                    val profile = ApiClient.retrofitService.getPatient(patientId)

                    // Update greeting text dynamically
                    view.findViewById<TextView>(R.id.topGreetingText).text =
                        "Hello, ${profile.firstName}"

                    // Populate UI with actual patient details
                    view.findViewById<TextView>(R.id.txtUsername).text =
                        "Email/Username: ${profile.email}"
                    view.findViewById<TextView>(R.id.txtNRIC).text =
                        "NRIC: ${profile.nric}"
                    view.findViewById<TextView>(R.id.txtFullName).text =
                        "Name: ${profile.firstName} ${profile.lastName}"
                    view.findViewById<TextView>(R.id.txtGender).text =
                        "Gender: ${profile.gender}"
                    view.findViewById<TextView>(R.id.txtDOB).text =
                        "DOB: ${profile.dob}"

                    // Display clinic name if returned by backend
                    val clinicName = profile.clinic?.clinicName ?: "Unknown clinic"
                    view.findViewById<TextView>(R.id.txtClinic).text = "Clinic: $clinicName"

                } catch (e: Exception) {
                    // Handle error: show a message if fetch fails
                    Toast.makeText(
                        requireContext(),
                        "Failed to load profile: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "No patientId found. Please log in again.",
                Toast.LENGTH_LONG
            ).show()
        }

        // Edit profile button (placeholder for future implementation)
        val editButton = view.findViewById<Button>(R.id.editProfileButton)
        editButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Logout button: clear session and navigate to login
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // 1. Clear SharedPreferences
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()

            // 2. Navigate back to login screen and clear the back stack
            val navController = requireActivity().findNavController(R.id.nav_host_fragment)
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.mainFragment, true) // Clear everything up to mainFragment
                .build()
            navController.navigate(R.id.loginFragment, null, navOptions)
        }
    }
}
