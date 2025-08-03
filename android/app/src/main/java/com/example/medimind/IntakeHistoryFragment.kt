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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medimind.adapter.DateAdapter
import com.example.medimind.model.DateGroup
import com.example.medimind.model.IntakeGroup
import com.example.medimind.model.MedicineIntakeItem
import com.example.medimind.network.ApiClient
import kotlinx.coroutines.launch

class IntakeHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intake_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val greetingTextView = view.findViewById<TextView>(R.id.topGreetingText)

        // Retrieve saved patientId
        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        if (patientId != null) {
            // Fetch patient details to personalize greeting
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val profile = ApiClient.retrofitService.getPatient(patientId)
                    greetingTextView.text = "Hello, ${profile.firstName}"
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load profile: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    greetingTextView.text = "Hello"
                }
            }
        } else {
            greetingTextView.text = "Hello"
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

        val recyclerView = view.findViewById<RecyclerView>(R.id.intakeHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Mock data (unchanged)
        val mockData = listOf(
            DateGroup(
                date = "21/07/2025",
                intakeGroups = listOf(
                    IntakeGroup(
                        time = "0900h",
                        medicines = listOf(
                            MedicineIntakeItem("Amlodipine", true),
                            MedicineIntakeItem("Xanax", true),
                            MedicineIntakeItem("Ibuprofen", false),
                            MedicineIntakeItem("Loratadine", true)
                        )
                    )
                )
            ),
            DateGroup(
                date = "20/07/2025",
                intakeGroups = listOf(
                    IntakeGroup(
                        time = "2100h",
                        medicines = listOf(
                            MedicineIntakeItem("Amlodipine", true),
                            MedicineIntakeItem("Loratadine", true),
                            MedicineIntakeItem("Xanax", false)
                        )
                    )
                )
            )
        )

        recyclerView.adapter = DateAdapter(mockData)
    }
}
