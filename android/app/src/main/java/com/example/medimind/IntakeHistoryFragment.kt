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
import com.example.medimind.data.IntakeHistoryResponse
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
        val recyclerView = view.findViewById<RecyclerView>(R.id.intakeHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val patientId = sharedPref.getString("patientId", null)

        if (patientId != null) {
            // Greet patient
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val profile = ApiClient.retrofitService.getPatient(patientId)
                    greetingTextView.text = "Hello, ${profile.firstName}"
                } catch (e: Exception) {
                    greetingTextView.text = "Hello"
                }
            }

            // Load intake history
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response: List<IntakeHistoryResponse> =
                        ApiClient.retrofitService.getIntakeHistory(patientId)

                    // Group by date (yyyy-MM-dd)
                    val groupedData = response
                        .groupBy { it.scheduledTime.substring(0, 10) }
                        .map { (date, entriesForDate) ->
                            val intakeGroups = entriesForDate
                                .groupBy {
                                    it.scheduledTime.substring(11, 16) // HH:mm
                                }
                                .map { (time, items) ->
                                    IntakeGroup(
                                        time = "${time}h",
                                        medicines = items.map {
                                            MedicineIntakeItem(
                                                name = it.medicationName,
                                                hasTaken = it.status == "TAKEN"
                                            )
                                        }
                                    )
                                }

                            DateGroup(
                                date = date,
                                intakeGroups = intakeGroups
                            )
                        }

                    recyclerView.adapter = DateAdapter(groupedData)

                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load intake history: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        } else {
            greetingTextView.text = "Hello"
        }

        // Logout
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            sharedPref.edit().clear().apply()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            val navController = requireActivity().findNavController(R.id.nav_host_fragment)
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.mainFragment, true)
                .build()
            navController.navigate(R.id.loginFragment, null, navOptions)
        }
    }
}
